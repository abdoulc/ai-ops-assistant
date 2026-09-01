package org.abdel.aiops.api.document;

import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentType;
import org.abdel.aiops.domain.document.IngestionStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentDetailsResponse(
        UUID id,
        String title,
        String content,
        DocumentType type,
        String checksum,
        IngestionStatus status,
        Instant createdAt
) {

    public static DocumentDetailsResponse from(Document document) {
        return new DocumentDetailsResponse(
                document.id().value(),
                document.title(),
                document.content(),
                document.type(),
                document.checksum(),
                document.status(),
                document.createdAt()
        );
    }
}
