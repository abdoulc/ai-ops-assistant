package org.abdel.aiops.api.document;

import org.abdel.aiops.application.document.GetDocumentUseCase;
import org.abdel.aiops.application.document.IngestDocumentCommand;
import org.abdel.aiops.application.document.IngestDocumentUseCase;
import org.abdel.aiops.domain.DomainUtils;
import org.abdel.aiops.domain.document.Document;
import org.abdel.aiops.domain.document.DocumentAlreadyExistsException;
import org.abdel.aiops.domain.document.DocumentId;
import org.abdel.aiops.domain.document.DocumentNotFoundException;
import org.abdel.aiops.domain.document.DocumentType;
import org.abdel.aiops.domain.document.IngestionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    private static final String CONTENT = "# Restart the payment service";
    private static final UUID DOCUMENT_ID = UUID.fromString(
            "3b43b952-186f-4b6d-8b88-d78e89fe52fd"
    );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestDocumentUseCase useCase;

    @MockitoBean
    private GetDocumentUseCase getDocumentUseCase;

    @Test
    void shouldIngestValidDocument() throws Exception {
        String checksum = DomainUtils.calculateChecksum(CONTENT);
        Document document = new Document(
                new DocumentId(DOCUMENT_ID),
                "Payment runbook",
                CONTENT,
                DocumentType.MARKDOWN,
                checksum,
                IngestionStatus.PENDING,
                Instant.parse("2026-08-22T20:00:00Z")
        );

        when(useCase.execute(new IngestDocumentCommand(
                "Payment runbook",
                CONTENT,
                DocumentType.MARKDOWN
        ))).thenReturn(document);

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-ID", "document-api-test")
                        .content("""
                                {
                                  "title": "Payment runbook",
                                  "content": "# Restart the payment service",
                                  "type": "MARKDOWN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/documents/" + DOCUMENT_ID
                ))
                .andExpect(header().string(
                        "X-Correlation-ID",
                        "document-api-test"
                ))
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$.title").value("Payment runbook"))
                .andExpect(jsonPath("$.type").value("MARKDOWN"))
                .andExpect(jsonPath("$.checksum").value(checksum))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-08-22T20:00:00Z"));
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": " ",
                                  "content": " ",
                                  "type": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.errors.title")
                        .value("title is required"))
                .andExpect(jsonPath("$.errors.content")
                        .value("content is required"))
                .andExpect(jsonPath("$.errors.type")
                        .value("type is required"));

        verifyNoInteractions(useCase);
    }

    @Test
    void shouldReturnConflictForDuplicateContent() throws Exception {
        String checksum = DomainUtils.calculateChecksum(CONTENT);
        IngestDocumentCommand command = new IngestDocumentCommand(
                "Payment runbook",
                CONTENT,
                DocumentType.MARKDOWN
        );

        when(useCase.execute(command))
                .thenThrow(new DocumentAlreadyExistsException(checksum));

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Payment runbook",
                                  "content": "# Restart the payment service",
                                  "type": "MARKDOWN"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("Document already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(
                        "A document with the same content already exists"
                ))
                .andExpect(jsonPath("$.instance")
                        .value("/api/v1/documents"));
    }

    @Test
    void shouldGetDocumentById() throws Exception {
        Document document = document();
        when(getDocumentUseCase.execute(new DocumentId(DOCUMENT_ID)))
                .thenReturn(document);

        mockMvc.perform(get("/api/v1/documents/{id}", DOCUMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$.title").value("Payment runbook"))
                .andExpect(jsonPath("$.content").value(CONTENT))
                .andExpect(jsonPath("$.type").value("MARKDOWN"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnNotFoundForUnknownDocument() throws Exception {
        DocumentId id = new DocumentId(DOCUMENT_ID);
        when(getDocumentUseCase.execute(id))
                .thenThrow(new DocumentNotFoundException(id));

        mockMvc.perform(get("/api/v1/documents/{id}", DOCUMENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title").value("Document not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(
                        "The requested document does not exist"
                ));
    }

    @Test
    void shouldRejectInvalidDocumentId() throws Exception {
        mockMvc.perform(get("/api/v1/documents/not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getDocumentUseCase);
    }

    private static Document document() {
        return new Document(
                new DocumentId(DOCUMENT_ID),
                "Payment runbook",
                CONTENT,
                DocumentType.MARKDOWN,
                DomainUtils.calculateChecksum(CONTENT),
                IngestionStatus.PENDING,
                Instant.parse("2026-08-22T20:00:00Z")
        );
    }
}
