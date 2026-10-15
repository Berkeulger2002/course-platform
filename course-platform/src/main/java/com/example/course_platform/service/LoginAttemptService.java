package com.example.course_platform.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


@Service
public class LoginAttemptService {


    // =========================================================
    // POLICY
    // =========================================================

    private static final Duration WINDOW =
            Duration.ofMinutes(15);

    private static final int MAX_FAILURES_PER_EMAIL =
            5;

    private static final int MAX_FAILURES_PER_IP =
            20;


    // =========================================================
    // REDIS KEY PREFIXES
    // =========================================================

    private static final String EMAIL_KEY_PREFIX =
            "login:attempt:email:";

    private static final String IP_KEY_PREFIX =
            "login:attempt:ip:";


    // =========================================================
    // REDIS
    // =========================================================

    private final StringRedisTemplate redisTemplate;


    public LoginAttemptService(
            StringRedisTemplate redisTemplate
    ) {

        this.redisTemplate =
                redisTemplate;
    }


    // =========================================================
    // BLOCK CHECK
    // =========================================================

    public boolean isBlocked(
            String email,
            String clientIp
    ) {

        String emailKey =
                emailKey(
                        email
                );

        String ipKey =
                ipKey(
                        clientIp
                );


        long emailFailures =
                getFailureCount(
                        emailKey
                );

        long ipFailures =
                getFailureCount(
                        ipKey
                );


        return
                emailFailures >= MAX_FAILURES_PER_EMAIL
                        ||
                        ipFailures >= MAX_FAILURES_PER_IP;
    }


    // =========================================================
    // FAILED LOGIN
    // =========================================================

    public void loginFailed(
            String email,
            String clientIp
    ) {

        increment(
                emailKey(
                        email
                )
        );

        increment(
                ipKey(
                        clientIp
                )
        );
    }


    // =========================================================
    // SUCCESSFUL LOGIN
    //
    // Başarılı girişte sadece ilgili e-posta sayacını
    // temizliyoruz.
    //
    // IP sayacı korunur. Böylece aynı IP üzerinden çok sayıda
    // farklı hesaba brute-force yapılması engellenmeye devam eder.
    // =========================================================

    public void loginSucceeded(
            String email
    ) {

        redisTemplate.delete(
                emailKey(
                        email
                )
        );
    }


    // =========================================================
    // RETRY AFTER
    // =========================================================

    public long retryAfterSeconds(
            String email,
            String clientIp
    ) {

        long emailRetry =
                retryAfterSeconds(
                        emailKey(
                                email
                        ),
                        MAX_FAILURES_PER_EMAIL
                );

        long ipRetry =
                retryAfterSeconds(
                        ipKey(
                                clientIp
                        ),
                        MAX_FAILURES_PER_IP
                );


        return Math.max(
                emailRetry,
                ipRetry
        );
    }


    // =========================================================
    // INTERNAL - INCREMENT
    // =========================================================

    private void increment(
            String key
    ) {

        Long failures =
                redisTemplate
                        .opsForValue()
                        .increment(
                                key
                        );


        if (
                failures != null
                        &&
                        failures == 1
        ) {

            redisTemplate.expire(
                    key,
                    WINDOW
            );
        }
    }


    // =========================================================
    // INTERNAL - FAILURE COUNT
    // =========================================================

    private long getFailureCount(
            String key
    ) {

        String value =
                redisTemplate
                        .opsForValue()
                        .get(
                                key
                        );


        if (
                value == null
        ) {

            return 0;
        }


        try {

            return Long.parseLong(
                    value
            );

        } catch (
                NumberFormatException exception
        ) {

            redisTemplate.delete(
                    key
            );

            return 0;
        }
    }


    // =========================================================
    // INTERNAL - RETRY AFTER
    // =========================================================

    private long retryAfterSeconds(
            String key,
            int limit
    ) {

        long failures =
                getFailureCount(
                        key
                );


        if (
                failures < limit
        ) {

            return 0;
        }


        Long ttl =
                redisTemplate.getExpire(
                        key,
                        TimeUnit.SECONDS
                );


        if (
                ttl == null
                        ||
                        ttl <= 0
        ) {

            redisTemplate.delete(
                    key
            );

            return 0;
        }


        return Math.max(
                1,
                ttl
        );
    }


    // =========================================================
    // REDIS KEYS
    // =========================================================

    private String emailKey(
            String email
    ) {

        return EMAIL_KEY_PREFIX
                +
                normalizeEmail(
                        email
                );
    }


    private String ipKey(
            String clientIp
    ) {

        return IP_KEY_PREFIX
                +
                normalizeIp(
                        clientIp
                );
    }


    // =========================================================
    // NORMALIZATION
    // =========================================================

    private String normalizeEmail(
            String email
    ) {

        if (
                email == null
        ) {

            return "<unknown-email>";
        }


        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private String normalizeIp(
            String clientIp
    ) {

        if (
                clientIp == null
                        ||
                        clientIp.isBlank()
        ) {

            return "<unknown-ip>";
        }


        return clientIp.trim();
    }
}