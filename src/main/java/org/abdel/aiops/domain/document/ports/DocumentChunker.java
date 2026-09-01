package org.abdel.aiops.domain.document.ports;

import org.abdel.aiops.domain.document.ChunkingPolicy;
import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentChunk;

import java.util.List;

public interface DocumentChunker {

    List<DocumentChunk> split(
            Document document,
            ChunkingPolicy policy
    );
}