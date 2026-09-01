package org.abdel.aiops.api.document;

import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentType;
import org.abdel.aiops.domain.document.IngestionStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String title,
        DocumentType type,
        String checksum,
        IngestionStatus status,
        Instant createdAt
) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.id().value(),
                document.title(),
                document.type(),
                document.checksum(),
                document.status(),
                document.createdAt()
        );
    }
}
