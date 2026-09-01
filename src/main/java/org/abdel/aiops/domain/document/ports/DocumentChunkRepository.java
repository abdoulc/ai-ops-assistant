package org.abdel.aiops.domain.document.ports;

import org.abdel.aiops.domain.document.DocumentChunk;
import org.abdel.aiops.domain.document.DocumentId;

import java.util.List;

public interface DocumentChunkRepository {

    void saveAll(Iterable<DocumentChunk> chunks);

    List<DocumentChunk> findByDocumentId(DocumentId documentId);
}
