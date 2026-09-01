package org.abdel.aiops.domain.document;

import java.util.Objects;
import java.util.UUID;

public record DocumentId(UUID value) {

    public DocumentId {
        Objects.requireNonNull(value, "DocumentId is required");
    }

    public static DocumentId generate() {
        return new DocumentId(UUID.randomUUID());
    }
}
