package org.abdel.aiops.domain.incident;

import java.util.List;

public record IncidentAnalysis(String summary,
                               String probableCause,
                               Severity severity,
                               List<String> hypotheses,
                               List<String> recommendations,
                               double confidence) {
    public IncidentAnalysis {
        summary = requireText(summary, "summary");
        probableCause = requireText(probableCause, "probableCause");
        severity = requireValue(severity, "severity");
        hypotheses = immutableList(hypotheses, "hypotheses");
        recommendations = immutableList(
                recommendations,
                "recommendations"
        );

        if (!Double.isFinite(confidence)
                || confidence < 0.0
                || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be between 0.0 and 1.0"
            );
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required"
            );
        }

        return value;
    }

    private static <T> T requireValue(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(
                    field + " is required"
            );
        }

        return value;
    }

    private static <T> List<T> immutableList(
            List<T> values,
            String field
    ) {
        requireValue(values, field);
        return List.copyOf(values);
    }
}
