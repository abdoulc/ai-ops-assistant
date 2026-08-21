package org.abdel.aiops.application.llm;

import org.abdel.aiops.domain.llm.LlmResponse;
import org.abdel.aiops.infrastructure.llm.FakeLlmGateway;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateResponseUseCaseTest {

    @Test
    void shouldSendPromptToGatewayAndReturnItsResponse() {
        FakeLlmGateway gateway = new FakeLlmGateway();
        GenerateResponseUseCase useCase = new GenerateResponseUseCase(gateway);

        LlmResponse response = useCase.execute("Pourquoi le service est lent ?");

        assertThat(gateway.receivedRequest().prompt())
                .isEqualTo("Pourquoi le service est lent ?");
        assertThat(response.content())
                .isEqualTo("Fake response for prompt: Pourquoi le service est lent ?");
    }
}
