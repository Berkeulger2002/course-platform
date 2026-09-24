package com.example.course_platform.controller;


import com.example.course_platform.config.JwtService;

import com.example.course_platform.dto.AuthMeResponse;
import com.example.course_platform.dto.LoginRequest;
import com.example.course_platform.dto.RegisterRequest;

import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.Student;
import com.example.course_platform.entity.User;

import com.example.course_platform.repository.StudentRepository;
import com.example.course_platform.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;


@RestController
@RequestMapping("/api/auth")
public class AuthController {


    // =========================================================
    // GMAIL
    // =========================================================

    private static final Pattern GMAIL_PATTERN =
            Pattern.compile(

                    "^[A-Za-z0-9._%+-]+@gmail\\.com$",

                    Pattern.CASE_INSENSITIVE

            );


    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final StudentRepository
            studentRepository;

    private final UserRepository
            userRepository;

    private final PasswordEncoder
            passwordEncoder;

    private final JwtService
            jwtService;


    // =========================================================
    // COOKIE SETTINGS
    // =========================================================

    private final String
            cookieName;

    private final boolean
            cookieSecure;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AuthController(

            StudentRepository studentRepository,

            UserRepository userRepository,

            PasswordEncoder passwordEncoder,

            JwtService jwtService,

            @Value("${app.auth.cookie-name}")
            String cookieName,

            @Value("${app.auth.cookie-secure}")
            boolean cookieSecure

    ) {


        this.studentRepository =
                studentRepository;


        this.userRepository =
                userRepository;


        this.passwordEncoder =
                passwordEncoder;


        this.jwtService =
                jwtService;


        this.cookieName =
                cookieName;


        this.cookieSecure =
                cookieSecure;
    }


    // =========================================================
    // REGISTER
    //
    // PUBLIC REGISTRATION SADECE STUDENT OLUŞTURUR.
    //
    // İstemci artık rol göndermez.
    //
    // Böylece bir kullanıcı request body değiştirerek
    // kendisini TEACHER yapamaz.
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<String> register(

            @Valid
            @RequestBody
            RegisterRequest request

    ) {


        String name =
                request
                        .name()
                        .trim();


        String email =
                normalizeEmail(
                        request.email()
                );


        // =====================================================
        // GMAIL CONTROL
        // =====================================================

        if (
                !GMAIL_PATTERN
                        .matcher(
                                email
                        )
                        .matches()
        ) {


            return ResponseEntity
                    .badRequest()
                    .body(
                            "Sadece @gmail.com uzantılı e-posta adresleri kullanılabilir."
                    );
        }


        // =====================================================
        // DUPLICATE EMAIL
        // =====================================================

        if (
                userRepository
                        .existsByEmailIgnoreCase(
                                email
                        )
        ) {


            return ResponseEntity
                    .status(
                            HttpStatus.CONFLICT
                    )
                    .body(
                            "Bu e-posta adresi zaten kayıtlı."
                    );
        }


        // =====================================================
        // PASSWORD HASH
        // =====================================================

        String encodedPassword =
                passwordEncoder.encode(
                        request.password()
                );


        // =====================================================
        // PUBLIC REGISTER -> ALWAYS STUDENT
        // =====================================================

        Student student =
                new Student();


        student.setName(
                name
        );


        student.setEmail(
                email
        );


        student.setPassword(
                encodedPassword
        );


        student.setRole(
                Role.STUDENT
        );


        studentRepository.save(
                student
        );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        "Öğrenci başarıyla kaydedildi!"
                );
    }


    // =========================================================
    // LOGIN
    //
    // STUDENT ve TEACHER giriş yapabilir.
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(

            @Valid
            @RequestBody
            LoginRequest request

    ) {


        String email =
                normalizeEmail(
                        request.email()
                );


        Optional<User> userOptional =
                userRepository
                        .findByEmailIgnoreCase(
                                email
                        );


        if (
                userOptional.isEmpty()
        ) {


            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            "E-posta veya şifre hatalı!"
                    );
        }


        User user =
                userOptional.get();


        if (
                !passwordEncoder.matches(

                        request.password(),

                        user.getPassword()

                )
        ) {


            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            "E-posta veya şifre hatalı!"
                    );
        }


        // =====================================================
        // JWT
        // =====================================================

        String token =
                jwtService.generateToken(
                        user
                );


        // =====================================================
        // HTTP ONLY COOKIE
        // =====================================================

        ResponseCookie cookie =
                createLoginCookie(
                        token
                );


        // =====================================================
        // RESPONSE
        // =====================================================

        AuthMeResponse response =
                createUserResponse(
                        user
                );


        return ResponseEntity
                .ok()

                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )

                .body(
                        response
                );
    }


    // =========================================================
    // ME
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<?> me(

            Authentication authentication

    ) {


        if (
                authentication == null
        ) {


            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .build();
        }


        Optional<User> userOptional =
                userRepository
                        .findByEmailIgnoreCase(
                                authentication.getName()
                        );


        if (
                userOptional.isEmpty()
        ) {


            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .build();
        }


        return ResponseEntity.ok(

                createUserResponse(
                        userOptional.get()
                )

        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {


        ResponseCookie deleteCookie =
                ResponseCookie

                        .from(
                                cookieName,
                                ""
                        )

                        .httpOnly(
                                true
                        )

                        .secure(
                                cookieSecure
                        )

                        .sameSite(
                                "Lax"
                        )

                        .path(
                                "/"
                        )

                        .maxAge(
                                Duration.ZERO
                        )

                        .build();


        return ResponseEntity

                .noContent()

                .header(
                        HttpHeaders.SET_COOKIE,
                        deleteCookie.toString()
                )

                .build();
    }


    // =========================================================
    // CREATE LOGIN COOKIE
    // =========================================================

    private ResponseCookie createLoginCookie(

            String token

    ) {


        return ResponseCookie

                .from(
                        cookieName,
                        token
                )

                .httpOnly(
                        true
                )

                .secure(
                        cookieSecure
                )

                .sameSite(
                        "Lax"
                )

                .path(
                        "/"
                )

                .maxAge(
                        Duration.ofMillis(
                                jwtService
                                        .getExpirationMs()
                        )
                )

                .build();
    }


    // =========================================================
    // RESPONSE
    // =========================================================

    private AuthMeResponse createUserResponse(

            User user

    ) {


        return new AuthMeResponse(

                user.getId(),

                user.getName(),

                user.getEmail(),

                user.getRole().name()

        );
    }


    // =========================================================
    // EMAIL NORMALIZATION
    // =========================================================

    private String normalizeEmail(

            String email

    ) {


        return email

                .trim()

                .toLowerCase(
                        Locale.ROOT
                );
    }
}