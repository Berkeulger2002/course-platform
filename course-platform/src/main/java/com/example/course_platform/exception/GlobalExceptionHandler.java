package com.example.course_platform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {


    // =========================================================
    // 403 FORBIDDEN
    //
    // Kullanıcı giriş yapmış durumda fakat başka bir
    // kullanıcının verisine veya yetkisi olmayan bir
    // kaynağa erişmeye çalışıyor.
    // =========================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(
            AccessDeniedException exception) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        Map.of(
                                "error",
                                "Forbidden",
                                "message",
                                exception.getMessage()
                        )
                );
    }


    // =========================================================
    // 401 UNAUTHORIZED
    //
    // Authentication mevcut değil veya geçersiz.
    //
    // Not:
    // Spring Security filter seviyesinde oluşan 401 cevapları
    // SecurityConfig tarafından yönetilmeye devam eder.
    // =========================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(
            AuthenticationException exception) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        Map.of(
                                "error",
                                "Unauthorized",
                                "message",
                                exception.getMessage()
                        )
                );
    }
}