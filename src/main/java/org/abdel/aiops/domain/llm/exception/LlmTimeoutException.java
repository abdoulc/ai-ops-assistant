package org.abdel.aiops.domain.llm.exception;

public final class LlmTimeoutException extends LlmGatewayException {

    public LlmTimeoutException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
