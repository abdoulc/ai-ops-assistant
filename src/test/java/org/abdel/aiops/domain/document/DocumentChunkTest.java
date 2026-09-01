package org.abdel.aiops.domain.document;

import org.abdel.aiops.domain.DomainUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DocumentChunkTest {

    private static final DocumentId DOCUMENT_ID = DocumentId.generate();
    private static final String CONTENT = "Restart the payment service";
    private static final String CHECKSUM =
            DomainUtils.calculateChecksum(CONTENT);

    @Test
    void shouldCreateValidChunk() {
        DocumentChunk chunk = new DocumentChunk(
                DOCUMENT_ID,
                0,
                CONTENT,
                CHECKSUM
        );

        assertThat(chunk.documentId()).isEqualTo(DOCUMENT_ID);
        assertThat(chunk.index()).isZero();
        assertThat(chunk.content()).isEqualTo(CONTENT);
        assertThat(chunk.checksum()).isEqualTo(CHECKSUM);
    }

    @Test
    void shouldRejectMissingDocumentId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DocumentChunk(
                        null,
                        0,
                        CONTENT,
                        CHECKSUM
                ))
                .withMessage("documentId is required");
    }

    @Test
    void shouldRejectNegativeIndex() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DocumentChunk(
                        DOCUMENT_ID,
                        -1,
                        CONTENT,
                        CHECKSUM
                ))
                .withMessage("index must be non-negative");
    }

    @Test
    void shouldRejectBlankContent() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DocumentChunk(
                        DOCUMENT_ID,
                        0,
                        " ",
                        CHECKSUM
                ))
                .withMessage("content is required");
    }

    @Test
    void shouldRejectInvalidChecksumFormat() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DocumentChunk(
                        DOCUMENT_ID,
                        0,
                        CONTENT,
                        "not-a-sha-256"
                ))
                .withMessage(
                        "checksum must be a lowercase SHA-256 value"
                );
    }

    @Test
    void shouldRejectChecksumThatDoesNotMatchContent() {
        String checksumForDifferentContent =
                DomainUtils.calculateChecksum("different content");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DocumentChunk(
                        DOCUMENT_ID,
                        0,
                        CONTENT,
                        checksumForDifferentContent
                ))
                .withMessage("checksum does not match content");
    }
}
