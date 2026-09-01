package org.abdel.aiops.infrastructure.document.memory;

import org.abdel.aiops.domain.DomainUtils;
import org.abdel.aiops.domain.document.DocumentChunk;
import org.abdel.aiops.domain.document.DocumentId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDocumentChunkRepositoryTest {

    private final InMemoryDocumentChunkRepository repository =
            new InMemoryDocumentChunkRepository();

    @Test
    void shouldSaveAndFindChunksInIndexOrder() {
        DocumentId documentId = DocumentId.generate();
        DocumentChunk second = chunk(documentId, 1, "second");
        DocumentChunk first = chunk(documentId, 0, "first");

        repository.saveAll(List.of(second, first));

        assertThat(repository.findByDocumentId(documentId))
                .containsExactly(first, second);
    }

    @Test
    void shouldKeepChunksFromDifferentDocumentsSeparated() {
        DocumentId firstDocumentId = DocumentId.generate();
        DocumentId secondDocumentId = DocumentId.generate();
        DocumentChunk first = chunk(firstDocumentId, 0, "same content");
        DocumentChunk second = chunk(secondDocumentId, 0, "same content");

        repository.saveAll(List.of(first, second));

        assertThat(repository.findByDocumentId(firstDocumentId))
                .containsExactly(first);
        assertThat(repository.findByDocumentId(secondDocumentId))
                .containsExactly(second);
    }

    @Test
    void shouldReplaceChunkWithSameDocumentIdAndIndex() {
        DocumentId documentId = DocumentId.generate();
        DocumentChunk original = chunk(documentId, 0, "original");
        DocumentChunk updated = chunk(documentId, 0, "updated");

        repository.saveAll(List.of(original));
        repository.saveAll(List.of(updated));

        assertThat(repository.findByDocumentId(documentId))
                .containsExactly(updated);
    }

    @Test
    void shouldReturnEmptyListForUnknownDocument() {
        assertThat(repository.findByDocumentId(DocumentId.generate()))
                .isEmpty();
    }

    private static DocumentChunk chunk(
            DocumentId documentId,
            int index,
            String content
    ) {
        return new DocumentChunk(
                documentId,
                index,
                content,
                DomainUtils.calculateChecksum(content)
        );
    }
}
