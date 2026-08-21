package org.abdel.aiops.infrastructure.llm;

import org.abdel.aiops.domain.llm.LlmGateway;
import org.abdel.aiops.domain.llm.LlmRequest;
import org.abdel.aiops.domain.llm.LlmResponse;

public class FakeLlmGateway implements LlmGateway {

    private LlmRequest receivedRequest;

    @Override
    public LlmResponse generate(LlmRequest request) {
        receivedRequest = request;
        return new LlmResponse("Fake response for prompt: " + request.prompt());
    }

    public LlmRequest receivedRequest() {
        return receivedRequest;
    }
}
