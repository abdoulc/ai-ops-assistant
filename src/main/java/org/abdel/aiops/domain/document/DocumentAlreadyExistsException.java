package org.abdel.aiops.domain.document;

public class DocumentAlreadyExistsException extends RuntimeException {

    public DocumentAlreadyExistsException(String checksum) {
        super("Document already exists with checksum: " + checksum);
    }
}
