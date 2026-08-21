package org.abdel.aiops.domain.llm;

public interface LlmGateway {
    LlmResponse generate(LlmRequest request);

}
