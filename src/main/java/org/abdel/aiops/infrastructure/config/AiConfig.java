package org.abdel.aiops.infrastructure.config;

import org.abdel.aiops.application.document.GetDocumentUseCase;
import org.abdel.aiops.application.document.IngestDocumentUseCase;
import org.abdel.aiops.application.incident.AnalyzeIncidentUseCase;
import org.abdel.aiops.application.llm.GenerateResponseUseCase;
import org.abdel.aiops.domain.document.ChunkingPolicy;
import org.abdel.aiops.domain.document.ports.DocumentChunkRepository;
import org.abdel.aiops.domain.document.ports.DocumentChunker;
import org.abdel.aiops.domain.document.ports.DocumentRepository;
import org.abdel.aiops.domain.llm.LlmGateway;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(IngestionProperties.class)
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    GenerateResponseUseCase generateResponseUseCase(LlmGateway llmGateway) {
        return new GenerateResponseUseCase(llmGateway);
    }

    @Bean
    AnalyzeIncidentUseCase analyzeIncidentUseCase(LlmGateway llmGateway) {
        return new AnalyzeIncidentUseCase(llmGateway);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    ChunkingPolicy chunkingPolicy(
            IngestionProperties properties
    ) {
        return new ChunkingPolicy(
                properties.chunkSize(),
                properties.overlapSize()
        );
    }

    @Bean
    IngestDocumentUseCase ingestDocumentUseCase(
            DocumentRepository documentRepository,
            DocumentChunker documentChunker,
            DocumentChunkRepository documentChunkRepository,
            ChunkingPolicy chunkingPolicy,
            Clock clock
    ) {
        return new IngestDocumentUseCase(
                documentRepository,
                documentChunker,
                documentChunkRepository,
                chunkingPolicy,
                clock
        );
    }

    @Bean
    GetDocumentUseCase getDocumentUseCase(
            DocumentRepository documentRepository
    ) {
        return new GetDocumentUseCase(documentRepository);
    }
}
