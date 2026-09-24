package com.example.course_platform.config;


import com.example.course_platform.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.io.Decoders;

import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;


@Service
public class JwtService {


    // =========================================================
    // SIGNING KEY
    // =========================================================

    private final SecretKey signingKey;


    // =========================================================
    // TOKEN SÜRESİ
    // =========================================================

    private final long expirationMs;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public JwtService(

            @Value("${app.jwt.secret}")
            String secret,

            @Value("${app.jwt.expiration-ms}")
            long expirationMs

    ) {

        byte[] keyBytes =
                Decoders.BASE64.decode(
                        secret
                );


        this.signingKey =
                Keys.hmacShaKeyFor(
                        keyBytes
                );


        this.expirationMs =
                expirationMs;
    }


    // =========================================================
    // TOKEN OLUŞTUR
    // =========================================================

    public String generateToken(
            User user
    ) {


        Date now =
                new Date();


        Date expiration =
                new Date(
                        now.getTime()
                                + expirationMs
                );


        return Jwts.builder()

                .subject(
                        user.getEmail()
                )

                .claim(
                        "userId",
                        user.getId()
                )

                .claim(
                        "role",
                        user.getRole().name()
                )

                .issuedAt(
                        now
                )

                .expiration(
                        expiration
                )

                .signWith(
                        signingKey
                )

                .compact();
    }


    // =========================================================
    // TOKEN İÇİNDEN EMAIL
    // =========================================================

    public String extractEmail(
            String token
    ) {


        return extractAllClaims(
                token
        )
                .getSubject();
    }


    // =========================================================
    // TOKEN GEÇERLİ Mİ?
    // =========================================================

    public boolean isTokenValid(
            String token
    ) {


        try {

            Claims claims =
                    extractAllClaims(
                            token
                    );


            return claims
                    .getExpiration()
                    .after(
                            new Date()
                    );


        } catch (
                JwtException
                |
                IllegalArgumentException exception
        ) {

            return false;
        }
    }


    // =========================================================
    // CLAIMS
    // =========================================================

    private Claims extractAllClaims(
            String token
    ) {


        return Jwts
                .parser()

                .verifyWith(
                        signingKey
                )

                .build()

                .parseSignedClaims(
                        token
                )

                .getPayload();
    }


    // =========================================================
    // EXPIRATION
    // =========================================================

    public long getExpirationMs() {

        return expirationMs;
    }
}