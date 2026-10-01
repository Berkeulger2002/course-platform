package com.example.course_platform.controller;


import com.example.course_platform.config.JwtService;

import com.example.course_platform.dto.AuthMeResponse;
import com.example.course_platform.dto.LoginRequest;
import com.example.course_platform.dto.RegisterRequest;

import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.Student;
import com.example.course_platform.entity.User;
import com.example.course_platform.entity.UserSession;

import com.example.course_platform.repository.StudentRepository;
import com.example.course_platform.repository.UserRepository;

import com.example.course_platform.service.LoginAttemptService;
import com.example.course_platform.service.UserSessionService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

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
@RequestMapping(
        "/api/auth"
)
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


    private final UserSessionService
            userSessionService;


    private final LoginAttemptService
            loginAttemptService;


    // =========================================================
    // COOKIE
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

            UserSessionService userSessionService,

            LoginAttemptService loginAttemptService,

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


        this.userSessionService =
                userSessionService;


        this.loginAttemptService =
                loginAttemptService;


        this.cookieName =
                cookieName;


        this.cookieSecure =
                cookieSecure;
    }


    // =========================================================
    // REGISTER
    //
    // PUBLIC REGISTER = STUDENT ONLY
    // =========================================================

    @PostMapping(
            "/register"
    )
    public ResponseEntity<String>
    register(

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
        // GMAIL
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
        // DUPLICATE
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
        // PASSWORD
        // =====================================================

        String encodedPassword =

                passwordEncoder.encode(
                        request.password()
                );


        // =====================================================
        // STUDENT
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
    // =========================================================

    @PostMapping(
            "/login"
    )
    public ResponseEntity<?>
    login(

            @Valid
            @RequestBody
            LoginRequest request,

            HttpServletRequest httpRequest
    ) {


        String email =

                normalizeEmail(
                        request.email()
                );



        String clientIp =
                httpRequest.getRemoteAddr();


        if (
                loginAttemptService
                        .isBlocked(
                                email,
                                clientIp
                        )
        ) {


            long retryAfterSeconds =
                    loginAttemptService
                            .retryAfterSeconds(
                                    email,
                                    clientIp
                            );


            return ResponseEntity

                    .status(
                            HttpStatus.TOO_MANY_REQUESTS
                    )

                    .header(
                            HttpHeaders.RETRY_AFTER,
                            String.valueOf(
                                    retryAfterSeconds
                            )
                    )

                    .body(
                            "Çok fazla başarısız giriş denemesi. Lütfen daha sonra tekrar deneyin."
                    );
        }


        Optional<User> userOptional =

                userRepository
                        .findByEmailIgnoreCase(
                                email
                        );


        if (
                userOptional.isEmpty()
        ) {


            loginAttemptService
                    .loginFailed(
                            email,
                            clientIp
                    );


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


            loginAttemptService
                    .loginFailed(
                            email,
                            clientIp
                    );


            return ResponseEntity

                    .status(
                            HttpStatus.UNAUTHORIZED
                    )

                    .body(
                            "E-posta veya şifre hatalı!"
                    );
        }


        loginAttemptService
                .loginSucceeded(
                        email
                );


        // =====================================================
        // CREATE DATABASE SESSION
        // =====================================================

        UserSession session =

                userSessionService
                        .createSession(

                                user,

                                jwtService
                                        .getExpirationMs()
                        );


        // =====================================================
        // JWT + SID
        // =====================================================

        String token =

                jwtService.generateToken(

                        user,

                        session.getSessionId()
                );


        // =====================================================
        // COOKIE
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
    // CURRENT USER
    // =========================================================

    @GetMapping(
            "/me"
    )
    public ResponseEntity<?>
    me(

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

                                authentication
                                        .getName()
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
    // HEARTBEAT
    //
    // Frontend kullanıcı gerçekten aktifken
    // yaklaşık 60 saniyede bir çağıracak.
    // =========================================================

    @PostMapping(
            "/heartbeat"
    )
    public ResponseEntity<Void>
    heartbeat(

            HttpServletRequest request
    ) {


        String sessionId =

                extractSessionIdFromRequest(
                        request
                );


        if (
                sessionId == null
        ) {


            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .build();
        }


        userSessionService
                .heartbeat(
                        sessionId
                );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping(
            "/logout"
    )
    public ResponseEntity<Void>
    logout(

            HttpServletRequest request
    ) {


        String sessionId =

                extractSessionIdFromRequest(
                        request
                );


        // =====================================================
        // SESSION HISTORY
        // =====================================================

        if (
                sessionId != null
        ) {


            userSessionService
                    .logout(
                            sessionId
                    );
        }


        // =====================================================
        // DELETE COOKIE
        // =====================================================

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
    // SESSION ID FROM JWT COOKIE
    // =========================================================

    private String extractSessionIdFromRequest(

            HttpServletRequest request
    ) {


        String token =

                extractTokenFromCookie(
                        request
                );


        if (
                token == null
                        ||
                        !jwtService
                                .isTokenValid(
                                        token
                                )
        ) {


            return null;
        }


        try {


            return jwtService
                    .extractSessionId(
                            token
                    );


        } catch (
                Exception exception
        ) {


            return null;
        }
    }


    // =========================================================
    // TOKEN FROM COOKIE
    // =========================================================

    private String extractTokenFromCookie(

            HttpServletRequest request
    ) {


        Cookie[] cookies =
                request.getCookies();


        if (
                cookies == null
        ) {


            return null;
        }


        for (
                Cookie cookie
                :
                cookies
        ) {


            if (
                    cookieName.equals(
                            cookie.getName()
                    )
            ) {


                return cookie.getValue();
            }
        }


        return null;
    }


    // =========================================================
    // LOGIN COOKIE
    // =========================================================

    private ResponseCookie
    createLoginCookie(

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

    private AuthMeResponse
    createUserResponse(

            User user
    ) {


        return new AuthMeResponse(

                user.getId(),

                user.getName(),

                user.getEmail(),

                user.getRole()
                        .name()
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