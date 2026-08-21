package org.abdel.aiops.domain.incident;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class IncidentAnalysisTest {
    @Test
    void shouldRejectConfidenceLowerThanZero() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> createAnalysis(-0.1))
                .withMessage("confidence must be between 0.0 and 1.0");
    }

    @Test
    void shouldRejectConfidenceGreaterThanOne() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> createAnalysis(1.1))
                .withMessage("confidence must be between 0.0 and 1.0");
    }

    @Test
    void shouldRejectBlankSummary() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new IncidentAnalysis(
                        " ",
                        "Connection pool exhaustion",
                        Severity.HIGH,
                        List.of(),
                        List.of("Inspect metrics"),
                        0.8
                ))
                .withMessage("summary is required");
    }

    @Test
    void shouldRejectMissingSeverity() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new IncidentAnalysis(
                        "Database connections are exhausted",
                        "Connection pool exhaustion",
                        null,
                        List.of(),
                        List.of("Inspect metrics"),
                        0.8
                ))
                .withMessage("severity is required");
    }

    @Test
    void shouldProtectHypothesesFromExternalMutation() {
        ArrayList<String> hypotheses =
                new ArrayList<>(List.of("Long-running transactions"));

        IncidentAnalysis analysis = new IncidentAnalysis(
                "Database connections are exhausted",
                "Connection pool exhaustion",
                Severity.HIGH,
                hypotheses,
                List.of("Inspect metrics"),
                0.8
        );

        hypotheses.add("Another hypothesis");

        assertThat(analysis.hypotheses())
                .containsExactly("Long-running transactions");

        assertThatThrownBy(
                () -> analysis.hypotheses().add("Forbidden mutation")
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    private IncidentAnalysis createAnalysis(double confidence) {
        return new IncidentAnalysis(
                "Database connections are exhausted",
                "Connection pool exhaustion",
                Severity.HIGH,
                List.of("Long-running transactions"),
                List.of("Inspect HikariCP metrics"),
                confidence
        );
    }

}