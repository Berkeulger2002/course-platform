package com.example.course_platform.repository;

import com.example.course_platform.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {


    // =========================================================
    // ÖĞRENCİ BİLDİRİMLERİ
    // =========================================================

    List<Notification>
    findAllByStudentIdOrderByCreatedAtDesc(
            Long studentId
    );


    long countByStudentIdAndReadFalse(
            Long studentId
    );


    boolean existsByStudentIdAndTypeAndMessage(
            Long studentId,
            String type,
            String message
    );


    // =========================================================
    // ÖĞRETMEN BİLDİRİMLERİ
    // =========================================================

    List<Notification>
    findAllByTeacherIdOrderByCreatedAtDesc(
            Long teacherId
    );


    long countByTeacherIdAndReadFalse(
            Long teacherId
    );


    boolean existsByTeacherIdAndTypeAndMessage(
            Long teacherId,
            String type,
            String message
    );
}