package org.abdel.aiops.api.error;

import jakarta.servlet.http.HttpServletRequest;
import org.abdel.aiops.domain.document.DocumentAlreadyExistsException;
import org.abdel.aiops.domain.document.DocumentNotFoundException;
import org.abdel.aiops.domain.llm.exception.LlmInvalidResponseException;
import org.abdel.aiops.domain.llm.exception.LlmModelUnavailableException;
import org.abdel.aiops.domain.llm.exception.LlmTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidationError(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "invalid value"
                ));

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "Request validation failed",
                request
        );

        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Malformed request",
                "Request body contains invalid JSON",
                request
        );
    }

    @ExceptionHandler(LlmTimeoutException.class)
    ProblemDetail handleLlmTimeout(
            LlmTimeoutException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.GATEWAY_TIMEOUT,
                "LLM timeout",
                "The language model did not respond in time",
                request
        );
    }

    @ExceptionHandler(LlmModelUnavailableException.class)
    ProblemDetail handleLlmModelUnavailable(
            LlmModelUnavailableException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "LLM unavailable",
                "The configured language model is unavailable",
                request
        );
    }

    @ExceptionHandler(LlmInvalidResponseException.class)
    ProblemDetail handleLlmInvalidResponse(
            LlmInvalidResponseException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.BAD_GATEWAY,
                "Invalid LLM response",
                "The language model returned an invalid response",
                request
        );
    }

    @ExceptionHandler(DocumentAlreadyExistsException.class)
    ProblemDetail handleDocumentAlreadyExists(
            DocumentAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.CONFLICT,
                "Document already exists",
                "A document with the same content already exists",
                request
        );
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    ProblemDetail handleDocumentNotFound(
            DocumentNotFoundException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "Document not found",
                "The requested document does not exist",
                request
        );
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                status,
                detail
        );

        problem.setTitle(title);
        problem.setInstance(
                URI.create(request.getRequestURI())
        );

        return problem;
    }
}
