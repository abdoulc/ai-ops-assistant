package org.abdel.aiops.infrastructure.document.memory;

import org.abdel.aiops.domain.document.DocumentChunk;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.ports.DocumentChunkRepository;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("memory")
public class InMemoryDocumentChunkRepository implements DocumentChunkRepository {

    private final Map<ChunkKey, DocumentChunk> chunkStore =
            new ConcurrentHashMap<>();

    @Override
    public void saveAll(Iterable<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            chunkStore.put(
                    new ChunkKey(chunk.documentId(), chunk.index()),
                    chunk
            );
        }
    }

    @Override
    public List<DocumentChunk> findByDocumentId(DocumentId documentId) {
        return chunkStore.values().stream()
                .filter(chunk -> chunk.documentId().equals(documentId))
                .sorted((left, right) ->
                        Integer.compare(left.index(), right.index())
                )
                .toList();
    }

    private record ChunkKey(DocumentId documentId, int index) {
    }
}
