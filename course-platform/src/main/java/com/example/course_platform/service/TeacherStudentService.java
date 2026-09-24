package com.example.course_platform.service;

import com.example.course_platform.dto.TeacherStudentResponse;

import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.CourseProgress;
import com.example.course_platform.entity.Student;

import com.example.course_platform.repository.CourseProgressRepository;
import com.example.course_platform.repository.CourseRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class TeacherStudentService {

    private final CourseRepository courseRepository;

    private final CourseProgressRepository progressRepository;

    private final CurrentUserService currentUserService;


    // =========================================================
    // ÖĞRETMENİN TÜM ÖĞRENCİ KAYITLARI
    // =========================================================

    @Transactional(readOnly = true)
    public List<TeacherStudentResponse> getTeacherStudents(
            Long teacherId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        //
        // JWT'deki kullanıcı TEACHER olmalı.
        //
        // URL:
        // /teacher-students/teacher/{teacherId}
        //
        // içindeki teacherId ile JWT'deki gerçek öğretmen
        // ID'si aynı olmak zorunda.
        // =====================================================

        currentUserService.requireTeacher(
                teacherId
        );


        // =====================================================
        // SADECE BU ÖĞRETMENİN KURSLARI
        // =====================================================

        List<Course> courses =
                courseRepository
                        .findAllByTeacherId(
                                teacherId
                        );


        List<TeacherStudentResponse> result =
                new ArrayList<>();


        /*
         * Aynı öğrenci öğretmenin birden fazla kursuna
         * kayıtlı olabilir.
         *
         * Bu ekran öğrenci-kurs kayıtlarını gösterdiği için
         * her kurs kaydını ayrı satır olarak koruyoruz.
         */
        for (
                Course course :
                courses
        ) {

            for (
                    Student student :
                    course.getStudents()
            ) {

                result.add(
                        createResponse(
                                student,
                                course
                        )
                );
            }
        }


        return result;
    }


    // =========================================================
    // BELİRLİ BİR KURSUN ÖĞRENCİLERİ
    // =========================================================

    @Transactional(readOnly = true)
    public List<TeacherStudentResponse> getCourseStudents(
            Long teacherId,
            Long courseId) {


        // =====================================================
        // 1. JWT -> TEACHER ID KONTROLÜ
        //
        // Başka öğretmenin teacherId değeri URL'ye
        // yazılamaz.
        // =====================================================

        currentUserService.requireTeacher(
                teacherId
        );


        // =====================================================
        // 2. KURSU BUL
        // =====================================================

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
        // 3. KURS GERÇEKTEN BU ÖĞRETMENE Mİ AİT?
        //
        // Teacher 1 kendi ID'sini kullanıyor olsa bile
        // Teacher 2'nin courseId değerini yazarak öğrencileri
        // göremez.
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
                    "Bu kursun öğrencilerini görüntüleme yetkiniz yok."
            );
        }


        return course
                .getStudents()
                .stream()
                .map(
                        student ->
                                createResponse(
                                        student,
                                        course
                                )
                )
                .toList();
    }


    // =========================================================
    // RESPONSE OLUŞTUR
    // =========================================================

    private TeacherStudentResponse createResponse(
            Student student,
            Course course) {


        CourseProgress progress =
                progressRepository
                        .findByStudentIdAndCourseId(
                                student.getId(),
                                course.getId()
                        )
                        .orElse(
                                null
                        );


        return new TeacherStudentResponse(

                student.getId(),

                student.getName(),

                student.getEmail(),

                course.getId(),

                course.getName(),

                progress != null
                        ?
                        progress.getProgressPercentage()
                        :
                        0,

                progress != null
                        &&
                        progress.isCompleted(),

                progress != null
                        ?
                        progress.getLastWatchedAt()
                        :
                        null
        );
    }
}