CREATE TABLE documents (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    document_type VARCHAR(32) NOT NULL,
    checksum CHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT documents_type_check
        CHECK (document_type IN ('MARKDOWN', 'PLAIN_TEXT')),

    CONSTRAINT documents_status_check
        CHECK (
            status IN (
                'PENDING',
                'PROCESSING',
                'COMPLETED',
                'FAILED'
            )
        )
);

CREATE TABLE document_chunks (
    document_id UUID NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    checksum CHAR(64) NOT NULL,

    PRIMARY KEY (document_id, chunk_index),

    CONSTRAINT document_chunks_document_fk
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE,

    CONSTRAINT document_chunks_index_check
        CHECK (chunk_index >= 0)
);

CREATE INDEX document_chunks_checksum_idx
    ON document_chunks(checksum);