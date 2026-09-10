package com.example.ems.config;

import com.example.ems.common.dto.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;
import com.example.ems.attendance.exception.DuplicateCheckInException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.onboarding.exception.InvalidOnboardingTransitionException;
import java.util.List;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        List<ErrorResponse.ErrorDetails.Detail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.ErrorDetails.Detail(error.getField(), error.getRejectedValue()))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error("Validation failed: " + errors, "VAL_001", details));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required type");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error(message, "VAL_002"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error("Malformed JSON request body: " + ex.getMessage(), "VAL_003"));
    }

    @ExceptionHandler(DuplicateCheckInException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCheckIn(
            DuplicateCheckInException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.error(ex.getMessage(), "ATT_002"));
    }

    @ExceptionHandler(com.example.ems.attendance.exception.AttendanceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAttendanceNotFound(
            com.example.ems.attendance.exception.AttendanceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.error(ex.getMessage(), "ATT_404"));
    }

    @ExceptionHandler(com.example.ems.attendance.exception.InvalidAttendanceStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAttendanceState(
            com.example.ems.attendance.exception.InvalidAttendanceStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error(ex.getMessage(), "ATT_400"));
    }

    @ExceptionHandler(com.example.ems.attendance.exception.ActiveBreakExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveBreakExists(
            com.example.ems.attendance.exception.ActiveBreakExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.error(ex.getMessage(), "ATT_409"));
    }

    @ExceptionHandler(com.example.ems.attendance.exception.ActiveBreakNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleActiveBreakNotFound(
            com.example.ems.attendance.exception.ActiveBreakNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error(ex.getMessage(), "ATT_400"));
    }

    @ExceptionHandler(com.example.ems.attendance.exception.EmployeeNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotActive(
            com.example.ems.attendance.exception.EmployeeNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error(ex.getMessage(), "EMP_INACTIVE"));
    }

    @ExceptionHandler({
        org.springframework.orm.ObjectOptimisticLockingFailureException.class,
        jakarta.persistence.OptimisticLockException.class
    })
    public ResponseEntity<ErrorResponse> handleOptimisticLockFailure(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.error("Concurrent update conflict detected. Please retry your request.", "ATT_CONCURRENCY_CONFLICT"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.error(ex.getMessage(), "RES_404"));
    }

    @ExceptionHandler(InvalidOnboardingTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOnboardingTransition(
            InvalidOnboardingTransitionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error(ex.getMessage(), "ONB_400"));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.error(ex.getMessage(), "CON_409"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error(ex.getMessage(), "BAD_REQUEST"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.error(ex.getMessage(), "CONFLICT"));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error(ex.getMessage(), "AUTH_002"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error(ex.getMessage(), "AUTH_002"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) throws Exception {
        if (ex instanceof NoResourceFoundException) {
            throw ex;
        }
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.error("An unexpected error occurred: " + ex.getMessage(), "SYS_500"));
    }
}

