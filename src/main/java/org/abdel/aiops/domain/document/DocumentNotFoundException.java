package org.abdel.aiops.domain.document;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(DocumentId id) {
        super("Document not found: " + id.value());
    }
}
