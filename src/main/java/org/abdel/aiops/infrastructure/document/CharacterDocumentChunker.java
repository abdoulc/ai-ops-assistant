package org.abdel.aiops.infrastructure.document;

import org.abdel.aiops.domain.DomainUtils;
import org.abdel.aiops.domain.document.ChunkingPolicy;
import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentChunk;
import org.abdel.aiops.domain.document.ports.DocumentChunker;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CharacterDocumentChunker implements DocumentChunker {
    @Override
    public List<DocumentChunk> split(Document document, ChunkingPolicy policy) {
        List<DocumentChunk> chunks = new ArrayList<>();
        int step = policy.chunkSize() - policy.overlapSize();

        for (int start = 0, index = 0;
             start < document.content().length();
             start += step, index++) {
            int end = Math.min(
                    start + policy.chunkSize(),
                    document.content().length()
            );

            String content = document.content().substring(start, end);

            chunks.add(new DocumentChunk(
                    document.id(),
                    index,
                    content,
                    DomainUtils.calculateChecksum(content)
            ));

            if (end == document.content().length()) {
                break;
            }
        }
        return chunks;
    }
}
