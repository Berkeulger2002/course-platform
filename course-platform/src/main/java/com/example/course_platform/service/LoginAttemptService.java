package com.example.course_platform.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;


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
    // STATE
    //
    // Bu yapı tek backend instance'ı için yeterlidir.
    // Uygulama birden fazla instance'a ölçeklenirse Redis gibi
    // ortak bir store'a taşınmalıdır.
    // =========================================================

    private final ConcurrentHashMap<String, AttemptWindow>
            emailFailures =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, AttemptWindow>
            ipFailures =
            new ConcurrentHashMap<>();


    // =========================================================
    // BLOCK CHECK
    // =========================================================

    public boolean isBlocked(
            String email,
            String clientIp
    ) {

        Instant now =
                Instant.now();

        return
                isBlocked(
                        emailFailures,
                        normalizeEmail(email),
                        MAX_FAILURES_PER_EMAIL,
                        now
                )
                        ||
                        isBlocked(
                                ipFailures,
                                normalizeIp(clientIp),
                                MAX_FAILURES_PER_IP,
                                now
                        );
    }


    // =========================================================
    // FAILED LOGIN
    // =========================================================

    public void loginFailed(
            String email,
            String clientIp
    ) {

        Instant now =
                Instant.now();

        increment(
                emailFailures,
                normalizeEmail(email),
                now
        );

        increment(
                ipFailures,
                normalizeIp(clientIp),
                now
        );
    }


    // =========================================================
    // SUCCESSFUL LOGIN
    //
    // Başarılı girişte ilgili hesabın başarısız deneme sayacını
    // temizliyoruz. IP sayacını silmiyoruz; aynı IP üzerinden çok
    // sayıda hesaba brute-force yapılmasını engellemeye devam eder.
    // =========================================================

    public void loginSucceeded(
            String email
    ) {

        emailFailures.remove(
                normalizeEmail(email)
        );
    }


    // =========================================================
    // RETRY AFTER
    // =========================================================

    public long retryAfterSeconds(
            String email,
            String clientIp
    ) {

        Instant now =
                Instant.now();

        long emailRetry =
                retryAfterSeconds(
                        emailFailures,
                        normalizeEmail(email),
                        MAX_FAILURES_PER_EMAIL,
                        now
                );

        long ipRetry =
                retryAfterSeconds(
                        ipFailures,
                        normalizeIp(clientIp),
                        MAX_FAILURES_PER_IP,
                        now
                );

        return Math.max(
                emailRetry,
                ipRetry
        );
    }


    // =========================================================
    // INTERNAL - BLOCK
    // =========================================================

    private boolean isBlocked(
            ConcurrentHashMap<String, AttemptWindow> store,
            String key,
            int limit,
            Instant now
    ) {

        AttemptWindow window =
                store.get(
                        key
                );

        if (
                window == null
        ) {

            return false;
        }


        if (
                isExpired(
                        window,
                        now
                )
        ) {

            store.remove(
                    key,
                    window
            );

            return false;
        }


        return
                window.failures()
                        >=
                        limit;
    }


    // =========================================================
    // INTERNAL - INCREMENT
    // =========================================================

    private void increment(
            ConcurrentHashMap<String, AttemptWindow> store,
            String key,
            Instant now
    ) {

        store.compute(

                key,

                (
                        ignored,
                        current
                ) -> {

                    if (
                            current == null
                                    ||
                                    isExpired(
                                            current,
                                            now
                                    )
                    ) {

                        return new AttemptWindow(
                                1,
                                now
                        );
                    }


                    return new AttemptWindow(

                            current.failures()
                                    +
                                    1,

                            current.windowStartedAt()
                    );
                }
        );
    }


    // =========================================================
    // INTERNAL - RETRY AFTER
    // =========================================================

    private long retryAfterSeconds(
            ConcurrentHashMap<String, AttemptWindow> store,
            String key,
            int limit,
            Instant now
    ) {

        AttemptWindow window =
                store.get(
                        key
                );

        if (
                window == null
                        ||
                        window.failures()
                                <
                                limit
        ) {

            return 0;
        }


        if (
                isExpired(
                        window,
                        now
                )
        ) {

            store.remove(
                    key,
                    window
            );

            return 0;
        }


        Instant unlockAt =
                window.windowStartedAt()
                        .plus(
                                WINDOW
                        );


        long seconds =
                Duration
                        .between(
                                now,
                                unlockAt
                        )
                        .getSeconds();


        return Math.max(
                1,
                seconds
        );
    }


    // =========================================================
    // INTERNAL - WINDOW
    // =========================================================

    private boolean isExpired(
            AttemptWindow window,
            Instant now
    ) {

        return
                !window
                        .windowStartedAt()
                        .plus(
                                WINDOW
                        )
                        .isAfter(
                                now
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


    // =========================================================
    // VALUE OBJECT
    // =========================================================

    private record AttemptWindow(
            int failures,
            Instant windowStartedAt
    ) {
    }
}
