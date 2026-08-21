# AI Ops Assistant

AI Ops Assistant is a local-first Java backend for building reliable AI-assisted operations workflows. It currently analyzes an incident stack trace through a typed API, uses a local Ollama model through Spring AI, and returns a validated diagnosis without requiring a paid external API.

The completed S0 and S1 milestones establish the architecture and the first end-to-end use case. The next milestone introduces document ingestion as the foundation for retrieval-augmented generation (RAG).

## Current Capabilities

- Java 21 and Spring Boot 4;
- local inference with Spring AI, Ollama, and Qwen3;
- provider-independent `LlmGateway` domain port;
- typed incident analysis with severity, hypotheses, recommendations, and confidence;
- request validation and `ProblemDetail` errors;
- dedicated handling for timeouts, unavailable models, and invalid model output;
- correlation IDs and safe HTTP metadata logging;
- PostgreSQL with pgvector, ready for the RAG milestones;
- Docker Compose health checks and persistent model storage;
- unit, MVC, architecture, adapter, and Spring AI integration tests.

This is not an autonomous remediation agent. It performs analysis only and has no operational write tools.

## Architecture

```text
HTTP client
    |
    v
CorrelationIdFilter
    |
    v
IncidentController                 API layer
    |
    v
AnalyzeIncidentUseCase             Application layer
    |
    v
LlmGateway                         Domain port
    ^
    |
SpringAiLlmGateway                 Infrastructure adapter
    |
    v
Spring AI -> Ollama                Local inference
```

Package responsibilities:

```text
org.abdel.aiops
|-- api             HTTP controllers, validation, filters, and errors
|-- application     framework-independent use cases and prompt templates
|-- domain          business models, ports, and provider-neutral exceptions
`-- infrastructure  Spring configuration and external adapters
```

Spring AI is isolated behind the domain port so the application layer does not depend on a specific model provider. See [ADR-001](docs/adr/ADR-001-isolate-spring-ai-behind-llm-gateway.md).

## Prerequisites

- Java 21;
- Maven 3.8 or later;
- Docker Desktop with Docker Compose;
- enough memory for PostgreSQL and the selected Ollama model.

The default model is `qwen3:0.6b`. It is practical on constrained hardware, but a larger model will generally produce better incident diagnoses.

## First-Time Setup

Start Ollama and PostgreSQL:

```powershell
docker compose up -d ollama postgres
```

Download the default model into the Docker-managed Ollama instance:

```powershell
docker compose exec ollama ollama pull qwen3:0.6b
docker compose exec ollama ollama list
```

The `ollama_data` volume preserves downloaded models when the container is recreated.

## Run Spring Locally

In this mode, Spring runs on Windows while Ollama and PostgreSQL run in Docker:

```text
Application: http://localhost:8081
Ollama:      http://localhost:11435
PostgreSQL:  localhost:5432
```

Start the dependencies and run Spring with the local profile:

```powershell
docker compose up -d ollama postgres
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

For an IDE run configuration, set:

```text
SPRING_PROFILES_ACTIVE=local
```

Do not activate the `local` profile when the application itself runs in Docker.

## Run the Full Docker Stack

```powershell
mvn clean package
docker compose up -d --build
docker compose ps
```

The application container uses Docker service discovery:

```text
app_engine -> http://ollama:11434
app_engine -> jdbc:postgresql://postgres:5432/vector_db
```

The Docker application is exposed on `http://localhost:8080`.

## Analyze an Incident

Endpoint:

```http
POST /api/v1/incidents/analyze
Content-Type: application/json
X-Correlation-ID: incident-123
```

PowerShell example for a locally running Spring application:

```powershell
$body = @{
    service = "payment-service"
    stackTrace = "java.sql.SQLTransientConnectionException: timeout"
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri "http://localhost:8081/api/v1/incidents/analyze" `
    -Method Post `
    -Headers @{ "X-Correlation-ID" = "incident-123" } `
    -ContentType "application/json" `
    -Body $body
```

Example response:

```json
{
  "summary": "Database connections are exhausted",
  "probableCause": "Connection pool exhaustion",
  "severity": "HIGH",
  "hypotheses": ["Long-running transactions"],
  "recommendations": ["Inspect connection-pool metrics"],
  "confidence": 0.85
}
```

`service` is required and limited to 100 characters. `stackTrace` is required and limited to 50,000 characters.

The lower-level `POST /api/v1/llm/generate` endpoint remains available as a text-generation playground, but incident analysis is the primary product API.

## Error Contract

Errors use `application/problem+json` and do not expose raw provider messages.

| Situation | HTTP status | Title |
|---|---:|---|
| Invalid request or malformed JSON | 400 | `Validation failed` or `Malformed request` |
| Invalid structured model output | 502 | `Invalid LLM response` |
| Configured model unavailable | 503 | `LLM unavailable` |
| Model request timeout | 504 | `LLM timeout` |

Example:

```json
{
  "title": "LLM timeout",
  "status": 504,
  "detail": "The language model did not respond in time",
  "instance": "/api/v1/incidents/analyze"
}
```

## Correlation and Safe Logging

Clients may send `X-Correlation-ID`. Valid identifiers are returned in the response and included in application logs. Missing or unsafe values are replaced with a UUID.

The application logs request method, path, response status, and duration. It deliberately avoids logging request bodies, supplied stack traces, full prompts, and raw model responses. Keep Spring Web and Spring AI logging at `INFO` wherever incident data may be sensitive.

## Configuration Reference

| Variable | Purpose | Local default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Activates environment-specific configuration | none |
| `OLLAMA_BASE_URL` | Ollama URL for the local profile | `http://localhost:11435` |
| `OLLAMA_CHAT_MODEL` | Ollama chat model | `qwen3:0.6b` |
| `HTTP_CONNECT_TIMEOUT` | HTTP connection timeout | `3s` |
| `HTTP_READ_TIMEOUT` | Maximum inference response wait | `2m` |
| `AI_RETRY_MAX_ATTEMPTS` | Maximum Spring AI attempts | `1` |

Qwen3 thinking is explicitly disabled for this structured-output workflow. With a small output budget, reasoning can consume every generated token and leave no JSON response. The Docker equivalent is `SPRING_AI_OLLAMA_CHAT_THINK=false`.

Copy `.env.example` when overrides are needed. Do not commit credentials, production stack traces, prompts containing confidential data, or model responses derived from private incidents.

## Health Checks

```text
Local Spring:     http://localhost:8081/actuator/health/readiness
Docker stack:     http://localhost:8080/actuator/health/readiness
Ollama host port: http://localhost:11435/api/tags
```

## Tests

Run the complete suite:

```powershell
mvn test
```

The suite covers:

- domain invariants for `IncidentAnalysis`;
- use cases with a deterministic `FakeLlmGateway`;
- prompt version and contract;
- request validation and HTTP error contracts with MockMvc;
- correlation ID validation and MDC cleanup;
- architecture boundaries with ArchUnit;
- adapter error translation;
- real Spring AI/Ollama HTTP serialization and structured conversion through MockWebServer.

The integration tests do not require Docker or a downloaded model. They use the real Spring AI client against a deterministic local HTTP fixture.

## Troubleshooting

### Model not found

The Ollama container and a Windows Ollama installation have separate model stores. Install the model inside the container used by this project:

```powershell
docker compose exec ollama ollama pull qwen3:0.6b
```

### Connection closes before the response

Compare the Docker network with the published Windows port:

```powershell
docker compose exec ops-app-engine curl http://ollama:11434/api/tags
curl.exe http://127.0.0.1:11435/api/tags
```

If the internal request succeeds but the published port returns an empty response, recreate only Ollama. The named volume keeps the models:

```powershell
docker compose up -d --force-recreate ollama
```

### Structured analysis returns 502

Confirm that Qwen thinking is disabled and restart Spring after changing configuration:

```yaml
spring:
  ai:
    ollama:
      chat:
        think: false
```

### Generation is slow

The first request may load the model into memory. Inspect resource usage with:

```powershell
docker stats ollama
```

## Project Status and Roadmap

- S0 - Local foundation: complete;
- S1 - Typed stacktrace analysis: complete;
- S2 - Document ingestion: next.

The detailed roadmap is maintained in [BACKLOG.md](BACKLOG.md).

Future milestones add document ingestion, pgvector search, cited RAG, read-only operational tools, authorization, observability, evaluation, resilience, Kubernetes deployment, guardrails, and governed agent workflows.

## Current Limitations

- diagnoses are model-generated and must not be treated as authoritative;
- the default 0.6B model favors low resource usage over diagnostic quality;
- there is no retrieval pipeline or evidence citation yet;
- correlation context is not yet propagated to every Reactor worker thread;
- no operational tools or write actions are available;
- authentication and authorization are not implemented yet.
