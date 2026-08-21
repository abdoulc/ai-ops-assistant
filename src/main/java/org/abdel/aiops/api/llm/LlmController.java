package org.abdel.aiops.api.llm;

import org.abdel.aiops.application.llm.GenerateResponseUseCase;
import org.abdel.aiops.domain.llm.LlmResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/llm")
public class LlmController {
    private final GenerateResponseUseCase useCase;

    public LlmController(GenerateResponseUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/generate")
    public LlmResponse generate(@RequestBody GenerateRequest request) {
        return useCase.execute(request.prompt());
    }
}
