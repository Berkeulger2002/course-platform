package com.example.course_platform.service;


import com.example.course_platform.dto.AdminUserActivityResponse;
import com.example.course_platform.dto.AdminUserActivitySummaryResponse;

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
public class AdminUserActivityService {


    // =========================================================
    // TRACKED ROLES
    // =========================================================

    private static final List<Role>
            TRACKED_ROLES =
            List.of(

                    Role.STUDENT,

                    Role.TEACHER
            );


    // =========================================================
    // ONLINE THRESHOLD
    //
    // Heartbeat yaklaşık 60 saniyede bir.
    // Son heartbeat 2 dakikadan eski değilse online.
    // =========================================================

    private static final long
            ONLINE_THRESHOLD_SECONDS =
            120;


    private final UserSessionRepository
            userSessionRepository;


    // =========================================================
    // SUMMARY
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public AdminUserActivitySummaryResponse
    getSummary() {


        LocalDateTime now =
                LocalDateTime.now();


        LocalDateTime onlineThreshold =

                now.minusSeconds(
                        ONLINE_THRESHOLD_SECONDS
                );


        LocalDateTime todayStart =

                LocalDate
                        .now()
                        .atStartOfDay();


        long onlineNow =

                userSessionRepository
                        .countDistinctOnlineUsers(

                                TRACKED_ROLES,

                                SessionStatus.ACTIVE,

                                onlineThreshold
                        );


        long onlineStudents =

                userSessionRepository
                        .countDistinctOnlineUsersByRole(

                                Role.STUDENT,

                                SessionStatus.ACTIVE,

                                onlineThreshold
                        );


        long onlineTeachers =

                userSessionRepository
                        .countDistinctOnlineUsersByRole(

                                Role.TEACHER,

                                SessionStatus.ACTIVE,

                                onlineThreshold
                        );


        long todayLogins =

                userSessionRepository
                        .countByRoleInAndLoginAtGreaterThanEqual(

                                TRACKED_ROLES,

                                todayStart
                        );


        Double averageValue =

                userSessionRepository
                        .averageActiveSeconds(
                                TRACKED_ROLES
                        );


        long averageActiveSeconds =

                averageValue == null

                        ?

                        0

                        :

                        Math.round(
                                averageValue
                        );


        return new AdminUserActivitySummaryResponse(

                onlineNow,

                onlineStudents,

                onlineTeachers,

                todayLogins,

                averageActiveSeconds
        );
    }


    // =========================================================
    // RECENT ACTIVITIES
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public List<AdminUserActivityResponse>
    getRecentActivities() {


        LocalDateTime now =
                LocalDateTime.now();


        LocalDateTime onlineThreshold =

                now.minusSeconds(
                        ONLINE_THRESHOLD_SECONDS
                );


        return userSessionRepository

                .findTop8ByRoleInOrderByLoginAtDesc(
                        TRACKED_ROLES
                )

                .stream()

                .map(

                        session ->

                                mapSession(

                                        session,

                                        onlineThreshold
                                )
                )

                .toList();
    }


    // =========================================================
    // MAP
    // =========================================================

    private AdminUserActivityResponse
    mapSession(

            UserSession session,

            LocalDateTime onlineThreshold
    ) {


        boolean online =

                session.getStatus()
                        ==
                        SessionStatus.ACTIVE

                        &&

                        session.getLastSeenAt()
                                !=
                                null

                        &&

                        !session
                                .getLastSeenAt()
                                .isBefore(
                                        onlineThreshold
                                );


        String status;


        if (
                online
        ) {


            status =
                    "ONLINE";


        } else if (
                session.getStatus()
                        ==
                        SessionStatus.ACTIVE
        ) {


            status =
                    "OFFLINE";


        } else {


            status =
                    session.getStatus()
                            .name();
        }


        return new AdminUserActivityResponse(

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

                status,

                online
        );
    }
}