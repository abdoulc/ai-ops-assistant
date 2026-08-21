# AI Ops Assistant — Product and Engineering Backlog

> This backlog is a living document. Priorities may change as technical assumptions are validated.

## Vision

Build a local-first assistant that investigates production incidents using documentation, incidents, deployments, logs, and metrics. Answers must be traceable, secure, observable, and measurable.

The project demonstrates senior backend engineering: deterministic contracts around non-deterministic models, testable boundaries, safe tools, resilience, evaluation, and explicit trade-offs.

## Core Decisions

- Java 21, Spring Boot, and Spring AI.
- Ollama is the first local inference engine; no paid API is required.
- Spring AI and provider details stay in infrastructure.
- PostgreSQL and pgvector are the initial data platform.
- REST endpoints are versioned under `/api/v1`.
- JUnit 5, ArchUnit, Testcontainers, Docker, and Docker Compose.
- React remains outside the MVP until API workflows are stable.

## Non-negotiable Principles

1. Domain and application code do not depend on Spring AI, Ollama, or provider SDKs.
2. Important AI outputs use typed and validated contracts.
3. RAG answers cite evidence or explicitly abstain.
4. Write tools require authorization; destructive actions require human approval.
5. Prompts, models, and inference settings are versioned.
6. Secrets and sensitive content are not logged by default.
7. Every sprint delivers a tested, demonstrable capability.

## Tracking

- `[ ]` planned
- `[~]` in progress
- `[x]` completed
- `[!]` blocked
- `P0` required, `P1` important, `P2` optional

## Milestones

| Milestone | Sprints | Outcome |
|---|---:|---|
| M0 — Local foundation | S0–S1 | Typed local incident analysis |
| M1 — Useful RAG | S2–S5 | Indexed, searchable, cited documentation |
| M2 — Investigation | S6–S8 | RAG plus authorized operational tools |
| M3 — Production readiness | S9–S12 | Observable, evaluated, resilient system |
| M4 — Portfolio | S13 | Reproducible demo and CI |
| M5 — Inference engineering | S14–S15 | Benchmarked local inference engines |
| M6 — AI platform | S16–S18 | Kubernetes deployment and operations |
| M7 — Advanced AI Ops | S19–S21 | Guardrails, MCP, governed orchestration |

---

## S0 — Local Technical Foundation

**Goal:** start the platform locally and enforce architectural boundaries.

- [x] `P0` Stabilize Maven/Spring Boot on Java 21.
- [x] `P0` Organize `domain`, `application`, `infrastructure`, and `api`.
- [x] `P0` Define provider-independent LLM contracts.
- [x] `P0` Implement the Spring AI/Ollama infrastructure adapter.
- [x] `P0` Add Ollama, PostgreSQL, and pgvector with volumes and healthchecks.
- [x] `P0` Externalize model, URL, inference settings, retries, and timeouts.
- [x] `P0` Add a local profile and secret-free environment example.
- [x] `P0` Add a deterministic fake and use-case test.
- [x] `P1` Add Actuator readiness probes.
- [x] `P1` Protect boundaries with ArchUnit.
- [x] `P1` Add ADR-001 and project documentation.

**Acceptance:** tests target Java 21, Compose services become healthy, models are configurable without Java changes, and setup is documented.

## S1 — Typed Stacktrace Analysis

**Goal:** deliver the first end-to-end AI Ops use case.

- [x] `P0` Model `Severity` and `IncidentAnalysis`.
- [x] `P0` Add structured generation behind `LlmGateway`.
- [x] `P0` Create `POST /api/v1/incidents/analyze`.
- [x] `P0` Validate service name, stacktrace presence, and maximum size.
- [x] `P0` Return summary, probable cause, severity, hypotheses, recommendations, and confidence.
- [x] `P0` Handle timeout, unavailable model, and invalid model output.
- [x] `P0` Version and test the incident-analysis prompt.
- [x] `P0` Add domain, use-case, API, and adapter unit tests.
- [ ] `P0` Add a Spring AI adapter integration test without a mocked `ChatClient`.
- [x] `P1` Add request correlation and safe logging.

## S2 — Document Ingestion

- [ ] Upload Markdown and plain text.
- [ ] Model versions, checksums, metadata, and ingestion status.
- [ ] Implement configurable, idempotent chunking.
- [ ] Add Flyway and PostgreSQL Testcontainers tests.
- [ ] Add safe deletion and reindexing; support PDF/HTML later.

## S3 — Semantic Search with pgvector

- [ ] Define a provider-independent `EmbeddingGateway`.
- [ ] Store embeddings with model and dimension metadata.
- [ ] Create `POST /api/v1/search` with filters, `topK`, and scores.
- [ ] Add controlled reindexing and similarity integration tests.

## S4 — RAG v1 with Citations

- [ ] Create `POST /api/v1/ask`.
- [ ] Use a versioned, context-grounded prompt.
- [ ] Return citations and chunk identifiers.
- [ ] Abstain when evidence is insufficient.
- [ ] Treat document instructions as untrusted data.

## S5 — RAG Quality

- [ ] Configure thresholds, chunking, overlap, and metadata filters.
- [ ] Build a dataset with expected sources and no-answer cases.
- [ ] Measure hit-rate@k, MRR, abstention, and p50/p95 latency.
- [ ] Evaluate hybrid search and reranking only when justified.

## S6 — Read-only Tool Calling

- [ ] Define tool schemas, timeouts, permissions, and typed results.
- [ ] Add incident, deployment, and metrics tools.
- [ ] Validate all model-generated arguments.
- [ ] Bound calls, loop depth, duration, and result size.
- [ ] Test unknown tools, invalid inputs, timeout, and loops.

## S7 — Multi-source Investigation

- [ ] Persist investigations and expose an investigation endpoint.
- [ ] Orchestrate RAG and tools in a bounded loop.
- [ ] Produce timeline, evidence, hypotheses, confidence, and actions.
- [ ] Separate facts, inferences, and missing information.
- [ ] Support cancellation, budgets, partial failure, and optional SSE.

## S8 — Tool Security

- [ ] Add OAuth2/OIDC roles.
- [ ] Classify tools as `READ`, `WRITE`, or `DESTRUCTIVE`.
- [ ] Authorize outside the LLM.
- [ ] Require expiring human approval for sensitive actions.
- [ ] Make effects idempotent, auditable, and security-tested.

## S9 — End-to-end Observability

- [ ] Instrument API, embeddings, retrieval, LLM, and tools.
- [ ] Propagate request and trace identifiers.
- [ ] Measure latency, errors, tokens, context, chunks, and tool calls.
- [ ] Add Prometheus, Grafana, OTLP traces, SLI/SLOs, and redaction.

## S10 — Continuous AI Evaluation

- [ ] Version datasets, prompts, models, parameters, and results.
- [ ] Separate deterministic retrieval from probabilistic generation tests.
- [ ] Measure groundedness, citations, abstention, and tool selection.
- [ ] Add adversarial cases, thresholds, and human review.

## S11 — Resilience and Performance

- [ ] Define separate timeout budgets.
- [ ] Retry only transient, safe operations.
- [ ] Add circuit breakers, bulkheads, concurrency limits, and backpressure.
- [ ] Queue indexing and run load and failure tests.

## S12 — Multi-provider Support

- [ ] Model provider capabilities explicitly.
- [ ] Keep LLM and embedding providers separate.
- [ ] Add a second adapter and shared contract tests.
- [ ] Never fall back silently from local inference to cloud.

## S13 — Delivery and Portfolio

- [ ] Maintain diagrams and product-focused documentation.
- [ ] Provide synthetic, non-confidential demo data.
- [ ] Add CI, static analysis, migrations, and image build.
- [ ] Generate an SBOM and scan dependencies/images.
- [ ] Prepare a repeatable five-minute demo.

---

## Phase 2 — Local Inference Engineering

## S14 — Open Model Laboratory

- [ ] Compare representative Mistral, Llama, and Qwen models.
- [ ] Verify licenses, provenance, digests, and restrictions.
- [ ] Benchmark quality, TTFT, tokens/s, latency, RAM/VRAM, and errors.
- [ ] Record model, tokenizer, prompt, settings, and hardware.

## S15 — Ollama, vLLM, and Quantization

- [ ] Containerize pinned Ollama and vLLM deployments.
- [ ] Add an OpenAI-compatible vLLM adapter.
- [ ] Compare GGUF and AWQ where supported.
- [ ] Measure quality, memory, throughput, concurrency, and cold start.
- [ ] Test readiness, streaming, cancellation, and backpressure.

## Phase 3 — Kubernetes AI Platform

## S16 — Kubernetes and Helm

- [ ] Package the stack with Helm.
- [ ] Configure resources, probes, secrets, and rolling updates.
- [ ] Enforce non-root execution and NetworkPolicies.
- [ ] Test install, upgrade, smoke checks, and rollback.

## S17 — Kubernetes Inference Serving

- [ ] Deploy persistent model caching and an optional GPU profile.
- [ ] Expose queue, TTFT, throughput, and saturation metrics.
- [ ] Compare HPA and KEDA using demand-related metrics.
- [ ] Bound queues and document scale-from-zero cost.

## S18 — HA Vector Data and Ingestion

- [ ] Define volume, RPO, and RTO before changing databases.
- [ ] Measure pgvector as the baseline.
- [ ] Compare Qdrant and Milvus operationally.
- [ ] Test backup, restore, failure, and upgrades.
- [ ] Add idempotent Spring Batch ingestion and blue/green reindexing.

## Phase 4 — Advanced AI Operations

## S19 — LLM Observability and Guardrails

- [ ] Compare self-hosted Langfuse and Phoenix/Arize.
- [ ] Trace model, prompt version, TTFT, tokens, retrieval, and tools.
- [ ] Define capture, redaction, encryption, retention, and access policies.
- [ ] Evaluate layered input/output guardrails and AI incident runbooks.

## S20 — Model Context Protocol

- [ ] Study MCP lifecycle, capabilities, tools, resources, and transports.
- [ ] Build a read-only AI Ops MCP server and client.
- [ ] Preserve application authorization outside MCP.
- [ ] Allowlist servers and test malicious capability changes and results.

## S21 — Governed Multi-agent Orchestration

- [ ] Establish a measured single-agent baseline.
- [ ] Define bounded coordinator, retrieval, and operations roles.
- [ ] Keep deterministic state ownership and termination.
- [ ] Limit recursion, fan-out, concurrency, and permissions.
- [ ] Keep the single-agent design unless benefits are measured.

---

## Cross-cutting Backlog

- [ ] Add formatting, static analysis, and selected mutation testing.
- [x] Protect layer dependencies with ArchUnit.
- [ ] Define data retention, deletion, backup, and tenant isolation.
- [ ] Maintain a threat model for injection, exfiltration, poisoning, and tool abuse.
- [ ] Treat documents and tool results as untrusted data.
- [ ] Maintain a versioned adversarial test suite.

## Outside the MVP

- Autonomous access to real production systems.
- Automatically executed destructive actions.
- Multi-agent workflows before a measured baseline.
- Kubernetes before validated load and availability requirements.
- Unjustified microservices, model fine-tuning, and an early React UI.

## Major Risks

| Risk | Response |
|---|---|
| Invalid LLM output | Strict validation, bounded repair, explicit errors |
| Hallucinated diagnosis | Citations, abstention, evaluation |
| Indirect prompt injection | Untrusted context, external policy, human approval |
| Insufficient local hardware | Configurable models, limits, benchmarks |
| Embedding incompatibility | Version model/dimensions and reindex explicitly |
| Sensitive telemetry | Redaction, sampling, minimal capture |
| Model licensing | Verify license, source, digest, and model card |
| Queue or GPU saturation | Bounded queues, backpressure, inference metrics |
| Platform over-engineering | Entry criteria and ADRs before adoption |

## Immediate Next Actions

- [x] Complete S0.
- [ ] Complete S1 typed incident analysis.
- [ ] Select an embedding model before S3.
- [ ] Record a short S0 demonstration.

## Architecture Decision Log

- [x] `ADR-001` — Isolate Spring AI behind `LlmGateway`.
- [ ] `ADR-002` — Local Ollama and no implicit cloud fallback.
- [ ] `ADR-003` — PostgreSQL and pgvector as the initial data platform.
- [ ] `ADR-004` — Typed outputs, validation, and abstention.
- [ ] `ADR-005` — Tool permissions and human approval.
- [ ] `ADR-006` — Local model, quantization, and hardware budget.
- [ ] `ADR-007` — Ollama versus vLLM.
- [ ] `ADR-008` — Kubernetes entry criteria and SLO/RPO/RTO.
- [ ] `ADR-009` — pgvector versus Qdrant versus Milvus.
- [ ] `ADR-010` — LLM observability and capture policy.
- [ ] `ADR-011` — MCP versus direct integration.
- [ ] `ADR-012` — Single-agent baseline and multi-agent criteria.
