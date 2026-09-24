package com.example.course_platform.service;

import com.example.course_platform.dto.NotificationResponse;

import com.example.course_platform.entity.Certificate;
import com.example.course_platform.entity.Notification;
import com.example.course_platform.entity.PaymentHistory;
import com.example.course_platform.entity.Student;
import com.example.course_platform.entity.Teacher;

import com.example.course_platform.repository.CertificateRepository;
import com.example.course_platform.repository.NotificationRepository;
import com.example.course_platform.repository.PaymentHistoryRepository;
import com.example.course_platform.repository.StudentRepository;
import com.example.course_platform.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final StudentRepository studentRepository;

    private final TeacherRepository teacherRepository;

    private final CertificateRepository certificateRepository;

    private final PaymentHistoryRepository paymentHistoryRepository;

    private final CurrentUserService currentUserService;


    // =========================================================
    // ÖĞRENCİ BİLDİRİMİ OLUŞTUR
    //
    // Sistem içi kullanım içindir.
    // Progress / Certificate / Payment gibi servislerden
    // çağrılabileceği için burada current user kontrolü yoktur.
    // =========================================================

    @Transactional
    public NotificationResponse createNotification(
            Long studentId,
            String title,
            String message,
            String type) {

        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğrenci bulunamadı."
                                )
                        );


        Notification notification =
                new Notification();


        notification.setStudent(
                student
        );


        notification.setTeacher(
                null
        );


        notification.setTitle(
                title
        );


        notification.setMessage(
                message
        );


        notification.setType(
                type
        );


        notification.setRead(
                false
        );


        notification.setCreatedAt(
                LocalDateTime.now()
        );


        Notification saved =
                notificationRepository.save(
                        notification
                );


        return toResponse(
                saved
        );
    }


    // =========================================================
    // AYNI ÖĞRENCİ BİLDİRİMİNİ TEKRAR OLUŞTURMA
    // =========================================================

    @Transactional
    public void createNotificationIfNecessary(
            Long studentId,
            String title,
            String message,
            String type) {

        boolean exists =
                notificationRepository
                        .existsByStudentIdAndTypeAndMessage(
                                studentId,
                                type,
                                message
                        );


        if (exists) {

            return;
        }


        createNotification(
                studentId,
                title,
                message,
                type
        );
    }


    // =========================================================
    // ÖĞRETMEN BİLDİRİMİ OLUŞTUR
    //
    // Sistem içi kullanım içindir.
    // =========================================================

    @Transactional
    public NotificationResponse createTeacherNotification(
            Long teacherId,
            String title,
            String message,
            String type) {

        Teacher teacher =
                teacherRepository
                        .findById(teacherId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğretmen bulunamadı."
                                )
                        );


        Notification notification =
                new Notification();


        notification.setStudent(
                null
        );


        notification.setTeacher(
                teacher
        );


        notification.setTitle(
                title
        );


        notification.setMessage(
                message
        );


        notification.setType(
                type
        );


        notification.setRead(
                false
        );


        notification.setCreatedAt(
                LocalDateTime.now()
        );


        Notification saved =
                notificationRepository.save(
                        notification
                );


        return toResponse(
                saved
        );
    }


    // =========================================================
    // AYNI ÖĞRETMEN BİLDİRİMİNİ TEKRAR OLUŞTURMA
    // =========================================================

    @Transactional
    public void createTeacherNotificationIfNecessary(
            Long teacherId,
            String title,
            String message,
            String type) {

        boolean exists =
                notificationRepository
                        .existsByTeacherIdAndTypeAndMessage(
                                teacherId,
                                type,
                                message
                        );


        if (exists) {

            return;
        }


        createTeacherNotification(
                teacherId,
                title,
                message,
                type
        );
    }


    // =========================================================
    // ESKİ SERTİFİKALAR İÇİN
    // EKSİK ÖĞRENCİ BİLDİRİMLERİNİ OLUŞTUR
    //
    // Sistem içi yardımcı metottur.
    // =========================================================

    @Transactional
    public void syncCertificateNotifications(
            Long studentId) {

        List<Certificate> certificates =
                certificateRepository
                        .findAllByStudentIdOrderByIssuedAtDesc(
                                studentId
                        );


        for (
                Certificate certificate
                :
                certificates
        ) {

            String message =
                    certificate
                            .getCourse()
                            .getName()
                            +
                            " eğitimini başarıyla tamamladınız. "
                            +
                            "Sertifikanız hazır.";


            createNotificationIfNecessary(
                    studentId,
                    "Yeni Sertifika Kazandınız! 🏆",
                    message,
                    "CERTIFICATE"
            );
        }
    }


    // =========================================================
    // ÖĞRETMEN SATIŞ BİLDİRİMLERİNİ SENKRONLA
    //
    // Sistem içi yardımcı metottur.
    // =========================================================

    @Transactional
    public void syncTeacherSaleNotifications(
            Long teacherId) {

        teacherRepository
                .findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Öğretmen bulunamadı."
                        )
                );


        List<PaymentHistory> payments =
                paymentHistoryRepository
                        .findAllByTeacherId(
                                teacherId
                        );


        for (
                PaymentHistory payment
                :
                payments
        ) {

            if (
                    !Objects.equals(
                            payment.getStatus(),
                            "COMPLETED"
                    )
            ) {

                continue;
            }


            String studentName =
                    payment
                            .getStudent()
                            .getName();


            String courseName =
                    payment
                            .getCourseName();


            String message =
                    studentName
                            +
                            ", \""
                            +
                            courseName
                            +
                            "\" kursunu "
                            +
                            payment.getAmount()
                            +
                            " TL tutarında satın aldı.";


            createTeacherNotificationIfNecessary(
                    teacherId,
                    "Yeni Kurs Satışı 🛒",
                    message,
                    "SALE"
            );
        }
    }


    // =========================================================
    // ÖĞRENCİNİN BİLDİRİMLERİ
    // =========================================================

    @Transactional
    public List<NotificationResponse>
    getStudentNotifications(
            Long studentId) {


        // =====================================================
        // IDOR / OWNERSHIP
        //
        // URL'deki öğrenci ID'si ile JWT kullanıcısının
        // ID'si aynı olmak zorunda.
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


        syncCertificateNotifications(
                studentId
        );


        return notificationRepository
                .findAllByStudentIdOrderByCreatedAtDesc(
                        studentId
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }


    // =========================================================
    // ÖĞRETMENİN BİLDİRİMLERİ
    // =========================================================

    @Transactional
    public List<NotificationResponse>
    getTeacherNotifications(
            Long teacherId) {


        // =====================================================
        // IDOR / OWNERSHIP
        // =====================================================

        currentUserService.requireTeacher(
                teacherId
        );


        teacherRepository
                .findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Öğretmen bulunamadı."
                        )
                );


        syncTeacherSaleNotifications(
                teacherId
        );


        return notificationRepository
                .findAllByTeacherIdOrderByCreatedAtDesc(
                        teacherId
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }


    // =========================================================
    // ÖĞRENCİ - TEK BİLDİRİMİ OKUNDU YAP
    // =========================================================

    @Transactional
    public NotificationResponse markAsRead(
            Long studentId,
            Long notificationId) {


        // URL'deki studentId başka bir öğrenci olamaz.
        currentUserService.requireStudent(
                studentId
        );


        Notification notification =
                notificationRepository
                        .findById(
                                notificationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Bildirim bulunamadı."
                                )
                        );


        // Bildirim gerçekten bu öğrenciye mi ait?
        if (
                notification.getStudent() == null
                        ||
                        !Objects.equals(
                                notification
                                        .getStudent()
                                        .getId(),
                                studentId
                        )
        ) {

            throw new AccessDeniedException(
                    "Bu bildirime erişemezsiniz."
            );
        }


        notification.setRead(
                true
        );


        Notification saved =
                notificationRepository.save(
                        notification
                );


        return toResponse(
                saved
        );
    }


    // =========================================================
    // ÖĞRETMEN - TEK BİLDİRİMİ OKUNDU YAP
    // =========================================================

    @Transactional
    public NotificationResponse markTeacherNotificationAsRead(
            Long teacherId,
            Long notificationId) {


        // URL'deki teacherId başka öğretmene ait olamaz.
        currentUserService.requireTeacher(
                teacherId
        );


        Notification notification =
                notificationRepository
                        .findById(
                                notificationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Bildirim bulunamadı."
                                )
                        );


        // Bildirim gerçekten bu öğretmene mi ait?
        if (
                notification.getTeacher() == null
                        ||
                        !Objects.equals(
                                notification
                                        .getTeacher()
                                        .getId(),
                                teacherId
                        )
        ) {

            throw new AccessDeniedException(
                    "Bu bildirime erişemezsiniz."
            );
        }


        notification.setRead(
                true
        );


        Notification saved =
                notificationRepository.save(
                        notification
                );


        return toResponse(
                saved
        );
    }


    // =========================================================
    // ÖĞRENCİ - TÜMÜNÜ OKUNDU YAP
    // =========================================================

    @Transactional
    public void markAllAsRead(
            Long studentId) {


        currentUserService.requireStudent(
                studentId
        );


        List<Notification> notifications =
                notificationRepository
                        .findAllByStudentIdOrderByCreatedAtDesc(
                                studentId
                        );


        for (
                Notification notification
                :
                notifications
        ) {

            notification.setRead(
                    true
            );
        }


        notificationRepository.saveAll(
                notifications
        );
    }


    // =========================================================
    // ÖĞRETMEN - TÜMÜNÜ OKUNDU YAP
    // =========================================================

    @Transactional
    public void markAllTeacherNotificationsAsRead(
            Long teacherId) {


        currentUserService.requireTeacher(
                teacherId
        );


        List<Notification> notifications =
                notificationRepository
                        .findAllByTeacherIdOrderByCreatedAtDesc(
                                teacherId
                        );


        for (
                Notification notification
                :
                notifications
        ) {

            notification.setRead(
                    true
            );
        }


        notificationRepository.saveAll(
                notifications
        );
    }


    // =========================================================
    // ÖĞRENCİ OKUNMAMIŞ SAYISI
    // =========================================================

    @Transactional
    public long getUnreadCount(
            Long studentId) {


        currentUserService.requireStudent(
                studentId
        );


        syncCertificateNotifications(
                studentId
        );


        return notificationRepository
                .countByStudentIdAndReadFalse(
                        studentId
                );
    }


    // =========================================================
    // ÖĞRETMEN OKUNMAMIŞ SAYISI
    // =========================================================

    @Transactional
    public long getTeacherUnreadCount(
            Long teacherId) {


        currentUserService.requireTeacher(
                teacherId
        );


        syncTeacherSaleNotifications(
                teacherId
        );


        return notificationRepository
                .countByTeacherIdAndReadFalse(
                        teacherId
                );
    }


    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private NotificationResponse toResponse(
            Notification notification) {

        return new NotificationResponse(

                notification.getId(),

                notification.getTitle(),

                notification.getMessage(),

                notification.getType(),

                notification.isRead(),

                notification.getCreatedAt()
        );
    }
}