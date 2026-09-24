package com.example.course_platform.repository;

import com.example.course_platform.entity.PaymentHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentHistoryRepository
        extends JpaRepository<PaymentHistory, Long> {


    // =========================================================
    // ÖĞRENCİNİN ÖDEME GEÇMİŞİ
    // =========================================================

    List<PaymentHistory>
    findAllByStudentIdOrderByPurchasedAtDesc(
            Long studentId
    );


    // =========================================================
    // ÖĞRENCİ + KURS
    // =========================================================

    Optional<PaymentHistory>
    findByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );


    // =========================================================
    // AYNI KURS İÇİN ÖDEME VAR MI?
    // =========================================================

    boolean existsByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );


    // =========================================================
    // ÖĞRETMENİN TÜM SATIŞ / ÖDEME KAYITLARI
    //
    // PaymentHistory
    //      -> Course
    //          -> Teacher
    //
    // ilişkisi üzerinden öğretmenin kayıtlarını getiriyoruz.
    // =========================================================

    @Query("""
            SELECT p
            FROM PaymentHistory p
            WHERE p.course.teacher.id = :teacherId
            ORDER BY p.purchasedAt DESC
            """)
    List<PaymentHistory> findAllByTeacherId(
            @Param("teacherId")
            Long teacherId
    );


    // =========================================================
    // ÖĞRETMENİN BELİRLİ BİR KURSUNUN SATIŞLARI
    // =========================================================

    @Query("""
            SELECT p
            FROM PaymentHistory p
            WHERE p.course.teacher.id = :teacherId
            AND p.course.id = :courseId
            ORDER BY p.purchasedAt DESC
            """)
    List<PaymentHistory> findAllByTeacherIdAndCourseId(
            @Param("teacherId")
            Long teacherId,

            @Param("courseId")
            Long courseId
    );
}