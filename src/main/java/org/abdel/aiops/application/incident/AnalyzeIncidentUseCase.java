package org.abdel.aiops.application.incident;

import org.abdel.aiops.domain.incident.IncidentAnalysis;
import org.abdel.aiops.domain.llm.LlmGateway;
import org.abdel.aiops.domain.llm.LlmRequest;

public class AnalyzeIncidentUseCase {



    private final LlmGateway llmGateway;
    public AnalyzeIncidentUseCase(LlmGateway llmGateway){

        this.llmGateway = llmGateway;
    }

    public IncidentAnalysis execute( String service,
                                     String stackTrace) {
        String prompt = IncidentAnalysisPrompt.render(
                service,
                stackTrace
        );
        return llmGateway.generateStructured(
                new LlmRequest(prompt),
                IncidentAnalysis.class
        );
    }
}
