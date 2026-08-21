package org.abdel.aiops.infrastructure.llm;

import org.abdel.aiops.domain.llm.LlmGateway;
import org.abdel.aiops.domain.llm.LlmRequest;
import org.abdel.aiops.domain.llm.LlmResponse;

public class FakeLlmGateway implements LlmGateway {

    private LlmRequest receivedRequest;
    private Class<?> requestedResponseType;
    private Object structuredResponse;

    @Override
    public LlmResponse generate(LlmRequest request) {
        receivedRequest = request;
        return new LlmResponse("Fake response for prompt: " + request.prompt());
    }

    @Override
    public <T> T generateStructured(LlmRequest request, Class<T> responseType) {
        receivedRequest = request;
        requestedResponseType = responseType;

        if (structuredResponse == null) {
            throw new IllegalStateException(
                    "No structured response configured"
            );
        }

        return responseType.cast(structuredResponse);
    }

    public <T> void givenStructuredResponse(T response) {
        structuredResponse = response;
    }

    public LlmRequest receivedRequest() {
        return receivedRequest;
    }

    public Class<?> requestedResponseType() {
        return requestedResponseType;
    }
}
