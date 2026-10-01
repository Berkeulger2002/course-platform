package com.example.course_platform.service;


import com.example.course_platform.dto.AdminSessionSummaryResponse;
import com.example.course_platform.dto.AdminUserSessionResponse;

import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.SessionStatus;
import com.example.course_platform.entity.UserSession;

import com.example.course_platform.repository.UserSessionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminSessionService {


    // =========================================================
    // ONLINE LIMIT
    //
    // Heartbeat 60 saniyede bir geliyor.
    // Son 2 dakika içinde heartbeat varsa online kabul ediyoruz.
    // =========================================================

    private static final long
            ONLINE_THRESHOLD_MINUTES =
            2;


    private final UserSessionRepository
            userSessionRepository;


    // =========================================================
    // ALL STUDENT + TEACHER SESSIONS
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public List<AdminUserSessionResponse>
    getSessions() {


        LocalDateTime now =
                LocalDateTime.now();


        return userSessionRepository
                .findAllByOrderByLoginAtDesc()

                .stream()

                // =================================================
                // ADMIN OTURUMLARINI BU EKRANDA GÖSTERME
                // =================================================

                .filter(
                        session ->

                                session.getRole()
                                        ==
                                        Role.STUDENT

                                        ||

                                        session.getRole()
                                                ==
                                                Role.TEACHER
                )

                .map(
                        session ->

                                mapSession(
                                        session,
                                        now
                                )
                )

                .toList();
    }


    // =========================================================
    // SUMMARY
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public AdminSessionSummaryResponse
    getSummary() {


        LocalDateTime now =
                LocalDateTime.now();


        LocalDateTime todayStart =

                LocalDate
                        .now()
                        .atStartOfDay();


        List<UserSession> sessions =

                userSessionRepository
                        .findAllByOrderByLoginAtDesc()

                        .stream()

                        .filter(
                                session ->

                                        session.getRole()
                                                ==
                                                Role.STUDENT

                                                ||

                                                session.getRole()
                                                        ==
                                                        Role.TEACHER
                        )

                        .toList();


        // =====================================================
        // TOTAL
        // =====================================================

        long totalSessions =
                sessions.size();


        // =====================================================
        // ONLINE UNIQUE USERS
        // =====================================================

        long onlineNow =

                sessions
                        .stream()

                        .filter(
                                session ->

                                        isOnline(
                                                session,
                                                now
                                        )
                        )

                        .map(
                                session ->

                                        session
                                                .getUser()
                                                .getId()
                        )

                        .distinct()

                        .count();


        // =====================================================
        // ONLINE STUDENTS
        // =====================================================

        long onlineStudents =

                sessions
                        .stream()

                        .filter(
                                session ->

                                        session.getRole()
                                                ==
                                                Role.STUDENT
                        )

                        .filter(
                                session ->

                                        isOnline(
                                                session,
                                                now
                                        )
                        )

                        .map(
                                session ->

                                        session
                                                .getUser()
                                                .getId()
                        )

                        .distinct()

                        .count();


        // =====================================================
        // ONLINE TEACHERS
        // =====================================================

        long onlineTeachers =

                sessions
                        .stream()

                        .filter(
                                session ->

                                        session.getRole()
                                                ==
                                                Role.TEACHER
                        )

                        .filter(
                                session ->

                                        isOnline(
                                                session,
                                                now
                                        )
                        )

                        .map(
                                session ->

                                        session
                                                .getUser()
                                                .getId()
                        )

                        .distinct()

                        .count();


        // =====================================================
        // TODAY LOGINS
        // =====================================================

        long todayLogins =

                sessions
                        .stream()

                        .filter(
                                session ->

                                        session.getLoginAt()
                                                !=
                                                null

                                                &&

                                                !session
                                                        .getLoginAt()
                                                        .isBefore(
                                                                todayStart
                                                        )
                        )

                        .count();


        // =====================================================
        // AVERAGE ACTIVE TIME
        // =====================================================

        long averageActiveSeconds =

                sessions.isEmpty()

                        ?

                        0

                        :

                        Math.round(

                                sessions
                                        .stream()

                                        .mapToLong(
                                                UserSession::getActiveSeconds
                                        )

                                        .average()

                                        .orElse(
                                                0
                                        )
                        );


        return new AdminSessionSummaryResponse(

                totalSessions,

                onlineNow,

                onlineStudents,

                onlineTeachers,

                todayLogins,

                averageActiveSeconds
        );
    }


    // =========================================================
    // MAP
    // =========================================================

    private AdminUserSessionResponse
    mapSession(

            UserSession session,

            LocalDateTime now
    ) {


        boolean online =

                isOnline(
                        session,
                        now
                );


        String effectiveStatus;


        if (
                online
        ) {


            effectiveStatus =
                    "ONLINE";


        } else if (
                session.getStatus()
                        ==
                        SessionStatus.LOGGED_OUT
        ) {


            effectiveStatus =
                    "LOGGED_OUT";


        } else if (
                session.getStatus()
                        ==
                        SessionStatus.EXPIRED
        ) {


            effectiveStatus =
                    "EXPIRED";


        } else {


            effectiveStatus =
                    "OFFLINE";
        }


        return new AdminUserSessionResponse(

                session.getId(),

                session
                        .getUser()
                        .getId(),

                session
                        .getUser()
                        .getName(),

                session
                        .getUser()
                        .getEmail(),

                session
                        .getRole()
                        .name(),

                session.getLoginAt(),

                session.getLastSeenAt(),

                session.getLogoutAt(),

                session.getActiveSeconds(),

                effectiveStatus,

                online
        );
    }


    // =========================================================
    // ONLINE CHECK
    // =========================================================

    private boolean isOnline(

            UserSession session,

            LocalDateTime now
    ) {


        if (
                session.getStatus()
                        !=
                        SessionStatus.ACTIVE
        ) {


            return false;
        }


        if (
                session.getLastSeenAt()
                        ==
                        null
        ) {


            return false;
        }


        LocalDateTime threshold =

                now.minusMinutes(
                        ONLINE_THRESHOLD_MINUTES
                );


        return !session
                .getLastSeenAt()
                .isBefore(
                        threshold
                );
    }
}