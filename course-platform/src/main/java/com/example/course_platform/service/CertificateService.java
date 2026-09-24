package com.example.course_platform.service;

import com.example.course_platform.dto.CertificateResponse;

import com.example.course_platform.entity.Certificate;
import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.CourseProgress;
import com.example.course_platform.entity.Student;

import com.example.course_platform.repository.CertificateRepository;
import com.example.course_platform.repository.CourseProgressRepository;
import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;

    private final StudentRepository studentRepository;

    private final CourseRepository courseRepository;

    private final CourseProgressRepository courseProgressRepository;

    private final NotificationService notificationService;

    private final CurrentUserService currentUserService;


    // =========================================================
    // SERTİFİKA VARSA GETİR, YOKSA OLUŞTUR
    // =========================================================

    @Transactional
    public CertificateResponse createCertificateIfNecessary(
            Long studentId,
            Long courseId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        //
        // Başka öğrenci adına sertifika oluşturulamaz.
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        // =====================================================
        // ÖĞRENCİYİ BUL
        // =====================================================

        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğrenci bulunamadı."
                                )
                        );


        // =====================================================
        // KURSU BUL
        // =====================================================

        Course course =
                courseRepository
                        .findById(courseId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Kurs bulunamadı."
                                )
                        );


        // =====================================================
        // SERTİFİKA ZATEN VAR MI?
        //
        // Eski sistemden oluşmuş geçerli sertifikaları
        // tekrar üretmiyoruz.
        // =====================================================

        Certificate existingCertificate =
                certificateRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElse(
                                null
                        );


        if (
                existingCertificate != null
        ) {

            return toResponse(
                    existingCertificate
            );
        }


        // =====================================================
        // KURS GERÇEKTEN TAMAMLANMIŞ MI?
        //
        // Sadece endpoint güvenliğine güvenmiyoruz.
        // Sertifika oluşturma iş kuralını servis
        // seviyesinde de doğruluyoruz.
        // =====================================================

        CourseProgress progress =
                courseProgressRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Bu kurs için sertifika almaya hak kazanmadınız."
                                )
                        );


        if (
                !progress.isCompleted()
        ) {

            throw new AccessDeniedException(
                    "Kurs tamamlanmadan sertifika oluşturulamaz."
            );
        }


        // =====================================================
        // SERTİFİKA OLUŞTUR
        // =====================================================

        Certificate certificate =
                new Certificate();


        certificate.setStudent(
                student
        );


        certificate.setCourse(
                course
        );


        String certificateCode =
                "CERT-"
                        + studentId
                        + "-"
                        + courseId
                        + "-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();


        certificate.setCertificateCode(
                certificateCode
        );


        certificate.setIssuedAt(
                LocalDateTime.now()
        );


        // =====================================================
        // SERTİFİKAYI KAYDET
        // =====================================================

        Certificate savedCertificate =
                certificateRepository.save(
                        certificate
                );


        // =====================================================
        // ÖĞRENCİ BİLDİRİMİ
        // =====================================================

        String notificationMessage =
                course.getName()
                        +
                        " eğitimini başarıyla tamamladınız. "
                        +
                        "Sertifikanız hazır.";


        notificationService
                .createNotificationIfNecessary(
                        studentId,
                        "Yeni Sertifika Kazandınız! 🏆",
                        notificationMessage,
                        "CERTIFICATE"
                );


        return toResponse(
                savedCertificate
        );
    }


    // =========================================================
    // ÖĞRENCİNİN TÜM SERTİFİKALARINI GETİR
    // =========================================================

    @Transactional
    public List<CertificateResponse> getCertificatesForStudent(
            Long studentId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        //
        // student/8 giriş yapan öğrenci 8 ise çalışır.
        // student/9 denenirse 403 döner.
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


        // =====================================================
        // ÖĞRENCİNİN PROGRESS KAYITLARI
        // =====================================================

        List<CourseProgress> progressList =
                courseProgressRepository
                        .findAllByStudentIdOrderByLastWatchedAtDesc(
                                student.getId()
                        );


        // =====================================================
        // TAMAMLANMIŞ FAKAT EKSİK SERTİFİKASI OLAN
        // KURSLARI SENKRONİZE ET
        // =====================================================

        for (
                CourseProgress progress :
                progressList
        ) {

            if (
                    !progress.isCompleted()
            ) {

                continue;
            }


            Long completedCourseId =
                    progress
                            .getCourse()
                            .getId();


            boolean certificateExists =
                    certificateRepository
                            .existsByStudentIdAndCourseId(
                                    studentId,
                                    completedCourseId
                            );


            if (
                    !certificateExists
            ) {

                createCertificateIfNecessary(
                        studentId,
                        completedCourseId
                );
            }
        }


        // =====================================================
        // SADECE BU ÖĞRENCİNİN SERTİFİKALARI
        // =====================================================

        return certificateRepository
                .findAllByStudentIdOrderByIssuedAtDesc(
                        studentId
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

    private CertificateResponse toResponse(
            Certificate certificate) {

        return new CertificateResponse(

                certificate.getId(),

                certificate
                        .getCourse()
                        .getId(),

                certificate
                        .getCourse()
                        .getName(),

                certificate
                        .getCourse()
                        .getImageUrl(),

                certificate
                        .getCertificateCode(),

                certificate
                        .getIssuedAt()
        );
    }
}