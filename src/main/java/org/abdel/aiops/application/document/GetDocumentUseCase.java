package org.abdel.aiops.application.document;

import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.DocumentNotFoundException;
import org.abdel.aiops.domain.document.ports.DocumentRepository;

public class GetDocumentUseCase {

    private final DocumentRepository documentRepository;

    public GetDocumentUseCase(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document execute(DocumentId id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }
}
