package org.abdel.aiops.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiops.ingestion")
public record IngestionProperties(
        int chunkSize,
        int overlapSize
) {
}
