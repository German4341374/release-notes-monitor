package dev.portfolio.releasemonitor.api;

import dev.portfolio.releasemonitor.error.InvalidProductException;
import dev.portfolio.releasemonitor.error.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "dev.portfolio.releasemonitor.api")
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidProductException.class)
    public ResponseEntity<ProblemDetail> invalidProduct(
            InvalidProductException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Product validation failed", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more request fields are invalid.");
        detail.setTitle("Request validation failed");
        detail.setType(URI.create("https://example.test/problems/validation"));
        detail.setInstance(URI.create(request.getRequestURI()));

        Map<String, String> errors = new LinkedHashMap<>();
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        detail.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(detail);
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status, String title, String message, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        detail.setType(URI.create("https://example.test/problems/" + status.value()));
        detail.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(status).body(detail);
    }
}
