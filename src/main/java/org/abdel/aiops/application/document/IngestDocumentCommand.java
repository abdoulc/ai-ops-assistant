package org.abdel.aiops.application.document;

import org.abdel.aiops.domain.document.DocumentType;

public record IngestDocumentCommand(
        String title,
        String content,
        DocumentType type
) {
    public IngestDocumentCommand {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
    }
}
