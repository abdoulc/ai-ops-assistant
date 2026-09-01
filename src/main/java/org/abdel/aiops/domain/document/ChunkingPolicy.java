package org.abdel.aiops.domain.document;

public record ChunkingPolicy(
        int chunkSize,
        int overlapSize
) {
    public ChunkingPolicy {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "chunkSize must be positive"
            );
        }
        if (overlapSize < 0) {
            throw new IllegalArgumentException(
                    "overlapSize must be non-negative"
            );
        }
        if (overlapSize >= chunkSize) {
            throw new IllegalArgumentException(
                    "overlapSize must be less than chunkSize"
            );
        }
    }
}
