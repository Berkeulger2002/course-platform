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
    // TOKEN EXPIRATION
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
    // LEGACY TOKEN GENERATION
    //
    // Başka bir mevcut kod çağırıyorsa bozulmasın.
    // Yeni login akışı alttaki sessionId'li metodu kullanacak.
    // =========================================================

    public String generateToken(
            User user
    ) {


        return generateToken(

                user,

                null
        );
    }


    // =========================================================
    // TOKEN + SESSION ID
    // =========================================================

    public String generateToken(

            User user,

            String sessionId
    ) {


        Date now =
                new Date();


        Date expiration =
                new Date(

                        now.getTime()
                                +
                                expirationMs
                );


        var builder =

                Jwts.builder()

                        .subject(
                                user.getEmail()
                        )

                        .claim(
                                "userId",
                                user.getId()
                        )

                        .claim(
                                "role",
                                user.getRole()
                                        .name()
                        );


        if (
                sessionId != null
                        &&
                        !sessionId.isBlank()
        ) {


            builder.claim(

                    "sid",

                    sessionId
            );
        }


        return builder

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
    // EMAIL
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
    // SESSION ID
    // =========================================================

    public String extractSessionId(
            String token
    ) {


        return extractAllClaims(
                token
        )
                .get(
                        "sid",
                        String.class
                );
    }


    // =========================================================
    // VALIDATE
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