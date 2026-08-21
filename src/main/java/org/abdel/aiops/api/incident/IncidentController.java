package org.abdel.aiops.api.incident;

import jakarta.validation.Valid;
import org.abdel.aiops.application.incident.AnalyzeIncidentUseCase;
import org.abdel.aiops.domain.incident.IncidentAnalysis;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {
    private final AnalyzeIncidentUseCase useCase;

    public IncidentController(AnalyzeIncidentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/analyze")
    public IncidentAnalysis analyze(@Valid @RequestBody IncidentRequest incidentRequest){
        return useCase.execute(incidentRequest.service(), incidentRequest.stackTrace());
    }
}
