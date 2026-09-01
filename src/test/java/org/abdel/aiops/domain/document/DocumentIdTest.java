package org.abdel.aiops.domain.document;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DocumentIdTest {

    @Test
    void shouldWrapUuid() {
        UUID value = UUID.fromString(
                "9ddf9074-8d8b-4f93-a632-8971bf86f043"
        );

        assertThat(new DocumentId(value).value()).isEqualTo(value);
    }

    @Test
    void shouldGenerateDocumentId() {
        DocumentId first = DocumentId.generate();
        DocumentId second = DocumentId.generate();

        assertThat(first.value()).isNotNull();
        assertThat(second.value()).isNotNull();
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldRejectMissingUuid() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DocumentId(null))
                .withMessage("DocumentId is required");
    }
}
