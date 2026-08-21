package org.abdel.aiops.domain.llm.exception;

public final class LlmModelUnavailableException extends LlmGatewayException {

    public LlmModelUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
