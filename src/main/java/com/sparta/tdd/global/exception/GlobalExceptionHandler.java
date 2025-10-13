package com.sparta.tdd.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j(topic = "GlobalExceptionHandler")
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("handleBusinessException : {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode().name(), e.getMessage());

        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("handleMethodArgumentNotValidException : {}", e.getMessage());

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getDefaultMessage())
            .findFirst()
            .orElse("잘못된 요청입니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorMessage);
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<String> handleBindException(BindException e) {
        log.warn("handleBindException : {}", e.getMessage());

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getDefaultMessage())
            .findFirst()
            .orElse("잘못된 요청입니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorMessage);
    }

    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        MissingRequestHeaderException.class,
        TypeMismatchException.class,
        MethodArgumentTypeMismatchException.class
    })
    protected ResponseEntity<String> handleValidException(Exception e) {
        log.warn("handleValidException : {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("잘못된 요청입니다.");
    }

    @ExceptionHandler({
        NoHandlerFoundException.class,
        NoResourceFoundException.class
    })
    protected ResponseEntity<String> handleNotFoundException(Exception e) {
        log.warn("handleNotFoundException : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("요청하신 리소스를 찾을 수 없습니다.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("handleIllegalArgumentException : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    protected ResponseEntity<String> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("handleAuthorizationDeniedException : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<String> handleException(Exception e) {
        log.error("handleException : {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("서버 내부 오류가 발생했습니다.");
    }
}
