package com.example.course_platform.config;


import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.csrf.CsrfFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
import java.util.List;


@Configuration
public class SecurityConfig {


    // =========================================================
    // JWT FILTER
    // =========================================================

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;


    // =========================================================
    // CORS ORIGINS
    //
    // application.properties:
    //
    // app.cors.allowed-origins=
    // ${CORS_ALLOWED_ORIGINS:http://localhost:4200}
    //
    // Local:
    // http://localhost:4200
    //
    // Production:
    // https://course-platform.example.com
    //
    // Birden fazla origin:
    //
    // https://site1.com,https://site2.com
    // =========================================================

    private final String corsAllowedOrigins;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SecurityConfig(

            JwtAuthenticationFilter jwtAuthenticationFilter,

            @Value(
                    "${app.cors.allowed-origins:http://localhost:4200}"
            )
            String corsAllowedOrigins
    ) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.corsAllowedOrigins =
                corsAllowedOrigins;
    }


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {


        http


                // =================================================
                // CORS
                // =================================================

                .cors(cors ->

                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )


                // =================================================
                // CSRF - SPA MODE
                // =================================================

                .csrf(csrf ->

                        csrf.spa()
                )


                // =================================================
                // SESSION
                // =================================================

                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // =================================================
                // FORM LOGIN KAPALI
                // =================================================

                .formLogin(form ->

                        form.disable()
                )


                // =================================================
                // HTTP BASIC KAPALI
                // =================================================

                .httpBasic(basic ->

                        basic.disable()
                )


                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth ->

                        auth


                                // =================================
                                // CORS PREFLIGHT
                                // =================================

                                .requestMatchers(
                                        HttpMethod.OPTIONS,
                                        "/**"
                                )
                                .permitAll()


                                // =================================
                                // FRONTEND / SPA / STATIC FILES
                                // =================================
                                //
                                // Backend endpointlerinin tamamı:
                                //
                                // /api/**
                                //
                                // altında.
                                //
                                // /api dışındaki yollar Angular
                                // uygulamasına veya static dosyalara
                                // aittir.
                                //
                                // Böylece:
                                //
                                // /
                                // /index.html
                                // /main-xxx.js
                                // /styles-xxx.css
                                // /student/dashboard
                                // /teacher/dashboard
                                // /error
                                //
                                // gibi yollar JWT istemez.
                                //
                                // API güvenliği ise aşağıdaki
                                // kurallarla devam eder.
                                //
                                // =================================

                                .requestMatchers(request -> {

                                    String uri =
                                            request.getRequestURI();


                                    return
                                            !"/api".equals(uri)
                                                    &&
                                                    !uri.startsWith(
                                                            "/api/"
                                                    );
                                })
                                .permitAll()


                                // =================================
                                // AUTH - PUBLIC
                                // =================================

                                .requestMatchers(
                                        "/api/auth/login",
                                        "/api/auth/register"
                                )
                                .permitAll()


                                // =================================
                                // CSRF TOKEN
                                // =================================

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/auth/csrf"
                                )
                                .permitAll()


                                // =================================
                                // PUBLIC KURS LİSTESİ
                                // =================================

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/courses",
                                        "/api/courses/"
                                )
                                .permitAll()


                                // =================================
                                // PUBLIC COURSE REVIEWS
                                // =================================

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/reviews/course/**"
                                )
                                .permitAll()


                                // =================================
                                // ESKİ GÜVENSİZ STUDENT CREATE
                                // =================================

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/students/add"
                                )
                                .denyAll()


                                // =================================
                                // ESKİ GÜVENSİZ TEACHER CREATE
                                // =================================

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/teachers",
                                        "/api/teachers/"
                                )
                                .denyAll()


                                // =================================
                                // TÜM ÖĞRETMENLERİ LİSTELEME
                                // =================================

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/teachers",
                                        "/api/teachers/"
                                )
                                .denyAll()


                                // =================================
                                // ESKİ GENEL TEACHER UPDATE
                                // =================================

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/teachers/*"
                                )
                                .denyAll()


                                // =================================
                                // TEACHER DELETE
                                // =================================

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/teachers/*"
                                )
                                .denyAll()


                                // =================================
                                // COURSE - TEACHER
                                // =================================

                                .requestMatchers(
                                        "/api/courses/teacher/**",
                                        "/api/courses/*/teacher/**"
                                )
                                .hasRole(
                                        "TEACHER"
                                )


                                // =================================
                                // COURSE - STUDENT
                                // =================================

                                .requestMatchers(
                                        "/api/courses/student/**",
                                        "/api/courses/*/video/student/**"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // CART
                                // =================================

                                .requestMatchers(
                                        "/api/carts/**"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // CERTIFICATES
                                // =================================

                                .requestMatchers(
                                        "/api/certificates/**"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // COURSE PROGRESS
                                // =================================

                                .requestMatchers(
                                        "/api/progress/**"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // FAVORITES
                                // =================================

                                .requestMatchers(
                                        "/api/favorites/**"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // PAYMENT HISTORY
                                // =================================

                                .requestMatchers(
                                        "/api/payment-history/**"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // ORDERS
                                // =================================

                                .requestMatchers(
                                        "/api/orders/**"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // STUDENT PROFILE
                                // =================================

                                .requestMatchers(
                                        "/api/students/**"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // REVIEWS CREATE
                                // =================================

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/reviews",
                                        "/api/reviews/"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // STUDENT NOTIFICATIONS
                                // =================================

                                .requestMatchers(
                                        "/api/notifications/student/**"
                                )
                                .hasRole(
                                        "STUDENT"
                                )


                                // =================================
                                // TEACHER NOTIFICATIONS
                                // =================================

                                .requestMatchers(
                                        "/api/notifications/teacher/**"
                                )
                                .hasRole(
                                        "TEACHER"
                                )


                                // =================================
                                // TEACHER PROFILE
                                // =================================

                                .requestMatchers(
                                        "/api/teachers/**"
                                )
                                .hasRole(
                                        "TEACHER"
                                )


                                // =================================
                                // TEACHER REVENUE
                                // =================================

                                .requestMatchers(
                                        "/api/teacher-revenue/**"
                                )
                                .hasRole(
                                        "TEACHER"
                                )


                                // =================================
                                // TEACHER STUDENTS
                                // =================================

                                .requestMatchers(
                                        "/api/teacher-students/**"
                                )
                                .hasRole(
                                        "TEACHER"
                                )


                                // =================================
                                // IMAGE / VIDEO UPLOAD
                                // =================================

                                .requestMatchers(
                                        "/api/images/**"
                                )
                                .hasRole(
                                        "TEACHER"
                                )


                                // =================================
                                // DİĞER TÜM API ENDPOINTLERİ
                                // =================================
                                //
                                // Buraya kadar eşleşmeyen API
                                // endpointi public olmaz.
                                //
                                // En azından giriş yapmış kullanıcı
                                // gerekir.
                                //
                                // =================================

                                .anyRequest()
                                .authenticated()
                )


                // =================================================
                // 401 / 403
                // =================================================

                .exceptionHandling(exception ->

                        exception


                                // ---------------------------------
                                // JWT YOK / GEÇERSİZ
                                // ---------------------------------

                                .authenticationEntryPoint(
                                        (
                                                request,
                                                response,
                                                authException
                                        ) ->

                                                response.sendError(
                                                        HttpServletResponse
                                                                .SC_UNAUTHORIZED
                                                )
                                )


                                // ---------------------------------
                                // JWT VAR AMA YETKİ YOK
                                // ---------------------------------

                                .accessDeniedHandler(
                                        (
                                                request,
                                                response,
                                                accessDeniedException
                                        ) ->

                                                response.sendError(
                                                        HttpServletResponse
                                                                .SC_FORBIDDEN
                                                )
                                )
                )


                // =================================================
                // JWT FILTER
                //
                // JWT authentication,
                // CSRF kontrolünden önce hazırlanır.
                // =================================================

                .addFilterBefore(

                        jwtAuthenticationFilter,

                        CsrfFilter.class
                );


        return http.build();
    }


    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {


        CorsConfiguration configuration =
                new CorsConfiguration();


        // =====================================================
        // ALLOWED ORIGINS
        // =====================================================
        //
        // Virgülle ayrılmış environment variable desteklenir.
        //
        // Local:
        //
        // http://localhost:4200
        //
        // Production örneği:
        //
        // https://course-platform.onrender.com
        //
        // =====================================================

        List<String> allowedOrigins =

                Arrays.stream(
                                corsAllowedOrigins.split(",")
                        )

                        .map(
                                String::trim
                        )

                        .filter(
                                origin ->
                                        !origin.isBlank()
                        )

                        .toList();


        configuration.setAllowedOrigins(
                allowedOrigins
        );


        // =====================================================
        // HTTP METHODS
        // =====================================================

        configuration.setAllowedMethods(

                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );


        // =====================================================
        // HEADERS
        //
        // X-XSRF-TOKEN dahil.
        // =====================================================

        configuration.setAllowedHeaders(

                List.of(
                        "*"
                )
        );


        // =====================================================
        // COOKIE
        // =====================================================

        configuration.setAllowCredentials(
                true
        );


        // =====================================================
        // PREFLIGHT CACHE
        // =====================================================

        configuration.setMaxAge(
                3600L
        );


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                configuration
        );


        return source;
    }
}