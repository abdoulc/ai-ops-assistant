package org.abdel.aiops.infrastructure.llm.springai;

import io.netty.handler.timeout.ReadTimeoutException;
import org.abdel.aiops.domain.llm.LlmGateway;
import org.abdel.aiops.domain.llm.LlmRequest;
import org.abdel.aiops.domain.llm.LlmResponse;
import org.abdel.aiops.domain.llm.exception.LlmGatewayException;
import org.abdel.aiops.domain.llm.exception.LlmInvalidResponseException;
import org.abdel.aiops.domain.llm.exception.LlmModelUnavailableException;
import org.abdel.aiops.domain.llm.exception.LlmTimeoutException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.function.Supplier;

@Service
public class SpringAiLlmGateway implements LlmGateway {
    private final ChatClient chatClient;

    public SpringAiLlmGateway(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        return executeSafely(()->{
            String response = chatClient
                    .prompt()
                    .user( request.prompt())
                    .call()
                    .content();
            return new LlmResponse(response);
        });

    }


    @Override
    public <T> T generateStructured(LlmRequest request, Class<T> responseType) {
        return executeStructuredSafely(() -> chatClient
                    .prompt()
                    .user(request.prompt())
                    .call()
                    .entity(responseType));
    }

    private <T> T executeStructuredSafely(Supplier<T> operation) {
        try {
            return executeSafely(operation);
        } catch (LlmGatewayException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new LlmInvalidResponseException(
                    "LLM response is invalid",
                    exception
            );
        }
    }

    private <T> T executeSafely(Supplier<T> operation){
        try {
            return operation.get();
        } catch (RuntimeException exception) {
            if (isTimeout(exception)) {
                throw new LlmTimeoutException(
                        "LLM request timed out",
                        exception
                );
            }
            if (isModelUnavailable(exception)) {
                throw new LlmModelUnavailableException(
                        "LLM model unavailable",
                        exception
                );
            }
            throw exception;
        }
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof ReadTimeoutException) {
                return true;
            }
            current = current.getCause();
        }

        return false;
    }
    private boolean isModelUnavailable(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            String message = current.getMessage();

            if (current instanceof NonTransientAiException
                    && message != null
                    && message.contains("HTTP 404")
                    && message.toLowerCase(Locale.ROOT).contains("model")
                    && message.toLowerCase(Locale.ROOT).contains("not found")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
