package com.example.course_platform.service;

import com.example.course_platform.document.CourseReview;

import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.Student;

import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.CourseReviewRepository;
import com.example.course_platform.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class CourseReviewService {

    private final CourseReviewRepository reviewRepository;

    private final CourseRepository courseRepository;

    private final StudentRepository studentRepository;

    private final NotificationService notificationService;

    private final CurrentUserService currentUserService;


    // =========================================================
    // YENİ YORUM / DEĞERLENDİRME EKLE
    // =========================================================

    public CourseReview addReview(
            CourseReview incomingReview) {


        // =====================================================
        // 1. GİRİŞ YAPAN KULLANICI STUDENT OLMALI
        // =====================================================

        currentUserService.requireStudentRole();


        Long studentId =
                currentUserService
                        .getCurrentUserId();


        Student student =
                studentRepository
                        .findById(
                                studentId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğrenci bulunamadı."
                                )
                        );


        // =====================================================
        // 2. COURSE ID KONTROLÜ
        // =====================================================

        if (
                incomingReview.getCourseId() == null
        ) {

            throw new IllegalArgumentException(
                    "Kurs bilgisi zorunludur."
            );
        }


        Course course =
                courseRepository
                        .findById(
                                incomingReview.getCourseId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Değerlendirme yapılacak kurs bulunamadı."
                                )
                        );


        // =====================================================
        // 3. ÖĞRENCİ KURSA GERÇEKTEN SAHİP Mİ?
        //
        // Kursu satın almayan öğrenci yorum yapamaz.
        // =====================================================

        boolean enrolled =
                student
                        .getEnrolledCourses()
                        .stream()
                        .anyMatch(
                                enrolledCourse ->
                                        Objects.equals(
                                                enrolledCourse.getId(),
                                                course.getId()
                                        )
                        );


        if (
                !enrolled
        ) {

            throw new AccessDeniedException(
                    "Yalnızca kayıtlı olduğunuz kursları değerlendirebilirsiniz."
            );
        }


        // =====================================================
        // 4. PUAN KONTROLÜ
        // =====================================================

        if (
                incomingReview.getRating() < 1
                        ||
                        incomingReview.getRating() > 5
        ) {

            throw new IllegalArgumentException(
                    "Puan 1 ile 5 arasında olmalıdır."
            );
        }


        // =====================================================
        // 5. YORUM KONTROLÜ
        // =====================================================

        String comment =
                incomingReview.getComment() == null
                        ?
                        ""
                        :
                        incomingReview
                                .getComment()
                                .trim();


        if (
                comment.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Yorum boş bırakılamaz."
            );
        }


        if (
                comment.length() > 2000
        ) {

            throw new IllegalArgumentException(
                    "Yorum en fazla 2000 karakter olabilir."
            );
        }


        // =====================================================
        // 6. GÜVENLİ YENİ DOCUMENT OLUŞTUR
        //
        // Client'tan gelen:
        //
        // id
        // studentId
        // studentName
        // createdAt
        //
        // DEĞERLERİNİN HİÇBİRİNİ KULLANMIYORUZ.
        //
        // Böylece başka öğrenci adına yorum ve
        // Mongo ID üzerinden overwrite engellenir.
        // =====================================================

        CourseReview review =
                new CourseReview();


        review.setCourseId(
                course.getId()
        );


        review.setStudentId(
                student.getId()
        );


        review.setStudentName(
                student.getName()
        );


        review.setComment(
                comment
        );


        review.setRating(
                incomingReview.getRating()
        );


        review.setCreatedAt(
                LocalDateTime.now()
        );


        // =====================================================
        // 7. MONGODB'YE KAYDET
        // =====================================================

        CourseReview savedReview =
                reviewRepository.save(
                        review
                );


        // =====================================================
        // 8. ÖĞRETMENE BİLDİRİM
        // =====================================================

        if (
                course.getTeacher() != null
        ) {

            Long teacherId =
                    course
                            .getTeacher()
                            .getId();


            String message =
                    "\""
                            +
                            course.getName()
                            +
                            "\" kursunuza yeni bir değerlendirme geldi.";


            notificationService
                    .createTeacherNotification(
                            teacherId,
                            "Yeni Değerlendirme Geldi ⭐",
                            message,
                            "REVIEW"
                    );
        }


        return savedReview;
    }


    // =========================================================
    // BİR KURSA AİT TÜM YORUMLAR
    //
    // GET endpointi public kalabilir.
    // =========================================================

    public List<CourseReview>
    getReviewsByCourse(
            Long courseId) {

        return reviewRepository
                .findByCourseId(
                        courseId
                );
    }
}