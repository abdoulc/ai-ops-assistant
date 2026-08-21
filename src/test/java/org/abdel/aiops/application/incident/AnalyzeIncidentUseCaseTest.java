package org.abdel.aiops.application.incident;

import org.abdel.aiops.domain.incident.IncidentAnalysis;
import org.abdel.aiops.domain.incident.Severity;
import org.abdel.aiops.infrastructure.llm.FakeLlmGateway;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzeIncidentUseCaseTest {
    @Test
    void shouldAnalyzeIncidentUsingStructuredLlmResponse() {
        FakeLlmGateway gateway = new FakeLlmGateway();

        IncidentAnalysis expectedAnalysis = new IncidentAnalysis(
                "Database connections are exhausted",
                "Connection pool exhaustion",
                Severity.HIGH,
                List.of("Long-running transactions"),
                List.of("Inspect HikariCP metrics"),
                0.85
        );

        gateway.givenStructuredResponse(expectedAnalysis);

        AnalyzeIncidentUseCase useCase =
                new AnalyzeIncidentUseCase(gateway);

        IncidentAnalysis result = useCase.execute(
                "payment-service",
                "java.sql.SQLTransientConnectionException: timeout"
        );

        assertThat(result).isEqualTo(expectedAnalysis);

        assertThat(gateway.requestedResponseType())
                .isEqualTo(IncidentAnalysis.class);

        assertThat(gateway.receivedRequest().prompt())
                .contains("payment-service")
                .contains(
                        "java.sql.SQLTransientConnectionException: timeout"
                );
    }

}