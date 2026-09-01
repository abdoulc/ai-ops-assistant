package org.abdel.aiops.infrastructure.llm.springai;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.abdel.aiops.domain.incident.IncidentAnalysis;
import org.abdel.aiops.domain.incident.Severity;
import org.abdel.aiops.domain.llm.LlmRequest;
import org.abdel.aiops.domain.llm.LlmResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.ai.retry.max-attempts=1",
        "spring.ai.ollama.chat.model=test-model",
        "spring.ai.ollama.chat.think=false",
        "spring.flyway.enabled=false"
})
class SpringAiLlmGatewayIntegrationTest {

    private static final MockWebServer OLLAMA =
            new MockWebServer();
    @Autowired
    private SpringAiLlmGateway gateway;

    static {
        try {
            OLLAMA.start();
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @DynamicPropertySource
    static void configureOllama(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.ai.ollama.base-url",
                () -> OLLAMA.url("/").toString()
        );
    }

    @AfterAll
    static void stopServer() throws IOException {
        OLLAMA.shutdown();
    }

    @Test
    void shouldGenerateResponseThroughRealSpringAiClient()
            throws InterruptedException {

        OLLAMA.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "model": "test-model",
                      "created_at": "2026-08-21T12:00:00Z",
                      "message": {
                        "role": "assistant",
                        "content": "Integration OK"
                      },
                      "done": true,
                      "done_reason": "stop",
                      "total_duration": 1000,
                      "load_duration": 100,
                      "prompt_eval_count": 5,
                      "prompt_eval_duration": 100,
                      "eval_count": 2,
                      "eval_duration": 200
                    }
                    """));

        LlmResponse response = gateway.generate(
                new LlmRequest("Reply with Integration OK")
        );

        assertThat(response.content())
                .isEqualTo("Integration OK");

        RecordedRequest recordedRequest =
                OLLAMA.takeRequest();

        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getMethod())
                .isEqualTo("POST");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/api/chat");

        assertThat(recordedRequest.getBody().readUtf8())
                .contains("\"model\":\"test-model\"")
                .contains("Reply with Integration OK");
    }

    @Test
    void shouldGenerateStructuredResponseThroughRealSpringAiClient()
            throws InterruptedException {
        String structuredContent = """
                {
                  "summary": "Database connections are exhausted",
                  "probableCause": "Connection pool exhaustion",
                  "severity": "HIGH",
                  "hypotheses": ["Long-running transactions"],
                  "recommendations": ["Inspect HikariCP metrics"],
                  "confidence": 0.85
                }
                """;

        OLLAMA.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(ollamaResponse(structuredContent)));

        IncidentAnalysis analysis = gateway.generateStructured(
                new LlmRequest("Analyze this incident"),
                IncidentAnalysis.class
        );

        assertThat(analysis.summary())
                .isEqualTo("Database connections are exhausted");
        assertThat(analysis.probableCause())
                .isEqualTo("Connection pool exhaustion");
        assertThat(analysis.severity())
                .isEqualTo(Severity.HIGH);
        assertThat(analysis.hypotheses())
                .containsExactly("Long-running transactions");
        assertThat(analysis.recommendations())
                .containsExactly("Inspect HikariCP metrics");
        assertThat(analysis.confidence())
                .isEqualTo(0.85);

        RecordedRequest recordedRequest = OLLAMA.takeRequest();
        String requestBody = recordedRequest.getBody().readUtf8();

        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/chat");
        assertThat(requestBody)
                .contains("\"model\":\"test-model\"")
                .contains("Your response should be in JSON format")
                .contains("probableCause")
                .contains("Analyze this incident")
                .contains("\"think\":false");
    }

    private static String ollamaResponse(String content) {
        String escapedContent = content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");

        return """
                {
                  "model": "test-model",
                  "created_at": "2026-08-21T12:00:00Z",
                  "message": {
                    "role": "assistant",
                    "content": "%s"
                  },
                  "done": true,
                  "done_reason": "stop",
                  "total_duration": 1000,
                  "load_duration": 100,
                  "prompt_eval_count": 5,
                  "prompt_eval_duration": 100,
                  "eval_count": 2,
                  "eval_duration": 200
                }
                """.formatted(escapedContent);
    }
}
