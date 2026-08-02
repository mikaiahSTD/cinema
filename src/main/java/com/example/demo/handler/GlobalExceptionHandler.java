package com.example.demo.handler;

import com.example.demo.exception.*;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorBody> handleNotFoundError(NoResourceFoundException ex) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("NOT_FOUND")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorBody> handleNotFound(NotFoundException ex) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("NOT_FOUND")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorBody> handleBadRequest(BadRequestException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("BAD_REQUEST")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    String summary = "Validation failed for fields: " + String.join(", ", errors.keySet());

    ErrorBody errorBody =
        ErrorBody.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(summary)
            .details(errors)
            .build();

    return ResponseEntity.badRequest().body(errorBody);
  }

  @ExceptionHandler(UnprocessableEntityException.class)
  public ResponseEntity<ErrorBody> handleUnprocessable(UnprocessableEntityException ex) {
    HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("UNPROCESSABLE_ENTITY")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorBody> handleBadArgument(IllegalArgumentException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("BAD_REQUEST")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(TypeMismatchException.class)
  public ResponseEntity<ErrorBody> handleTypeMismatch(TypeMismatchException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("BAD_REQUEST")
                .message("Invalid parameter: " + ex.getPropertyName())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(MissingPathVariableException.class)
  public ResponseEntity<ErrorBody> handleMissingPathVariable(MissingPathVariableException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("BAD_REQUEST")
                .message("Missing or invalid path variable: " + ex.getVariableName())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorBody> handleGeneral(Exception ex) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("INTERNAL_ERROR")
                .message("Something went wrong")
                .status(status.value())
                .build());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorBody> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("BAD_REQUEST")
                .message("Request body is missing or malformed.")
                .status(status.value())
                .build());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorBody> handleConflict(ConflictException ex) {
    HttpStatus status = HttpStatus.CONFLICT;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("CONFLICT")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorBody> handleUnauthorized(UnauthorizedException ex) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("UNAUTHORIZED")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorBody> handleForbidden(ForbiddenException ex) {
    HttpStatus status = HttpStatus.FORBIDDEN;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("FORBIDDEN")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorBody> handleConflictException(DataIntegrityViolationException ex) {
    HttpStatus status = HttpStatus.CONFLICT;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("CONFLICT")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorBody> handleBadCredentials(BadCredentialsException ex) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    return ResponseEntity.status(status)
        .body(
            ErrorBody.builder()
                .error("UNAUTHORIZED")
                .message(ex.getMessage())
                .status(status.value())
                .build());
  }
}
