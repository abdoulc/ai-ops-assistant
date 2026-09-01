package org.abdel.aiops.domain.document;

import org.abdel.aiops.domain.DomainUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DocumentTest {

    private static final String CONTENT = "# Restarting the payment service";
    private static final String CHECKSUM =
            DomainUtils.calculateChecksum(CONTENT);
    private static final Instant CREATED_AT =
            Instant.parse("2026-08-21T12:00:00Z");

    @Test
    void shouldCreateValidDocument() {
        DocumentId id = DocumentId.generate();

        Document document = new Document(
                id,
                "Payment service runbook",
                CONTENT,
                DocumentType.MARKDOWN,
                CHECKSUM,
                IngestionStatus.PENDING,
                CREATED_AT
        );

        assertThat(document.id()).isEqualTo(id);
        assertThat(document.title()).isEqualTo("Payment service runbook");
        assertThat(document.content()).isEqualTo(CONTENT);
        assertThat(document.type()).isEqualTo(DocumentType.MARKDOWN);
        assertThat(document.checksum()).isEqualTo(CHECKSUM);
        assertThat(document.status()).isEqualTo(IngestionStatus.PENDING);
        assertThat(document.createdAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldRejectMissingId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validDocument(null, "title", CONTENT, CHECKSUM))
                .withMessage("id is required");
    }

    @Test
    void shouldRejectMissingCreationDate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document(
                        DocumentId.generate(),
                        "title",
                        CONTENT,
                        DocumentType.MARKDOWN,
                        CHECKSUM,
                        IngestionStatus.PENDING,
                        null
                ))
                .withMessage("createdAt is required");
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validDocument(
                        DocumentId.generate(),
                        " ",
                        CONTENT,
                        CHECKSUM
                ))
                .withMessage("title is required");
    }

    @Test
    void shouldRejectBlankContent() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validDocument(
                        DocumentId.generate(),
                        "title",
                        " ",
                        CHECKSUM
                ))
                .withMessage("content is required");
    }

    @Test
    void shouldRejectMissingType() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document(
                        DocumentId.generate(),
                        "title",
                        CONTENT,
                        null,
                        CHECKSUM,
                        IngestionStatus.PENDING,
                        CREATED_AT
                ))
                .withMessage("type is required");
    }

    @Test
    void shouldRejectMissingStatus() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document(
                        DocumentId.generate(),
                        "title",
                        CONTENT,
                        DocumentType.PLAIN_TEXT,
                        CHECKSUM,
                        null,
                        CREATED_AT
                ))
                .withMessage("status is required");
    }

    @Test
    void shouldRejectInvalidChecksumFormat() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validDocument(
                        DocumentId.generate(),
                        "title",
                        CONTENT,
                        "not-a-sha-256"
                ))
                .withMessage("checksum must be a lowercase SHA-256 value");
    }

    @Test
    void shouldRejectChecksumThatDoesNotMatchContent() {
        String checksumForDifferentContent =
                DomainUtils.calculateChecksum("different content");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> validDocument(
                        DocumentId.generate(),
                        "title",
                        CONTENT,
                        checksumForDifferentContent
                ))
                .withMessage("checksum does not match content");
    }

    private static Document validDocument(
            DocumentId id,
            String title,
            String content,
            String checksum
    ) {
        return new Document(
                id,
                title,
                content,
                DocumentType.MARKDOWN,
                checksum,
                IngestionStatus.PENDING,
                CREATED_AT
        );
    }
}
