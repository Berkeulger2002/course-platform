package com.example.course_platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class CsrfController {


    // =========================================================
    // CSRF TOKEN
    //
    // Frontend gerektiğinde bu endpointi çağırır.
    //
    // Spring:
    //
    // XSRF-TOKEN
    //
    // isimli cookie oluşturur.
    //
    // Bu cookie HttpOnly değildir çünkü Angular'ın tokenı
    // okuyup X-XSRF-TOKEN header olarak göndermesi gerekir.
    // =========================================================

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> getCsrfToken(
            CsrfToken csrfToken
    ) {


        return ResponseEntity.ok(
                Map.of(
                        "token",
                        csrfToken.getToken()
                )
        );
    }
}