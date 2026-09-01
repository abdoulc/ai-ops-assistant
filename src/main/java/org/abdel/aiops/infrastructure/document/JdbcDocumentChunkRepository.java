package org.abdel.aiops.infrastructure.document;

import org.abdel.aiops.domain.document.DocumentChunk;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.ports.DocumentChunkRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcDocumentChunkRepository implements DocumentChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDocumentChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(Iterable<DocumentChunk> chunks) {
        List<DocumentChunk> chunkList = new ArrayList<>();
        chunks.forEach(chunkList::add);
        if (chunkList.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate("""
                        INSERT INTO document_chunks (
                            document_id, chunk_index, content, checksum
                        ) VALUES (?, ?, ?, ?)
                        """,
                chunkList,
                chunkList.size(),
                (statement, chunk) -> {
                    statement.setObject(1, chunk.documentId().value());
                    statement.setInt(2, chunk.index());
                    statement.setString(3, chunk.content());
                    statement.setString(4, chunk.checksum());
                }
        );
    }

    @Override
    public List<DocumentChunk> findByDocumentId(DocumentId documentId) {
        return jdbcTemplate.query("""
                        SELECT document_id, chunk_index, content, checksum
                        FROM document_chunks
                        WHERE document_id = ?
                        ORDER BY chunk_index
                        """,
                (resultSet, rowNumber) -> new DocumentChunk(
                        new DocumentId(resultSet.getObject(
                                "document_id",
                                java.util.UUID.class
                        )),
                        resultSet.getInt("chunk_index"),
                        resultSet.getString("content"),
                        resultSet.getString("checksum")
                ),
                documentId.value()
        );
    }
}
