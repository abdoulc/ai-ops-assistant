package org.abdel.aiops.domain.document;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ChunkingPolicyTest {

    @Test
    void shouldCreateValidPolicy() {
        ChunkingPolicy policy = new ChunkingPolicy(1_000, 200);

        assertThat(policy.chunkSize()).isEqualTo(1_000);
        assertThat(policy.overlapSize()).isEqualTo(200);
    }

    @Test
    void shouldAllowPolicyWithoutOverlap() {
        ChunkingPolicy policy = new ChunkingPolicy(1_000, 0);

        assertThat(policy.overlapSize()).isZero();
    }

    @Test
    void shouldRejectNonPositiveChunkSize() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChunkingPolicy(0, 0))
                .withMessage("chunkSize must be positive");
    }

    @Test
    void shouldRejectNegativeOverlap() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChunkingPolicy(1_000, -1))
                .withMessage("overlapSize must be non-negative");
    }

    @Test
    void shouldRejectOverlapEqualToChunkSize() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChunkingPolicy(1_000, 1_000))
                .withMessage(
                        "overlapSize must be less than chunkSize"
                );
    }

    @Test
    void shouldRejectOverlapGreaterThanChunkSize() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChunkingPolicy(1_000, 1_001))
                .withMessage(
                        "overlapSize must be less than chunkSize"
                );
    }
}
