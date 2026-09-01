package org.abdel.aiops.api.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.abdel.aiops.domain.document.DocumentType;

public record IngestDocumentRequest(
        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must not exceed 200 characters")
        String title,

        @NotBlank(message = "content is required")
        @Size(
                max = 1_000_000,
                message = "content must not exceed 1000000 characters"
        )
        String content,

        @NotNull(message = "type is required")
        DocumentType type
) {
}
