package org.abdel.aiops.application.document;

import org.abdel.aiops.domain.DomainUtils;
import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.DocumentNotFoundException;
import org.abdel.aiops.domain.document.ports.DocumentRepository;
import org.abdel.aiops.domain.document.DocumentType;
import org.abdel.aiops.domain.document.IngestionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class GetDocumentUseCaseTest {

    private static final DocumentId ID = new DocumentId(UUID.fromString(
            "3b43b952-186f-4b6d-8b88-d78e89fe52fd"
    ));

    @Test
    void shouldReturnExistingDocument() {
        Document document = document();
        GetDocumentUseCase useCase = new GetDocumentUseCase(
                new StubDocumentRepository(document)
        );

        assertThat(useCase.execute(ID)).isEqualTo(document);
    }

    @Test
    void shouldRejectUnknownDocument() {
        GetDocumentUseCase useCase = new GetDocumentUseCase(
                new StubDocumentRepository(null)
        );

        assertThatExceptionOfType(DocumentNotFoundException.class)
                .isThrownBy(() -> useCase.execute(ID))
                .withMessageContaining(ID.value().toString());
    }

    private static Document document() {
        String content = "# Restart the payment service";
        return new Document(
                ID,
                "Payment runbook",
                content,
                DocumentType.MARKDOWN,
                DomainUtils.calculateChecksum(content),
                IngestionStatus.PENDING,
                Instant.parse("2026-08-22T20:00:00Z")
        );
    }

    private record StubDocumentRepository(Document document)
            implements DocumentRepository {

        @Override
        public Document save(Document document) {
            return document;
        }

        @Override
        public Optional<Document> findById(DocumentId id) {
            return Optional.ofNullable(document)
                    .filter(existing -> existing.id().equals(id));
        }

        @Override
        public Optional<Document> findByChecksum(String checksum) {
            return Optional.empty();
        }

        @Override
        public boolean existsByChecksum(String checksum) {
            return false;
        }
    }
}
