package org.abdel.aiops.application.document;

import org.abdel.aiops.domain.DomainUtils;
import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentAlreadyExistsException;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.ChunkingPolicy;
import org.abdel.aiops.domain.document.ports.DocumentRepository;
import org.abdel.aiops.domain.document.DocumentType;
import org.abdel.aiops.domain.document.IngestionStatus;
import org.abdel.aiops.domain.document.ports.DocumentChunkRepository;
import org.abdel.aiops.domain.document.ports.DocumentChunker;
import org.abdel.aiops.infrastructure.document.CharacterDocumentChunker;
import org.abdel.aiops.infrastructure.document.memory.InMemoryDocumentChunkRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class IngestDocumentUseCaseTest {

    private static final String CONTENT = "# Restart the payment service";
    private static final Instant FIXED_TIME =
            Instant.parse("2026-08-22T20:00:00Z");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(FIXED_TIME, ZoneOffset.UTC);
    private static final ChunkingPolicy CHUNKING_POLICY =
            new ChunkingPolicy(100, 2);
    private final DocumentChunker documentChunker =
            new CharacterDocumentChunker();
    private final DocumentChunkRepository documentChunkRepository =
            new InMemoryDocumentChunkRepository();

    @Test
    void shouldCreateAndSavePendingDocument() {
        InMemoryDocumentRepository repository =
                new InMemoryDocumentRepository();
        IngestDocumentUseCase useCase =
                new IngestDocumentUseCase(
                        repository,
                        documentChunker,
                        documentChunkRepository,
                        CHUNKING_POLICY,
                        FIXED_CLOCK
                );

        Document result = useCase.execute(new IngestDocumentCommand(
                "Payment service runbook",
                CONTENT,
                DocumentType.MARKDOWN
        ));

        assertThat(result.id()).isNotNull();
        assertThat(result.title()).isEqualTo("Payment service runbook");
        assertThat(result.content()).isEqualTo(CONTENT);
        assertThat(result.type()).isEqualTo(DocumentType.MARKDOWN);
        assertThat(result.checksum())
                .isEqualTo(DomainUtils.calculateChecksum(CONTENT));
        assertThat(result.status()).isEqualTo(IngestionStatus.PENDING);
        assertThat(result.createdAt()).isEqualTo(FIXED_TIME);
        assertThat(repository.findById(result.id())).contains(result);
        assertThat(documentChunkRepository.findByDocumentId(result.id()))
                .hasSize(1)
                .allSatisfy(chunk ->
                        assertThat(chunk.documentId()).isEqualTo(result.id())
                );
    }

    @Test
    void shouldReturnDocumentSavedByRepository() {
        InMemoryDocumentRepository repository =
                new InMemoryDocumentRepository();
        IngestDocumentUseCase useCase =
                new IngestDocumentUseCase(
                        repository,
                        documentChunker,
                        documentChunkRepository,
                        CHUNKING_POLICY,
                        FIXED_CLOCK
                );

        Document result = useCase.execute(new IngestDocumentCommand(
                "Plain-text procedure",
                CONTENT,
                DocumentType.PLAIN_TEXT
        ));

        assertThat(repository.lastSavedDocument()).isSameAs(result);
    }

    @Test
    void shouldRejectContentThatWasAlreadyIngested() {
        InMemoryDocumentRepository repository =
                new InMemoryDocumentRepository();
        IngestDocumentUseCase useCase =
                new IngestDocumentUseCase(
                        repository,
                        documentChunker,
                        documentChunkRepository,
                        CHUNKING_POLICY,
                        FIXED_CLOCK
                );
        IngestDocumentCommand command = new IngestDocumentCommand(
                "Payment service runbook",
                CONTENT,
                DocumentType.MARKDOWN
        );

        useCase.execute(command);

        assertThatExceptionOfType(DocumentAlreadyExistsException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessageContaining(
                        DomainUtils.calculateChecksum(CONTENT)
                );
        assertThat(repository.size()).isEqualTo(1);
    }

    private static final class InMemoryDocumentRepository
            implements DocumentRepository {

        private final Map<DocumentId, Document> documents =
                new HashMap<>();
        private Document lastSavedDocument;

        @Override
        public Document save(Document document) {
            documents.put(document.id(), document);
            lastSavedDocument = document;
            return document;
        }

        @Override
        public Optional<Document> findById(DocumentId id) {
            return Optional.ofNullable(documents.get(id));
        }

        @Override
        public Optional<Document> findByChecksum(String checksum) {
            return documents.values().stream()
                    .filter(document -> document.checksum().equals(checksum))
                    .findFirst();
        }

        @Override
        public boolean existsByChecksum(String checksum) {
            return findByChecksum(checksum).isPresent();
        }

        private Document lastSavedDocument() {
            return lastSavedDocument;
        }

        private int size() {
            return documents.size();
        }
    }
}
