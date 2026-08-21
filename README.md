# AI Ops Assistant

AI Ops Assistant is a Java backend for experimenting with reliable, local-first AI operations workflows. The current foundation exposes a provider-independent LLM use case backed by Spring AI and a local Ollama instance.

The project is being built incrementally toward incident analysis, retrieval-augmented generation, tool calling, observability, evaluation, and guarded operational workflows.

## Current Scope

The S0 technical foundation currently provides:

- Java 21 and Spring Boot;
- a domain-level `LlmGateway` abstraction;
- a Spring AI adapter backed by Ollama;
- local inference without a paid external API;
- PostgreSQL with pgvector, ready for later RAG work;
- Docker Compose healthchecks and persistent volumes;
- Actuator readiness probes;
- deterministic unit tests with `FakeLlmGateway`;
- ArchUnit rules protecting layer boundaries;
- configurable connection and response timeouts.

This is not yet an autonomous operations agent. The current API is intentionally small so that each later capability can be introduced and evaluated independently.

## Architecture

```text
HTTP request
    │
    ▼
LlmController                         API adapter
    │
    ▼
GenerateResponseUseCase               Application logic
    │
    ▼
LlmGateway                            Domain port
    ▲
    │
SpringAiLlmGateway                    Infrastructure adapter
    │
    ▼
Spring AI → Ollama                    Local inference
```

Package responsibilities:

```text
org.abdel.aiops
├── api             HTTP controllers and DTOs
├── application     framework-independent use cases
├── domain          business contracts and models
└── infrastructure  Spring configuration and external adapters
```

The architecture decision is documented in [ADR-001](docs/adr/ADR-001-isolate-spring-ai-behind-llm-gateway.md).

## Prerequisites

- Java 21
- Maven 3.8 or later
- Docker Desktop with Docker Compose
- At least enough memory to run PostgreSQL and a small Ollama model

The default local model is `qwen3:0.6b`. It is suitable for validating the integration on constrained hardware, but its answer quality is limited.

## Configuration Modes

The application supports two execution modes.

### Spring on Windows, dependencies in Docker

Activate the `local` Spring profile. The application runs on Windows and connects to Ollama through the exposed host port:

```text
Spring: http://localhost:8080
Ollama: http://localhost:11435
PostgreSQL: localhost:5432
```

Configuration comes from `application.yml` and `application-local.yml`.

### Full Docker Compose stack

The application, Ollama, and PostgreSQL all run in Docker. Containers use Compose service names and internal ports:

```text
app_engine → http://ollama:11434
app_engine → jdbc:postgresql://postgres:5432/vector_db
```

Do not enable the `local` profile inside Docker.

## First-Time Setup

Start Ollama and PostgreSQL:

```powershell
docker compose up -d ollama postgres
```

Download the configured model into the persistent Ollama volume:

```powershell
docker compose exec ollama ollama pull qwen3:0.6b
```

Verify the installed models:

```powershell
docker compose exec ollama ollama list
```

The named `ollama_data` volume keeps downloaded models when the container is recreated.

## Run Spring Locally

Start the dependencies:

```powershell
docker compose up -d ollama postgres
```

Run Spring with the local profile:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Alternatively, configure this environment variable in the IDE run configuration:

```text
SPRING_PROFILES_ACTIVE=local
```

The startup logs should confirm:

```text
The following 1 profile is active: "local"
```

## Run the Full Docker Stack

The Dockerfile copies the packaged JAR, so build it first:

```powershell
mvn clean package
```

Build and start all services:

```powershell
docker compose up -d --build
```

Check their status:

```powershell
docker compose ps
```

Expected result:

```text
app_engine          healthy
ollama              healthy
postgres_pgvector   healthy
```

## API Usage

Endpoint:

```http
POST /api/v1/llm/generate
Content-Type: application/json
```

Request:

```json
{
  "prompt": "Reply with OK only"
}
```

PowerShell example:

```powershell
$body = @{
    prompt = "Reply with OK only"
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/llm/generate" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

Example response:

```json
{
  "content": "OK"
}
```

Local inference speed depends heavily on CPU, GPU availability, Docker resources, model size, and whether the model is already loaded.

## Health Checks

Application readiness:

```text
http://localhost:8080/actuator/health/readiness
```

Ollama API through the host:

```text
http://localhost:11435/api/tags
```

Inspect all Compose services:

```powershell
docker compose ps
```

## Tests

Run all tests:

```powershell
mvn test
```

Run only the architecture tests:

```powershell
mvn -Dtest=ArchitectureTest test
```

The test suite currently includes:

- a deterministic unit test for `GenerateResponseUseCase`;
- a fake LLM gateway requiring neither Spring nor Ollama;
- ArchUnit rules that prevent `domain` and `application` from depending on Spring or outer adapters.

## Configuration Reference

| Variable | Purpose | Local default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Activates environment-specific Spring configuration | none |
| `OLLAMA_BASE_URL` | Ollama URL used by the `local` profile | `http://localhost:11435` |
| `OLLAMA_CHAT_MODEL` | Model used by the `local` profile | `qwen3:0.6b` |
| `HTTP_CONNECT_TIMEOUT` | Maximum time allowed to establish an HTTP connection | `3s` |
| `HTTP_READ_TIMEOUT` | Maximum wait for an HTTP response | `2m` |
| `AI_RETRY_MAX_ATTEMPTS` | Maximum Spring AI attempts per call | `1` |

Docker Compose supplies the equivalent standard Spring properties directly to the application container.

Copy `.env.example` when local environment overrides are needed. Never commit real credentials or sensitive values.

## Troubleshooting

### Model not found

If Ollama returns a `404` such as `model 'qwen3:0.6b' not found`, install the model inside the Docker-managed Ollama instance:

```powershell
docker compose exec ollama ollama pull qwen3:0.6b
```

An Ollama installation on Windows and the Ollama container use separate model stores.

### Ollama address differs by execution mode

Use:

- `http://localhost:11435` when Spring runs on Windows;
- `http://ollama:11434` when Spring runs in Docker Compose.

Using `localhost` from inside `app_engine` points back to the application container, not to Ollama.

### Generation is slow

Inspect container resources during a request:

```powershell
docker stats ollama
```

The first request can be slower while the model is loaded. If every request remains slow, verify the CPU and memory allocated to Docker Desktop or select a smaller model.

### Container configuration changed but behavior did not

Rebuild the JAR and recreate the application container:

```powershell
mvn clean package
docker compose up -d --build --force-recreate ops-app-engine
```

## Project Roadmap

The detailed incremental roadmap is maintained in [BACKLOG.md](BACKLOG.md).

The next product milestone introduces typed incident analysis. Later milestones add document ingestion, pgvector search, RAG with citations, tool calling, authorization, observability, evaluation, and advanced local inference.

## Current Limitations

- Generated output is not yet validated against a structured contract.
- The API does not yet translate LLM failures into dedicated error responses.
- There is no RAG pipeline or semantic search yet.
- There are no operational tools or write actions.
- The default small model prioritizes local compatibility over answer quality.
