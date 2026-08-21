package org.abdel.aiops.domain.llm.exception;

public abstract class LlmGatewayException extends RuntimeException {

    protected LlmGatewayException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
