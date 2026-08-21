package org.abdel.aiops.infrastructure.llm.springai;

import io.netty.handler.timeout.ReadTimeoutException;
import org.abdel.aiops.domain.llm.LlmRequest;
import org.abdel.aiops.domain.llm.exception.LlmInvalidResponseException;
import org.abdel.aiops.domain.llm.exception.LlmModelUnavailableException;
import org.abdel.aiops.domain.llm.exception.LlmTimeoutException;
import org.abdel.aiops.domain.incident.IncidentAnalysis;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringAiLlmGatewayTest {

    @Test
    void shouldTranslateNetworkTimeout() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ResourceAccessException timeout = new ResourceAccessException(
                "I/O error while calling Ollama",
                new SocketTimeoutException("Read timed out")
        );

        when(chatClient.prompt()
                .user(anyString())
                .call()
                .content())
                .thenThrow(timeout);

        SpringAiLlmGateway gateway = new SpringAiLlmGateway(chatClient);
        LlmRequest request = new LlmRequest("Analyze this incident");

        assertThatThrownBy(() -> gateway.generate(request))
                .isInstanceOf(LlmTimeoutException.class)
                .hasMessage("LLM request timed out")
                .hasCause(timeout);
    }

    @Test
    void shouldTranslateReactorNettyReadTimeout() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ReadTimeoutException timeout = ReadTimeoutException.INSTANCE;

        when(chatClient.prompt()
                .user(anyString())
                .call()
                .content())
                .thenThrow(timeout);

        SpringAiLlmGateway gateway = new SpringAiLlmGateway(chatClient);
        LlmRequest request = new LlmRequest("Analyze this incident");

        assertThatThrownBy(() -> gateway.generate(request))
                .isInstanceOf(LlmTimeoutException.class)
                .hasMessage("LLM request timed out")
                .hasCause(timeout);
    }

    @Test
    void shouldTranslateStructuredGenerationTimeout() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ResourceAccessException timeout = new ResourceAccessException(
                "I/O error while calling Ollama",
                new SocketTimeoutException("Read timed out")
        );

        when(chatClient.prompt()
                .user(anyString())
                .call()
                .entity(IncidentAnalysis.class))
                .thenThrow(timeout);

        SpringAiLlmGateway gateway = new SpringAiLlmGateway(chatClient);
        LlmRequest request = new LlmRequest("Analyze this incident");

        assertThatThrownBy(() -> gateway.generateStructured(
                request,
                IncidentAnalysis.class
        ))
                .isInstanceOf(LlmTimeoutException.class)
                .hasMessage("LLM request timed out")
                .hasCause(timeout);
    }

    @Test
    void shouldNotTranslateNonTimeoutException() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        RuntimeException providerFailure = new RuntimeException(
                "Invalid structured response"
        );

        when(chatClient.prompt()
                .user(anyString())
                .call()
                .content())
                .thenThrow(providerFailure);

        SpringAiLlmGateway gateway = new SpringAiLlmGateway(chatClient);
        LlmRequest request = new LlmRequest("Analyze this incident");

        assertThatThrownBy(() -> gateway.generate(request))
                .isSameAs(providerFailure);
    }

    @Test
    void shouldTranslateMissingModelError() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        NonTransientAiException missingModel = new NonTransientAiException(
                "HTTP 404 - {\"error\":\"model 'qwen3:4b' not found\"}"
        );

        when(chatClient.prompt()
                .user(anyString())
                .call()
                .content())
                .thenThrow(missingModel);

        SpringAiLlmGateway gateway = new SpringAiLlmGateway(chatClient);
        LlmRequest request = new LlmRequest("Analyze this incident");

        assertThatThrownBy(() -> gateway.generate(request))
                .isInstanceOf(LlmModelUnavailableException.class)
                .hasMessage("LLM model unavailable")
                .hasCause(missingModel);
    }

    @Test
    void shouldTranslateInvalidStructuredResponse() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        IllegalStateException conversionFailure = new IllegalStateException(
                "Could not deserialize the model output"
        );

        when(chatClient.prompt()
                .user(anyString())
                .call()
                .entity(IncidentAnalysis.class))
                .thenThrow(conversionFailure);

        SpringAiLlmGateway gateway = new SpringAiLlmGateway(chatClient);
        LlmRequest request = new LlmRequest("Analyze this incident");

        assertThatThrownBy(() -> gateway.generateStructured(
                request,
                IncidentAnalysis.class
        ))
                .isInstanceOf(LlmInvalidResponseException.class)
                .hasMessage("LLM response is invalid")
                .hasCause(conversionFailure);
    }
}
