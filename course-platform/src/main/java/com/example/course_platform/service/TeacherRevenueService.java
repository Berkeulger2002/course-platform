package com.example.course_platform.service;

import com.example.course_platform.dto.TeacherRevenueResponse;

import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.PaymentHistory;

import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.PaymentHistoryRepository;
import com.example.course_platform.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class TeacherRevenueService {

    private final PaymentHistoryRepository paymentHistoryRepository;

    private final TeacherRepository teacherRepository;

    private final CourseRepository courseRepository;

    private final CurrentUserService currentUserService;


    // =========================================================
    // ÖĞRETMENİN TÜM SATIŞLARI
    // =========================================================

    @Transactional(readOnly = true)
    public List<TeacherRevenueResponse>
    getTeacherPayments(
            Long teacherId) {


        // =====================================================
        // IDOR / OWNERSHIP
        //
        // JWT'deki öğretmen ile URL'deki teacherId
        // aynı olmak zorunda.
        // =====================================================

        currentUserService.requireTeacher(
                teacherId
        );


        teacherRepository
                .findById(
                        teacherId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Öğretmen bulunamadı."
                        )
                );


        return paymentHistoryRepository
                .findAllByTeacherId(
                        teacherId
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }


    // =========================================================
    // ÖĞRETMENİN BELİRLİ KURSUNUN SATIŞLARI
    // =========================================================

    @Transactional(readOnly = true)
    public List<TeacherRevenueResponse>
    getCoursePayments(
            Long teacherId,
            Long courseId) {


        // =====================================================
        // ÖNCE JWT -> TEACHER ID KONTROLÜ
        // =====================================================

        currentUserService.requireTeacher(
                teacherId
        );


        Course course =
                courseRepository
                        .findById(
                                courseId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Kurs bulunamadı."
                                )
                        );


        // =====================================================
        // SONRA KURS -> TEACHER OWNERSHIP KONTROLÜ
        // =====================================================

        if (
                course.getTeacher() == null
                        ||
                        !Objects.equals(
                                course
                                        .getTeacher()
                                        .getId(),
                                teacherId
                        )
        ) {

            throw new AccessDeniedException(
                    "Bu kursun satış bilgilerini görüntüleme yetkiniz yok."
            );
        }


        return paymentHistoryRepository
                .findAllByTeacherIdAndCourseId(
                        teacherId,
                        courseId
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }


    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private TeacherRevenueResponse toResponse(
            PaymentHistory payment) {

        return new TeacherRevenueResponse(

                payment.getId(),

                payment
                        .getStudent()
                        .getId(),

                payment
                        .getStudent()
                        .getName(),

                payment
                        .getStudent()
                        .getEmail(),

                payment
                        .getCourse()
                        .getId(),

                payment.getCourseName(),

                payment.getAmount(),

                payment.getStatus(),

                payment.getPaymentMethod(),

                payment.getTransactionCode(),

                payment.getPurchasedAt()
        );
    }
}