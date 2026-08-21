package org.abdel.aiops.infrastructure.config;

import org.abdel.aiops.application.llm.GenerateResponseUseCase;
import org.abdel.aiops.domain.llm.LlmGateway;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    GenerateResponseUseCase generateResponseUseCase(LlmGateway llmGateway) {
        return new GenerateResponseUseCase(llmGateway);
    }
}
