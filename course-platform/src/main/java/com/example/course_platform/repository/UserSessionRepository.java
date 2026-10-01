package com.example.course_platform.repository;


import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.SessionStatus;
import com.example.course_platform.entity.UserSession;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserSessionRepository
        extends JpaRepository<UserSession, Long> {


    // =========================================================
    // SESSION ID
    // =========================================================

    Optional<UserSession>
    findBySessionId(
            String sessionId
    );


    // =========================================================
    // ADMIN HISTORY
    // =========================================================

    List<UserSession>
    findAllByOrderByLoginAtDesc();


    // =========================================================
    // ADMIN - RECENT ACTIVITY
    // =========================================================

    List<UserSession>
    findTop8ByRoleInOrderByLoginAtDesc(

            Collection<Role> roles
    );


    // =========================================================
    // TODAY LOGIN COUNT
    // =========================================================

    long countByRoleInAndLoginAtGreaterThanEqual(

            Collection<Role> roles,

            LocalDateTime start
    );


    // =========================================================
    // UNIQUE ONLINE USERS
    // =========================================================

    @Query("""
            SELECT COUNT(DISTINCT session.user.id)
            FROM UserSession session
            WHERE session.role IN :roles
            AND session.status = :status
            AND session.lastSeenAt >= :threshold
            """)
    long countDistinctOnlineUsers(

            @Param("roles")
            Collection<Role> roles,

            @Param("status")
            SessionStatus status,

            @Param("threshold")
            LocalDateTime threshold
    );


    // =========================================================
    // UNIQUE ONLINE USERS BY ROLE
    // =========================================================

    @Query("""
            SELECT COUNT(DISTINCT session.user.id)
            FROM UserSession session
            WHERE session.role = :role
            AND session.status = :status
            AND session.lastSeenAt >= :threshold
            """)
    long countDistinctOnlineUsersByRole(

            @Param("role")
            Role role,

            @Param("status")
            SessionStatus status,

            @Param("threshold")
            LocalDateTime threshold
    );


    // =========================================================
    // AVERAGE ACTIVE TIME
    // =========================================================

    @Query("""
            SELECT AVG(session.activeSeconds)
            FROM UserSession session
            WHERE session.role IN :roles
            """)
    Double averageActiveSeconds(

            @Param("roles")
            Collection<Role> roles
    );


    // =========================================================
    // UPDATE LOCK
    //
    // Birden fazla tab aynı anda heartbeat gönderirse
    // activeSeconds iki kez eklenmesin.
    // =========================================================

    @Lock(
            LockModeType.PESSIMISTIC_WRITE
    )
    @Query("""
            SELECT session
            FROM UserSession session
            WHERE session.sessionId = :sessionId
            """)
    Optional<UserSession>
    findBySessionIdForUpdate(

            @Param("sessionId")
            String sessionId
    );
}