package com.example.course_platform.service;

import com.example.course_platform.dto.PaymentHistoryResponse;

import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.PaymentHistory;
import com.example.course_platform.entity.Student;

import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.PaymentHistoryRepository;
import com.example.course_platform.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PaymentHistoryService {


    private final PaymentHistoryRepository paymentHistoryRepository;

    private final StudentRepository studentRepository;

    private final CourseRepository courseRepository;

    private final NotificationService notificationService;

    private final CurrentUserService currentUserService;


    // =========================================================
    // SATIN ALMA KAYDI OLUŞTUR
    //
    // Bu metot HTTP endpointinden doğrudan çağrılmaz.
    //
    // Yalnızca başarılı OrderService checkout işlemi
    // içerisinde çağrılır.
    //
    // priceAtPurchase:
    //
    // OrderItem üzerinde saklanan gerçek satın alma fiyatıdır.
    // Course'un daha sonra değişebilen güncel fiyatına
    // güvenilmez.
    // =========================================================

    @Transactional
    public PaymentHistoryResponse
    createPaymentRecordIfNecessary(

            Long studentId,

            Long courseId,

            BigDecimal priceAtPurchase,

            String courseNameAtPurchase
    ) {


        // =====================================================
        // DAHA ÖNCE ÖDEME KAYDI VAR MI?
        // =====================================================

        PaymentHistory existing =
                paymentHistoryRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElse(
                                null
                        );


        if (
                existing != null
        ) {

            return toResponse(
                    existing
            );
        }


        // =====================================================
        // SATIN ALMA FİYATI KONTROLÜ
        // =====================================================

        if (
                priceAtPurchase == null
                        ||
                        priceAtPurchase.compareTo(
                                BigDecimal.ZERO
                        ) < 0
        ) {

            throw new IllegalArgumentException(
                    "Geçersiz satın alma fiyatı."
            );
        }


        // =====================================================
        // ÖĞRENCİ
        // =====================================================

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
        // KURS
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
        // ÖĞRENCİ KURSA GERÇEKTEN SAHİP Mİ?
        //
        // PaymentHistory yalnızca başarılı enrollment
        // sonrasında oluşabilir.
        // =====================================================

        boolean ownsCourse =
                student
                        .getEnrolledCourses()
                        .stream()
                        .anyMatch(
                                enrolledCourse ->
                                        enrolledCourse
                                                .getId()
                                                .equals(
                                                        courseId
                                                )
                        );


        if (
                !ownsCourse
        ) {

            throw new IllegalArgumentException(
                    "Satın alınmamış bir kurs için ödeme kaydı oluşturulamaz."
            );
        }


        // =====================================================
        // PAYMENT HISTORY
        // =====================================================

        PaymentHistory payment =
                new PaymentHistory();


        payment.setStudent(
                student
        );


        payment.setCourse(
                course
        );


        // =====================================================
        // KURS ADI SNAPSHOT
        //
        // OrderItem'taki satın alma anındaki isim kullanılır.
        //
        // Kurs adı daha sonra değişse bile ödeme geçmişi
        // değişmez.
        // =====================================================

        String finalCourseName =
                courseNameAtPurchase != null
                        &&
                        !courseNameAtPurchase.isBlank()

                        ?
                        courseNameAtPurchase

                        :
                        course.getName();


        payment.setCourseName(
                finalCourseName
        );


        // =====================================================
        // GERÇEK SATIN ALMA FİYATI
        //
        // ESKİ:
        //
        // course.getPrice()
        //
        // YENİ:
        //
        // OrderItem.priceAtPurchase
        // =====================================================

        payment.setAmount(
                priceAtPurchase
        );


        // =====================================================
        // DURUM
        // =====================================================

        payment.setStatus(
                "COMPLETED"
        );


        // =====================================================
        // ÖDEME YÖNTEMİ
        //
        // Henüz gerçek ödeme sağlayıcısı yok.
        // =====================================================

        payment.setPaymentMethod(
                "PLATFORM"
        );


        // =====================================================
        // TRANSACTION CODE
        // =====================================================

        String transactionCode =
                "PAY-"
                        +
                        studentId
                        +
                        "-"
                        +
                        courseId
                        +
                        "-"
                        +
                        UUID.randomUUID()
                                .toString()
                                .substring(
                                        0,
                                        8
                                )
                                .toUpperCase();


        payment.setTransactionCode(
                transactionCode
        );


        // =====================================================
        // SATIN ALMA TARİHİ
        // =====================================================

        payment.setPurchasedAt(
                LocalDateTime.now()
        );


        // =====================================================
        // DATABASE
        // =====================================================

        PaymentHistory saved =
                paymentHistoryRepository.save(
                        payment
                );


        // =====================================================
        // ÖĞRENCİ BİLDİRİMİ
        // =====================================================

        notificationService
                .createNotificationIfNecessary(

                        studentId,

                        "Kurs Satın Alma Tamamlandı 🎓",

                        finalCourseName
                                +
                                " kursu hesabınıza başarıyla eklendi.",

                        "PAYMENT"
                );


        return toResponse(
                saved
        );
    }


    // =========================================================
    // ÖĞRENCİNİN ÖDEME GEÇMİŞİ
    // =========================================================

    public List<PaymentHistoryResponse>
    getStudentPaymentHistory(
            Long studentId
    ) {


        // =====================================================
        // OWNERSHIP / IDOR
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        studentRepository
                .findById(
                        studentId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Öğrenci bulunamadı."
                        )
                );


        return paymentHistoryRepository
                .findAllByStudentIdOrderByPurchasedAtDesc(
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

    private PaymentHistoryResponse toResponse(
            PaymentHistory payment
    ) {


        return new PaymentHistoryResponse(

                payment.getId(),

                payment
                        .getCourse()
                        .getId(),

                payment.getCourseName(),

                payment
                        .getCourse()
                        .getImageUrl(),

                payment.getAmount(),

                payment.getStatus(),

                payment.getPaymentMethod(),

                payment.getTransactionCode(),

                payment.getPurchasedAt()
        );
    }
}