package com.example.course_platform.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "teacher_registration_requests"
)
@Getter
@Setter
@NoArgsConstructor
public class TeacherRegistrationRequest {


    // =========================================================
    // ID
    // =========================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    // =========================================================
    // AD SOYAD
    // =========================================================

    @Column(
            nullable = false
    )
    private String name;


    // =========================================================
    // EMAIL
    // =========================================================

    @Column(
            nullable = false
    )
    private String email;


    // =========================================================
    // DURUM
    // =========================================================

    @Enumerated(
            EnumType.STRING
    )
    @Column(
            nullable = false,
            length = 30
    )
    private TeacherRegistrationStatus status;


    // =========================================================
    // DOĞRULAMA KODUNUN HASH DEĞERİ
    //
    // Kodu düz metin olarak DB'de saklamayacağız.
    // =========================================================

    @Column(
            name = "verification_code_hash"
    )
    private String verificationCodeHash;


    // =========================================================
    // KOD SON KULLANMA ZAMANI
    // =========================================================

    @Column(
            name = "code_expires_at"
    )
    private LocalDateTime codeExpiresAt;


    // =========================================================
    // BAŞVURU TARİHİ
    // =========================================================

    @Column(
            name = "requested_at",
            nullable = false
    )
    private LocalDateTime requestedAt;


    // =========================================================
    // KOD GÖNDERİLME TARİHİ
    // =========================================================

    @Column(
            name = "code_sent_at"
    )
    private LocalDateTime codeSentAt;


    // =========================================================
    // RED TARİHİ
    // =========================================================

    @Column(
            name = "rejected_at"
    )
    private LocalDateTime rejectedAt;


    // =========================================================
    // KAYIT TAMAMLAMA TARİHİ
    // =========================================================

    @Column(
            name = "completed_at"
    )
    private LocalDateTime completedAt;
}