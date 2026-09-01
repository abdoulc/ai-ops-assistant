package org.abdel.aiops.infrastructure.document.memory;

import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.ports.DocumentRepository;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("memory")
public class InMemoryDocumentRepository implements DocumentRepository {

    private final Map<DocumentId, Document> documents =
            new ConcurrentHashMap<>();

    @Override
    public Document save(Document document) {
        documents.put(document.id(), document);
        return document;
    }

    @Override
    public Optional<Document> findById(DocumentId id) {
        return Optional.ofNullable(documents.get(id));
    }

    @Override
    public Optional<Document> findByChecksum(String checksum) {
        return documents.values().stream()
                .filter(document -> document.checksum().equals(checksum))
                .findFirst();
    }

    @Override
    public boolean existsByChecksum(String checksum) {
        return findByChecksum(checksum).isPresent();
    }
}
