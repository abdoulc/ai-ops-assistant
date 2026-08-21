package org.abdel.aiops.infrastructure.llm.springai;

import org.abdel.aiops.domain.llm.LlmGateway;
import org.abdel.aiops.domain.llm.LlmRequest;
import org.abdel.aiops.domain.llm.LlmResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAiLlmGateway implements LlmGateway {
    private final ChatClient chatClient;

    public SpringAiLlmGateway(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        String response = chatClient
                .prompt()
                .user( request.prompt())
                .call()
                .content();

        return new LlmResponse(response);
    }
}
