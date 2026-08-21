package org.abdel.aiops.api.incident;

import org.abdel.aiops.application.incident.AnalyzeIncidentUseCase;
import org.abdel.aiops.domain.incident.IncidentAnalysis;
import org.abdel.aiops.domain.incident.Severity;
import org.abdel.aiops.domain.llm.exception.LlmInvalidResponseException;
import org.abdel.aiops.domain.llm.exception.LlmModelUnavailableException;
import org.abdel.aiops.domain.llm.exception.LlmTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyzeIncidentUseCase useCase;

    @Test
    void shouldAnalyzeValidIncident() throws Exception {
        IncidentAnalysis analysis = new IncidentAnalysis(
                "Database connections are exhausted",
                "Connection pool exhaustion",
                Severity.HIGH,
                List.of("Long-running transactions"),
                List.of("Inspect HikariCP metrics"),
                0.85
        );

        when(useCase.execute(
                "payment-service",
                "java.sql.SQLTransientConnectionException: timeout"
        )).thenReturn(analysis);

        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType("application/json")
                        .header("X-Correlation-ID", "incident-api-test")
                        .content("""
                                {
                                  "service": "payment-service",
                                  "stackTrace": "java.sql.SQLTransientConnectionException: timeout"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary")
                        .value("Database connections are exhausted"))
                .andExpect(jsonPath("$.probableCause")
                        .value("Connection pool exhaustion"))
                .andExpect(jsonPath("$.severity")
                        .value("HIGH"))
                .andExpect(header().string(
                        "X-Correlation-ID",
                        "incident-api-test"
                ))
                .andExpect(jsonPath("$.confidence")
                        .value(0.85));
    }


    @Test
    void shouldRejectMissingStackTrace() throws Exception {
        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType("application/json")
                        .content("""
                            {
                              "service": "payment-service"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldRejectOversizedStackTrace() throws Exception {
        String oversizedStackTrace = "a".repeat(50_001);

        String requestBody = """
            {
              "service": "payment-service",
              "stackTrace": "%s"
            }
            """.formatted(oversizedStackTrace);

        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldRejectBlankService() throws Exception {
        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "service": " ",
                              "stackTrace": "java.lang.RuntimeException"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/v1/incidents/analyze"))
                .andExpect(jsonPath("$.errors.service")
                        .value("service is required"));

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldReturnAllInvalidFields() throws Exception {
        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "service": " ",
                              "stackTrace": " "
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.service")
                        .value("service is required"))
                .andExpect(jsonPath("$.errors.stackTrace")
                        .value("stackTrace is required"));

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "service": "payment-service",
                              "stackTrace":
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("Malformed request"))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request body contains invalid JSON"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/v1/incidents/analyze"));

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldReturnGatewayTimeoutWhenLlmTimesOut()
            throws Exception {
        when(useCase.execute(
                "payment-service",
                "java.lang.RuntimeException"
        )).thenThrow(new LlmTimeoutException(
                "LLM request timed out",
                new RuntimeException("read timeout")
        ));

        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "service": "payment-service",
                                  "stackTrace": "java.lang.RuntimeException"
                                }
                                """))
                .andExpect(status().isGatewayTimeout())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("LLM timeout"))
                .andExpect(jsonPath("$.status")
                        .value(504))
                .andExpect(jsonPath("$.detail")
                        .value("The language model did not respond in time"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/v1/incidents/analyze"));
    }

    @Test
    void shouldReturnServiceUnavailableWhenLlmModelIsUnavailable()
            throws Exception {
        when(useCase.execute(
                "payment-service",
                "java.lang.RuntimeException"
        )).thenThrow(new LlmModelUnavailableException(
                "LLM model unavailable",
                new RuntimeException("model not found")
        ));

        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "service": "payment-service",
                                  "stackTrace": "java.lang.RuntimeException"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("LLM unavailable"))
                .andExpect(jsonPath("$.status")
                        .value(503))
                .andExpect(jsonPath("$.detail")
                        .value("The configured language model is unavailable"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/v1/incidents/analyze"));
    }

    @Test
    void shouldReturnBadGatewayWhenLlmResponseIsInvalid()
            throws Exception {
        when(useCase.execute(
                "payment-service",
                "java.lang.RuntimeException"
        )).thenThrow(new LlmInvalidResponseException(
                "LLM response is invalid",
                new IllegalArgumentException("Cannot deserialize response")
        ));

        mockMvc.perform(post("/api/v1/incidents/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "service": "payment-service",
                                  "stackTrace": "java.lang.RuntimeException"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("Invalid LLM response"))
                .andExpect(jsonPath("$.status")
                        .value(502))
                .andExpect(jsonPath("$.detail")
                        .value("The language model returned an invalid response"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/v1/incidents/analyze"));
    }
}
