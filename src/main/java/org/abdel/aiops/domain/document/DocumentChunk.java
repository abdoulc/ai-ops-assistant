package org.abdel.aiops.domain.document;

import org.abdel.aiops.domain.DomainUtils;

public record DocumentChunk(
        DocumentId documentId,
        int index,
        String content,
        String checksum
) {
    public DocumentChunk{
        documentId = DomainUtils.requireValue(
                documentId,
                "documentId"
        );
        content = DomainUtils.requireText(content, "content");

        if (index < 0) {
            throw new IllegalArgumentException(
                    "index must be non-negative"
            );
        }

        DomainUtils.validateChecksum(checksum);

        if (!DomainUtils.checksumMatches(content, checksum)) {
            throw new IllegalArgumentException(
                    "checksum does not match content"
            );
        }
    }
}
