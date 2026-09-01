package org.abdel.aiops.application.document;

import org.abdel.aiops.domain.DomainUtils;
import org.abdel.aiops.domain.document.*;
import org.abdel.aiops.domain.document.ports.DocumentChunkRepository;
import org.abdel.aiops.domain.document.ports.DocumentChunker;
import org.abdel.aiops.domain.document.ports.DocumentRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class IngestDocumentUseCase {
    private final DocumentRepository documentRepository;
    private final DocumentChunker documentChunker;
    private final DocumentChunkRepository documentChunkRepository;
    private final ChunkingPolicy chunkingPolicy;
    private final Clock clock;

    public IngestDocumentUseCase(
            DocumentRepository documentRepository,
            DocumentChunker documentChunker,
            DocumentChunkRepository documentChunkRepository,
            ChunkingPolicy chunkingPolicy,
            Clock clock
    ) {
        this.documentRepository = documentRepository;
        this.documentChunker = documentChunker;
        this.documentChunkRepository = documentChunkRepository;
        this.chunkingPolicy = chunkingPolicy;
        this.clock = clock;
    }

    public Document execute(IngestDocumentCommand ingestDocumentCommand) {
        String checksum = DomainUtils.calculateChecksum(ingestDocumentCommand.content());
        if (documentRepository.existsByChecksum(checksum)) {
            throw new DocumentAlreadyExistsException(checksum);
        }
        Document document = new Document(
                DocumentId.generate(),
                ingestDocumentCommand.title(),
                ingestDocumentCommand.content(),
                ingestDocumentCommand.type(),
                checksum,
                IngestionStatus.PENDING,
                Instant.now(clock)
        );
        List<DocumentChunk> chunks = documentChunker.split(
                document,
                chunkingPolicy
        );
        Document savedDocument = documentRepository.save(document);
        documentChunkRepository.saveAll(chunks);
        return savedDocument;
    }
}
