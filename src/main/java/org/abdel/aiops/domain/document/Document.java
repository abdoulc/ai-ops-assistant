package org.abdel.aiops.domain.document;

import org.abdel.aiops.domain.DomainUtils;

import java.time.Instant;

public record Document(DocumentId id, String title, String content,
                       DocumentType type, String checksum, IngestionStatus status,
                       Instant createdAt) {

    public Document {
        id = DomainUtils.requireValue(id, "id");
        createdAt = DomainUtils.requireValue(createdAt, "createdAt");
        title = DomainUtils.requireText(title, "title");
        content = DomainUtils.requireText(content, "content");
        type = DomainUtils.requireValue(type, "type");
        status = DomainUtils.requireValue(status, "status");
        validateChecksum(content, checksum);
    }

    private static void validateChecksum(
            String content,
            String checksum
    ) {
        DomainUtils.validateChecksum(checksum);

        if (!DomainUtils.checksumMatches(content, checksum)) {
            throw new IllegalArgumentException(
                    "checksum does not match content"
            );
        }
    }
}
