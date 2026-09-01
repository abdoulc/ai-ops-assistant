package org.abdel.aiops.infrastructure.document;

import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.DocumentType;
import org.abdel.aiops.domain.document.IngestionStatus;
import org.abdel.aiops.domain.document.ports.DocumentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class JdbcDocumentRepository implements DocumentRepository {

    private static final String SELECT_COLUMNS = """
            SELECT id, title, content, document_type, checksum, status, created_at
            FROM documents
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcDocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Document save(Document document) {
        jdbcTemplate.update("""
                        INSERT INTO documents (
                            id, title, content, document_type,
                            checksum, status, created_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                document.id().value(),
                document.title(),
                document.content(),
                document.type().name(),
                document.checksum(),
                document.status().name(),
                Timestamp.from(document.createdAt())
        );
        return document;
    }

    @Override
    public Optional<Document> findById(DocumentId id) {
        return queryOne(SELECT_COLUMNS + " WHERE id = ?", id.value());
    }

    @Override
    public Optional<Document> findByChecksum(String checksum) {
        return queryOne(SELECT_COLUMNS + " WHERE checksum = ?", checksum);
    }

    @Override
    public boolean existsByChecksum(String checksum) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM documents WHERE checksum = ?)",
                Boolean.class,
                checksum
        ));
    }

    private Optional<Document> queryOne(String sql, Object argument) {
        return jdbcTemplate.query(sql, this::mapDocument, argument)
                .stream()
                .findFirst();
    }

    private Document mapDocument(ResultSet resultSet, int rowNumber)
            throws SQLException {
        return new Document(
                new DocumentId(resultSet.getObject("id", java.util.UUID.class)),
                resultSet.getString("title"),
                resultSet.getString("content"),
                DocumentType.valueOf(resultSet.getString("document_type")),
                resultSet.getString("checksum"),
                IngestionStatus.valueOf(resultSet.getString("status")),
                resultSet.getTimestamp("created_at").toInstant()
        );
    }
}
