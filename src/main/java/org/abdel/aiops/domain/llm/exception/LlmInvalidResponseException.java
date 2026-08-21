package org.abdel.aiops.domain.llm.exception;

public final class LlmInvalidResponseException extends LlmGatewayException {

    public LlmInvalidResponseException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
