package com.example.course_platform.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "user_sessions"
)
@Getter
@Setter
@NoArgsConstructor
public class UserSession {


    // =========================================================
    // ID
    // =========================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    // =========================================================
    // SESSION UUID
    //
    // JWT içerisindeki "sid" claim'i ile eşleşir.
    // =========================================================

    @Column(
            name = "session_id",
            nullable = false,
            unique = true,
            length = 36
    )
    private String sessionId;


    // =========================================================
    // USER
    // =========================================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    // =========================================================
    // ROLE SNAPSHOT
    // =========================================================

    @Enumerated(
            EnumType.STRING
    )
    @Column(
            nullable = false,
            length = 20
    )
    private Role role;


    // =========================================================
    // LOGIN
    // =========================================================

    @Column(
            name = "login_at",
            nullable = false
    )
    private LocalDateTime loginAt;


    // =========================================================
    // LAST ACTIVITY
    // =========================================================

    @Column(
            name = "last_seen_at",
            nullable = false
    )
    private LocalDateTime lastSeenAt;


    // =========================================================
    // LOGOUT
    // =========================================================

    @Column(
            name = "logout_at"
    )
    private LocalDateTime logoutAt;


    // =========================================================
    // REAL ACTIVE TIME
    //
    // Login -> logout farkı değildir.
    // Heartbeat'lerden hesaplanan aktif saniyedir.
    // =========================================================

    @Column(
            name = "active_seconds",
            nullable = false
    )
    private long activeSeconds = 0;


    // =========================================================
    // JWT / SESSION EXPIRATION
    // =========================================================

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;


    // =========================================================
    // STATUS
    // =========================================================

    @Enumerated(
            EnumType.STRING
    )
    @Column(
            nullable = false,
            length = 20
    )
    private SessionStatus status;
}