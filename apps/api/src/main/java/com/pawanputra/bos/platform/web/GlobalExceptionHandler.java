package com.pawanputra.bos.platform.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Maps every error to an RFC 7807 Problem Details body with a stable error
 * code and the request id (docs/architecture/06-api-architecture.md, D-06.3).
 * Business exception types are added here as modules introduce them.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROBLEM_BASE = "https://docs.pawanputra.local/errors/";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setType(URI.create(PROBLEM_BASE + "validation-failed"));
        problem.setTitle("Validation failed");
        problem.setDetail("One or more fields are invalid.");
        problem.setProperty("code", "VALIDATION_FAILED");
        problem.setProperty("errors", fieldErrors(exception));
        addRequestId(problem, request.getAttribute(RequestIdFilter.MDC_KEY, WebRequest.SCOPE_REQUEST));
        return ResponseEntity.unprocessableEntity().body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create(PROBLEM_BASE + "unknown-endpoint"));
        problem.setTitle("Not found");
        // The internal resolution detail ("no static resource ...") is not shown.
        problem.setDetail("This endpoint does not exist.");
        problem.setProperty("code", "UNKNOWN_ENDPOINT");
        addRequestId(problem, request.getAttribute(RequestIdFilter.MDC_KEY, WebRequest.SCOPE_REQUEST));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestIdFilter.MDC_KEY);
        // Details stay in the log; the client gets only the request id to quote.
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), exception);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(PROBLEM_BASE + "internal-error"));
        problem.setTitle("Internal server error");
        problem.setDetail("The request could not be completed. Quote the request id when reporting this.");
        problem.setProperty("code", "INTERNAL_ERROR");
        addRequestId(problem, requestId);
        return ResponseEntity.internalServerError().body(problem);
    }

    private static Map<String, String> fieldErrors(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }

    private static void addRequestId(ProblemDetail problem, Object requestId) {
        if (requestId != null) {
            problem.setProperty("requestId", requestId.toString());
        }
    }
}
