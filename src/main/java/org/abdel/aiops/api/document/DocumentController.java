package org.abdel.aiops.api.document;

import jakarta.validation.Valid;
import org.abdel.aiops.application.document.GetDocumentUseCase;
import org.abdel.aiops.application.document.IngestDocumentCommand;
import org.abdel.aiops.application.document.IngestDocumentUseCase;
import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final IngestDocumentUseCase useCase;
    private final GetDocumentUseCase getDocumentUseCase;

    public DocumentController(
            IngestDocumentUseCase useCase,
            GetDocumentUseCase getDocumentUseCase
    ) {
        this.useCase = useCase;
        this.getDocumentUseCase = getDocumentUseCase;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> ingest(
            @Valid @RequestBody IngestDocumentRequest request
    ) {
        Document document = useCase.execute(new IngestDocumentCommand(
                request.title(),
                request.content(),
                request.type()
        ));

        URI location = URI.create(
                "/api/v1/documents/" + document.id().value()
        );

        return ResponseEntity.created(location)
                .body(DocumentResponse.from(document));
    }

    @GetMapping("/{id}")
    public DocumentDetailsResponse getById(@PathVariable UUID id) {
        Document document = getDocumentUseCase.execute(
                new DocumentId(id)
        );
        return DocumentDetailsResponse.from(document);
    }
}
