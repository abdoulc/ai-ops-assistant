package org.abdel.aiops.domain.llm;

public interface LlmGateway {
    LlmResponse generate(LlmRequest request);

    <T> T generateStructured(
            LlmRequest request,
            Class<T> responseType
    );

}
