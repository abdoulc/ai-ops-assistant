package org.abdel.aiops.infrastructure.document;

import org.abdel.aiops.domain.DomainUtils;
import org.abdel.aiops.domain.document.ChunkingPolicy;
import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentChunk;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.DocumentType;
import org.abdel.aiops.domain.document.IngestionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterDocumentChunkerTest {

    private final CharacterDocumentChunker chunker = new CharacterDocumentChunker();

    @Test
    void shouldSplitLongDocumentIntoMultipleChunks() {
        Document document = createDocument("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        ChunkingPolicy policy = new ChunkingPolicy(10, 2);

        List<DocumentChunk> chunks = chunker.split(document, policy);

        assertThat(chunks).hasSize(3);
        assertThat(chunks)
                .extracting(DocumentChunk::content)
                .containsExactly(
                        "ABCDEFGHIJ",
                        "IJKLMNOPQR",
                        "QRSTUVWXYZ"
                );
        assertThat(chunks)
                .extracting(DocumentChunk::index)
                .containsExactly(0, 1, 2);
        assertThat(chunks)
                .extracting(DocumentChunk::documentId)
                .containsOnly(document.id());
    }

    @Test
    void shouldCreateSingleChunkForShortDocument() {
        Document document = createDocument("ABCDE");

        List<DocumentChunk> chunks = chunker.split(
                document,
                new ChunkingPolicy(10, 2)
        );

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).content()).isEqualTo("ABCDE");
        assertThat(chunks.get(0).index()).isZero();
    }

    @Test
    void shouldCreateSingleChunkWhenContentMatchesChunkSize() {
        Document document = createDocument("ABCDEFGHIJ");

        List<DocumentChunk> chunks = chunker.split(
                document,
                new ChunkingPolicy(10, 2)
        );

        assertThat(chunks)
                .extracting(DocumentChunk::content)
                .containsExactly("ABCDEFGHIJ");
    }

    @Test
    void shouldSplitWithoutOverlap() {
        Document document = createDocument("ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        List<DocumentChunk> chunks = chunker.split(
                document,
                new ChunkingPolicy(10, 0)
        );

        assertThat(chunks)
                .extracting(DocumentChunk::content)
                .containsExactly(
                        "ABCDEFGHIJ",
                        "KLMNOPQRST",
                        "UVWXYZ"
                );
    }

    @Test
    void shouldAllowLastChunkToBeShorter() {
        Document document = createDocument("ABCDEFGHIJKLMNOPQRSTUVWXY");

        List<DocumentChunk> chunks = chunker.split(
                document,
                new ChunkingPolicy(10, 2)
        );

        assertThat(chunks)
                .extracting(DocumentChunk::content)
                .containsExactly(
                        "ABCDEFGHIJ",
                        "IJKLMNOPQR",
                        "QRSTUVWXY"
                );
    }

    @Test
    void shouldCalculateChecksumForEachChunk() {
        Document document = createDocument("ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        List<DocumentChunk> chunks = chunker.split(
                document,
                new ChunkingPolicy(10, 2)
        );

        assertThat(chunks).allSatisfy(chunk ->
                assertThat(chunk.checksum()).isEqualTo(
                        DomainUtils.calculateChecksum(chunk.content())
                )
        );
    }

    @Test
    void shouldProduceSameChunksForSameInputAndPolicy() {
        Document document = createDocument("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        ChunkingPolicy policy = new ChunkingPolicy(10, 2);

        List<DocumentChunk> firstResult =
                chunker.split(document, policy);
        List<DocumentChunk> secondResult =
                chunker.split(document, policy);

        assertThat(firstResult).isNotNull();
        assertThat(secondResult).isEqualTo(firstResult);
    }

    private Document createDocument(String content) {
        return new Document(
                DocumentId.generate(),
                "Test Document",
                content,
                DocumentType.PLAIN_TEXT,
                DomainUtils.calculateChecksum(content),
                IngestionStatus.PENDING,
                Instant.parse("2026-08-23T10:00:00Z")
        );
    }

}
