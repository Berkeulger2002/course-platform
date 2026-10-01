package com.example.course_platform.service;


import com.example.course_platform.entity.SessionStatus;
import com.example.course_platform.entity.User;
import com.example.course_platform.entity.UserSession;

import com.example.course_platform.repository.UserSessionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserSessionService {



    // =========================================================
    // SESSION VALIDATION
    //
    // JWT içindeki sid gerçekten aktif bir DB oturumuna mı ait?
    // Ayrıca oturumun JWT subject'indeki kullanıcıya ait olduğunu
    // ve süresinin dolmadığını doğrular.
    // =========================================================

    @Transactional(readOnly = true)
    public boolean isSessionActive(
            String sessionId,
            String email
    ) {

        if (
                sessionId == null
                        ||
                        sessionId.isBlank()
                        ||
                        email == null
                        ||
                        email.isBlank()
        ) {

            return false;
        }


        UserSession session =

                userSessionRepository
                        .findBySessionId(
                                sessionId
                        )
                        .orElse(
                                null
                        );


        if (
                session == null
        ) {

            return false;
        }


        if (
                session.getStatus()
                        !=
                        SessionStatus.ACTIVE
        ) {

            return false;
        }


        if (
                session.getExpiresAt()
                        == null
                        ||
                        !session.getExpiresAt()
                                .isAfter(
                                        LocalDateTime.now()
                                )
        ) {

            return false;
        }


        User sessionUser =
                session.getUser();


        return
                sessionUser != null
                        &&
                        sessionUser.getEmail() != null
                        &&
                        sessionUser.getEmail()
                                .equalsIgnoreCase(
                                        email
                                );
    }


    // =========================================================
    // HEARTBEAT GAP
    //
    // Frontend yaklaşık 60 saniyede bir heartbeat gönderecek.
    //
    // 120 saniyeden büyük boşluğu aktif süre saymıyoruz.
    //
    // Örnek:
    //
    // Kullanıcı siteyi açık bırakıp 2 saat giderse,
    // sisteme 2 saat aktif yazılmaz.
    // =========================================================

    private static final long
            MAX_ACTIVE_GAP_SECONDS =
            120;


    private final UserSessionRepository
            userSessionRepository;


    // =========================================================
    // CREATE SESSION
    // =========================================================

    @Transactional
    public UserSession createSession(

            User user,

            long expirationMs
    ) {


        LocalDateTime now =
                LocalDateTime.now();


        UserSession session =
                new UserSession();


        session.setSessionId(

                UUID.randomUUID()
                        .toString()
        );


        session.setUser(
                user
        );


        session.setRole(
                user.getRole()
        );


        session.setLoginAt(
                now
        );


        session.setLastSeenAt(
                now
        );


        session.setActiveSeconds(
                0
        );


        session.setExpiresAt(

                now.plus(

                        Duration.ofMillis(
                                expirationMs
                        )
                )
        );


        session.setStatus(
                SessionStatus.ACTIVE
        );


        return userSessionRepository.save(
                session
        );
    }


    // =========================================================
    // HEARTBEAT
    // =========================================================

    @Transactional
    public void heartbeat(
            String sessionId
    ) {


        UserSession session =

                userSessionRepository
                        .findBySessionIdForUpdate(
                                sessionId
                        )

                        .orElseThrow(
                                () ->

                                        new ResponseStatusException(

                                                HttpStatus.UNAUTHORIZED,

                                                "Oturum bulunamadı."
                                        )
                        );


        LocalDateTime now =
                LocalDateTime.now();


        // =====================================================
        // EXPIRED
        // =====================================================

        if (
                !session
                        .getExpiresAt()
                        .isAfter(
                                now
                        )
        ) {


            session.setStatus(
                    SessionStatus.EXPIRED
            );


            userSessionRepository.save(
                    session
            );


            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "Oturumun süresi dolmuş."
            );
        }


        // =====================================================
        // NOT ACTIVE
        // =====================================================

        if (
                session.getStatus()
                        !=
                        SessionStatus.ACTIVE
        ) {


            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "Oturum aktif değil."
            );
        }


        // =====================================================
        // ACTIVE TIME
        // =====================================================

        addActiveTime(

                session,

                now
        );


        session.setLastSeenAt(
                now
        );


        userSessionRepository.save(
                session
        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @Transactional
    public void logout(
            String sessionId
    ) {


        if (
                sessionId == null
                        ||
                        sessionId.isBlank()
        ) {

            return;
        }


        UserSession session =

                userSessionRepository
                        .findBySessionIdForUpdate(
                                sessionId
                        )

                        .orElse(
                                null
                        );


        if (
                session == null
        ) {

            return;
        }


        if (
                session.getStatus()
                        !=
                        SessionStatus.ACTIVE
        ) {

            return;
        }


        LocalDateTime now =
                LocalDateTime.now();


        addActiveTime(

                session,

                now
        );


        session.setLastSeenAt(
                now
        );


        session.setLogoutAt(
                now
        );


        session.setStatus(
                SessionStatus.LOGGED_OUT
        );


        userSessionRepository.save(
                session
        );
    }


    // =========================================================
    // ACTIVE TIME CALCULATION
    // =========================================================

    private void addActiveTime(

            UserSession session,

            LocalDateTime now
    ) {


        if (
                session.getLastSeenAt()
                        == null
        ) {


            return;
        }


        long gapSeconds =

                Duration
                        .between(

                                session.getLastSeenAt(),

                                now
                        )

                        .getSeconds();


        // =====================================================
        // SADECE MAKUL HEARTBEAT ARALIĞINI SAY
        // =====================================================

        if (
                gapSeconds <= 0
                        ||
                        gapSeconds
                                >
                                MAX_ACTIVE_GAP_SECONDS
        ) {


            return;
        }


        session.setActiveSeconds(

                session.getActiveSeconds()
                        +
                        gapSeconds
        );
    }
}