package com.example.course_platform.config;


import com.example.course_platform.entity.User;

import com.example.course_platform.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {


    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final JwtService jwtService;

    private final UserRepository userRepository;


    // =========================================================
    // COOKIE NAME
    // =========================================================

    private final String cookieName;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public JwtAuthenticationFilter(

            JwtService jwtService,

            UserRepository userRepository,

            @Value("${app.auth.cookie-name}")
            String cookieName

    ) {

        this.jwtService =
                jwtService;

        this.userRepository =
                userRepository;

        this.cookieName =
                cookieName;
    }


    // =========================================================
    // FILTER
    // =========================================================

    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain

    ) throws ServletException, IOException {


        String token =
                extractTokenFromCookie(
                        request
                );


        if (
                token != null
                        &&
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                == null
                        &&
                        jwtService.isTokenValid(
                                token
                        )
        ) {


            String email =
                    jwtService.extractEmail(
                            token
                    );


            Optional<User> userOptional =
                    userRepository
                            .findByEmailIgnoreCase(
                                    email
                            );


            if (
                    userOptional.isPresent()
            ) {


                User user =
                        userOptional.get();


                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_"
                                        + user.getRole().name()
                        );


                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(

                                user.getEmail(),

                                null,

                                List.of(
                                        authority
                                )
                        );


                authentication.setDetails(

                        new WebAuthenticationDetailsSource()
                                .buildDetails(
                                        request
                                )
                );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }
        }


        filterChain.doFilter(
                request,
                response
        );
    }


    // =========================================================
    // COOKIE'DEN TOKEN AL
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
}