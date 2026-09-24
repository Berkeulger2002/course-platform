package com.example.course_platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification extends BaseEntity {


    // =========================================================
    // ÖĞRENCİ ALICI
    //
    // Öğrenci bildirimi ise student dolu olur.
    // Öğretmen bildirimi ise null olabilir.
    // =========================================================

    @ManyToOne
    @JoinColumn(
            name = "student_id"
    )
    private Student student;


    // =========================================================
    // ÖĞRETMEN ALICI
    //
    // Öğretmen bildirimi ise teacher dolu olur.
    // Öğrenci bildirimi ise null olabilir.
    // =========================================================

    @ManyToOne
    @JoinColumn(
            name = "teacher_id"
    )
    private Teacher teacher;


    // =========================================================
    // BAŞLIK
    // =========================================================

    @Column(
            nullable = false,
            length = 200
    )
    private String title;


    // =========================================================
    // MESAJ
    // =========================================================

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;


    // =========================================================
    // TİP
    //
    // Örnekler:
    //
    // CERTIFICATE
    // PAYMENT
    // COURSE
    //
    // SALE
    // REVIEW
    // STUDENT
    // COURSE_COMPLETED
    // =========================================================

    @Column(
            nullable = false,
            length = 50
    )
    private String type;


    // =========================================================
    // OKUNDU MU?
    // =========================================================

    @Column(name = "is_read")
    private boolean read = false;


    // =========================================================
    // TARİH
    // =========================================================

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;
}