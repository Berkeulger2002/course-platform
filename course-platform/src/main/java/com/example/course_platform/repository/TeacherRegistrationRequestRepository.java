package com.example.course_platform.repository;


import com.example.course_platform.entity.TeacherRegistrationRequest;
import com.example.course_platform.entity.TeacherRegistrationStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;


import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface TeacherRegistrationRequestRepository
        extends JpaRepository<TeacherRegistrationRequest, Long> {


    // =========================================================
    // TÜM BAŞVURULAR
    // =========================================================

    List<TeacherRegistrationRequest>
    findAllByOrderByRequestedAtDesc();


    // =========================================================
    // DURUMA GÖRE BAŞVURULAR
    // =========================================================

    List<TeacherRegistrationRequest>
    findAllByStatusOrderByRequestedAtDesc(
            TeacherRegistrationStatus status
    );


    // =========================================================
    // EMAIL + DURUM LİSTESİ
    //
    // Örneğin:
    //
    // PENDING
    // CODE_SENT
    //
    // durumlarından herhangi birinde aktif başvuru var mı?
    // =========================================================

    Optional<TeacherRegistrationRequest>
    findFirstByEmailIgnoreCaseAndStatusInOrderByRequestedAtDesc(

            String email,

            Collection<TeacherRegistrationStatus> statuses
    );


    // =========================================================
    // ADMIN İŞLEMİNDE ROW LOCK
    //
    // Aynı başvuruya iki admin isteği aynı anda gelirse
    // iki ayrı işlem yapılmasını engeller.
    // =========================================================

    @Lock(
            LockModeType.PESSIMISTIC_WRITE
    )
    @Query("""
            SELECT request
            FROM TeacherRegistrationRequest request
            WHERE request.id = :id
            """)
    Optional<TeacherRegistrationRequest>
    findByIdForUpdate(

            @Param("id")
            Long id
    );


    // =========================================================
    // TEACHER REGISTRATION COMPLETE - ROW LOCK
    //
    // Öğretmen:
    //
    // email
    // doğrulama kodu
    // şifre
    //
    // gönderdiğinde CODE_SENT durumundaki
    // başvurusunu bulur.
    //
    // PESSIMISTIC_WRITE:
    //
    // Aynı doğrulama koduyla aynı anda iki
    // kayıt tamamlama isteği gelirse yalnızca
    // bir tanesinin işlemi tamamlamasını sağlar.
    // =========================================================

    @Lock(
            LockModeType.PESSIMISTIC_WRITE
    )
    Optional<TeacherRegistrationRequest>
    findFirstByEmailIgnoreCaseAndStatusOrderByRequestedAtDesc(

            String email,

            TeacherRegistrationStatus status
    );
}