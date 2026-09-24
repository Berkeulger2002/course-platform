package com.example.course_platform.service;

import com.example.course_platform.dto.CourseProgressResponse;
import com.example.course_platform.dto.CourseProgressUpdateRequest;

import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.CourseProgress;
import com.example.course_platform.entity.Student;

import com.example.course_platform.repository.CourseProgressRepository;
import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class CourseProgressService {

    private final CourseProgressRepository progressRepository;

    private final StudentRepository studentRepository;

    private final CourseRepository courseRepository;

    private final CurrentUserService currentUserService;


    /*
     * Kurs tamamlandığında sertifika oluşturmak için.
     */
    private final CertificateService certificateService;


    /*
     * Kurs tamamlandığında öğretmene
     * bildirim göndermek için.
     */
    private final NotificationService notificationService;


    // =========================================================
    // İLERLEME GÜNCELLE
    // =========================================================

    @Transactional
    public CourseProgressResponse updateProgress(
            Long studentId,
            Long courseId,
            CourseProgressUpdateRequest request) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        //
        // URL'deki studentId gerçekten giriş yapan
        // öğrenciye mi ait?
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğrenci bulunamadı."
                                )
                        );


        Course course =
                courseRepository
                        .findById(courseId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Kurs bulunamadı."
                                )
                        );


        // =====================================================
        // ÖĞRENCİ BU KURSA KAYITLI MI?
        // =====================================================

        requireEnrollment(
                student,
                courseId,
                "Bu kurs için ilerleme kaydı oluşturamazsınız."
        );


        // =====================================================
        // VARSA ESKİ PROGRESS KAYDINI AL
        // YOKSA YENİ OLUŞTUR
        // =====================================================

        CourseProgress progress =
                progressRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElseGet(() -> {

                            CourseProgress newProgress =
                                    new CourseProgress();


                            newProgress.setStudent(
                                    student
                            );


                            newProgress.setCourse(
                                    course
                            );


                            return newProgress;
                        });


        /*
         * Daha önce tamamlanmış mıydı?
         *
         * Böylece tamamlama anını anlayabiliriz.
         */
        boolean wasAlreadyCompleted =
                progress.isCompleted();


        // =====================================================
        // GELEN VIDEO SÜRELERİNİ KONTROL ET
        // =====================================================

        double currentTime =
                Math.max(
                        request.currentTime(),
                        0
                );


        double duration =
                Math.max(
                        request.duration(),
                        0
                );


        progress.setLastPositionSeconds(
                currentTime
        );


        // =====================================================
        // EN İLERİ ULAŞILAN NOKTAYI KORU
        // =====================================================

        if (
                currentTime >
                        progress.getMaxPositionSeconds()
        ) {

            progress.setMaxPositionSeconds(
                    currentTime
            );
        }


        // =====================================================
        // İLERLEME YÜZDESİNİ HESAPLA
        // =====================================================

        if (
                duration > 0
        ) {

            progress.setDurationSeconds(
                    duration
            );


            double percentage =
                    (
                            progress.getMaxPositionSeconds()
                                    /
                                    duration
                    )
                            *
                            100;


            percentage =
                    Math.min(
                            percentage,
                            100
                    );


            progress.setProgressPercentage(
                    percentage
            );


            /*
             * Öğrenci videonun en az %95'ine
             * ulaştıysa kurs tamamlandı.
             */
            progress.setCompleted(
                    percentage >= 95
            );
        }


        // =====================================================
        // SON İZLEME ZAMANI
        // =====================================================

        progress.setLastWatchedAt(
                LocalDateTime.now()
        );


        // =====================================================
        // PROGRESS KAYDET
        // =====================================================

        CourseProgress saved =
                progressRepository.save(
                        progress
                );


        // =====================================================
        // KURS TAMAMLANDI
        // =====================================================

        if (
                saved.isCompleted()
        ) {

            // =================================================
            // ÖĞRENCİ SERTİFİKASI
            // =================================================

            certificateService
                    .createCertificateIfNecessary(
                            studentId,
                            courseId
                    );


            // =================================================
            // ÖĞRETMENE TAMAMLAMA BİLDİRİMİ
            //
            // Sadece kurs ilk kez tamamlandığında
            // bildirim gönderilir.
            // =================================================

            if (
                    !wasAlreadyCompleted
                            &&
                            course.getTeacher() != null
            ) {

                Long teacherId =
                        course
                                .getTeacher()
                                .getId();


                String message =
                        student.getName()
                                +
                                ", \""
                                +
                                course.getName()
                                +
                                "\" kursunuzu başarıyla tamamladı.";


                notificationService
                        .createTeacherNotificationIfNecessary(
                                teacherId,
                                "Öğrenci Kursunuzu Tamamladı 🎓",
                                message,
                                "COURSE_COMPLETED"
                        );
            }
        }


        return toResponse(
                saved
        );
    }


    // =========================================================
    // TEK KURS İLERLEMESİ
    // =========================================================

    public CourseProgressResponse getCourseProgress(
            Long studentId,
            Long courseId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğrenci bulunamadı."
                                )
                        );


        Course course =
                courseRepository
                        .findById(courseId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Kurs bulunamadı."
                                )
                        );


        // =====================================================
        // KURS SAHİPLİĞİ / KAYIT KONTROLÜ
        //
        // Progress kaydı henüz oluşmamış olsa bile
        // öğrenci kursa kayıtlı değilse bilgi alamaz.
        // =====================================================

        requireEnrollment(
                student,
                courseId,
                "Bu kursun ilerleme bilgisine erişemezsiniz."
        );


        CourseProgress progress =
                progressRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElse(
                                null
                        );


        /*
         * Henüz hiç izlenmediyse
         * sıfır progress dön.
         */
        if (
                progress == null
        ) {

            return new CourseProgressResponse(

                    course.getId(),

                    course.getName(),

                    course.getImageUrl(),

                    0,

                    0,

                    0,

                    0,

                    false,

                    null
            );
        }


        return toResponse(
                progress
        );
    }


    // =========================================================
    // ÖĞRENCİNİN TÜM İLERLEMELERİ
    //
    // Aynı zamanda izleme geçmişi tarafından kullanılır.
    // =========================================================

    public List<CourseProgressResponse>
    getStudentProgress(
            Long studentId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        studentRepository
                .findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Öğrenci bulunamadı."
                        )
                );


        return progressRepository
                .findAllByStudentIdOrderByLastWatchedAtDesc(
                        studentId
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }


    // =========================================================
    // KURSA KAYIT KONTROLÜ
    // =========================================================

    private void requireEnrollment(
            Student student,
            Long courseId,
            String errorMessage) {


        boolean enrolled =
                student
                        .getEnrolledCourses()
                        .stream()
                        .anyMatch(
                                enrolledCourse ->
                                        Objects.equals(
                                                enrolledCourse.getId(),
                                                courseId
                                        )
                        );


        if (!enrolled) {

            throw new AccessDeniedException(
                    errorMessage
            );
        }
    }


    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private CourseProgressResponse toResponse(
            CourseProgress progress) {

        return new CourseProgressResponse(

                progress
                        .getCourse()
                        .getId(),

                progress
                        .getCourse()
                        .getName(),

                progress
                        .getCourse()
                        .getImageUrl(),

                progress
                        .getLastPositionSeconds(),

                progress
                        .getMaxPositionSeconds(),

                progress
                        .getDurationSeconds(),

                progress
                        .getProgressPercentage(),

                progress.isCompleted(),

                progress
                        .getLastWatchedAt()
        );
    }
}