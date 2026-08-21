package org.abdel.aiops.application.llm;

import org.abdel.aiops.domain.llm.LlmGateway;
import org.abdel.aiops.domain.llm.LlmRequest;
import org.abdel.aiops.domain.llm.LlmResponse;


public class GenerateResponseUseCase {

    private final LlmGateway llmGateway;

    public GenerateResponseUseCase(LlmGateway llmGateway) {
        this.llmGateway = llmGateway;
    }

    public LlmResponse execute(String prompt) {
        return llmGateway.generate(
                new LlmRequest(prompt)
        );
    }
}
