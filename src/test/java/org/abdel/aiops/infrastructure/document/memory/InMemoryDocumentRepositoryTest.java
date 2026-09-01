package org.abdel.aiops.infrastructure.document.memory;

import org.abdel.aiops.domain.DomainUtils;
import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.DocumentType;
import org.abdel.aiops.domain.document.IngestionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDocumentRepositoryTest {

    private static final DocumentId DOCUMENT_ID = new DocumentId(
            UUID.fromString("3b43b952-186f-4b6d-8b88-d78e89fe52fd")
    );
    private static final Instant CREATED_AT =
            Instant.parse("2026-08-23T10:00:00Z");

    private InMemoryDocumentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDocumentRepository();
    }

    @Test
    void shouldSaveAndFindDocumentById() {
        Document document = document(
                "# Restart the payment service",
                IngestionStatus.PENDING
        );

        Document savedDocument = repository.save(document);

        assertThat(savedDocument).isSameAs(document);
        assertThat(repository.findById(DOCUMENT_ID)).contains(document);
    }

    @Test
    void shouldFindDocumentByChecksum() {
        Document document = document(
                "# Restart the payment service",
                IngestionStatus.PENDING
        );
        repository.save(document);

        assertThat(repository.findByChecksum(document.checksum()))
                .contains(document);
        assertThat(repository.existsByChecksum(document.checksum()))
                .isTrue();
    }

    @Test
    void shouldReturnEmptyForUnknownDocumentAndChecksum() {
        DocumentId unknownId = DocumentId.generate();

        assertThat(repository.findById(unknownId)).isEmpty();
        assertThat(repository.findByChecksum(
                DomainUtils.calculateChecksum("unknown content")
        )).isEmpty();
        assertThat(repository.existsByChecksum(
                DomainUtils.calculateChecksum("unknown content")
        )).isFalse();
    }

    @Test
    void shouldReplaceDocumentWithSameId() {
        Document original = document(
                "# Original procedure",
                IngestionStatus.PENDING
        );
        Document updated = document(
                "# Updated procedure",
                IngestionStatus.COMPLETED
        );

        repository.save(original);
        repository.save(updated);

        assertThat(repository.findById(DOCUMENT_ID)).contains(updated);
        assertThat(repository.existsByChecksum(original.checksum()))
                .isFalse();
        assertThat(repository.existsByChecksum(updated.checksum()))
                .isTrue();
    }

    private static Document document(
            String content,
            IngestionStatus status
    ) {
        return new Document(
                DOCUMENT_ID,
                "Payment runbook",
                content,
                DocumentType.MARKDOWN,
                DomainUtils.calculateChecksum(content),
                status,
                CREATED_AT
        );
    }
}
