# ADR-001 — Isolate Spring AI Behind `LlmGateway`

- **Status:** Accepted
- **Date:** 2026-08-21
- **Decision owners:** AI Ops Assistant team

## Context

AI Ops Assistant currently uses Spring AI to communicate with a model served locally by Ollama.

The first application use case, `GenerateResponseUseCase`, needs to request a text generation from an LLM. It could depend directly on Spring AI's `ChatClient`, but that would couple the application layer to a framework-specific API.

The project may later support other inference engines or providers, such as vLLM, OpenAI, or Anthropic. Its use cases must also remain testable without starting Spring, Docker, or Ollama.

The design therefore needs to:

- keep the domain and application layers independent from frameworks;
- allow the LLM implementation to change without changing application logic;
- support fast and deterministic unit tests;
- avoid designing a large speculative abstraction before concrete capabilities are needed.

## Decision

Communication with a language model is represented by the `LlmGateway` port in the domain layer:

```java
public interface LlmGateway {
    LlmResponse generate(LlmRequest request);
}
```

`LlmRequest` and `LlmResponse` form a provider-independent contract.

`GenerateResponseUseCase` depends only on `LlmGateway`. It contains no Spring annotations or imports.

`SpringAiLlmGateway`, located in the infrastructure layer, implements this port. It encapsulates Spring AI's `ChatClient` and communication with Ollama.

Dependency assembly happens in `AiConfig`. Spring creates the use case and injects the available `LlmGateway` implementation:

```text
API → GenerateResponseUseCase → LlmGateway ← SpringAiLlmGateway → Spring AI → Ollama
                                      ↑
                                FakeLlmGateway
                                   (tests)
```

Unit tests use `FakeLlmGateway`, located in the test sources, to return deterministic responses without an external dependency.

ArchUnit rules automatically verify that the domain and application layers do not depend on Spring or on outer adapters.

## Alternatives Considered

### Use `ChatClient` directly in the use case

This option requires less code initially, but couples the application layer to Spring AI. It makes tests heavier and allows framework changes to propagate into use cases.

This alternative is rejected.

### Define one interface per provider

Interfaces such as `OllamaClient`, `OpenAiClient`, or `AnthropicClient` would expose providers to the application layer and move the coupling instead of removing it.

This alternative is rejected.

### Model every possible LLM capability immediately

An interface that already includes streaming, structured output, tool calling, vision, and embeddings would be speculative and difficult to stabilize.

This alternative is deferred. The contract will remain minimal and evolve from concrete use cases.

### Use mocks only

Mocks can verify specific interactions, but a small fake communicates expected behavior more clearly and can be reused without coupling tests to a mocking library.

Mocks remain acceptable for narrow interaction tests, while a fake is preferred for ordinary use-case tests.

## Positive Consequences

- The domain and application layers do not depend on Spring AI.
- Ollama can be replaced without changing application use cases.
- Unit tests are fast, deterministic, and executable offline.
- Provider configuration and transport details stay in the infrastructure layer.
- ArchUnit detects violations of the dependency boundaries automatically.

## Negative Consequences

- The project contains an additional interface and transport-neutral types.
- Provider-specific features are not directly accessible from application use cases.
- The contract will need to evolve when structured output, streaming, or tool calling is introduced.
- Every new adapter will need to satisfy the same behavioral expectations and contract tests.

## Decision Boundaries

`LlmGateway` is not intended to pretend that all models and providers have identical capabilities.

When a new capability is introduced, the project must decide whether to:

- extend the existing contract;
- introduce a specialized port;
- explicitly model the capabilities supported by each adapter.

Embeddings will use a separate contract because they solve a different application problem from text generation.

## Verification

This decision is considered enforced when:

- `domain` and `application` contain no Spring imports;
- `GenerateResponseUseCase` depends only on `LlmGateway`;
- `SpringAiLlmGateway` remains in `infrastructure`;
- unit tests can use `FakeLlmGateway` without starting Spring;
- the related ArchUnit rules pass during `mvn test`.

## Re-evaluation Triggers

This decision will be reviewed when any of the following capabilities is introduced:

- structured output;
- streaming;
- tool calling;
- a second LLM provider;
- dynamic model or provider selection.

A future decision must not rewrite this ADR. It must be documented in a new ADR that references and supersedes or amends this one.
