package org.abdel.aiops.domain.document.ports;

import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentId;

import java.util.Optional;

public interface DocumentRepository {

    Document save(Document document);

    Optional<Document> findById(DocumentId id);

    Optional<Document> findByChecksum(String checksum);

    boolean existsByChecksum(String checksum);
}