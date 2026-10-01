package com.example.course_platform.service;


import com.example.course_platform.dto.TeacherRegistrationCompleteRequest;
import com.example.course_platform.dto.TeacherRegistrationCreateRequest;
import com.example.course_platform.dto.TeacherRegistrationRequestResponse;

import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.Teacher;
import com.example.course_platform.entity.TeacherRegistrationRequest;
import com.example.course_platform.entity.TeacherRegistrationStatus;

import com.example.course_platform.repository.TeacherRegistrationRequestRepository;
import com.example.course_platform.repository.TeacherRepository;
import com.example.course_platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.mail.MailException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;


import java.security.SecureRandom;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class TeacherRegistrationService {


    // =========================================================
    // VERIFICATION CODE SETTINGS
    // =========================================================

    private static final SecureRandom
            SECURE_RANDOM =
            new SecureRandom();


    private static final int
            VERIFICATION_CODE_EXPIRATION_MINUTES =
            15;


    // =========================================================
    // REPOSITORIES
    // =========================================================

    private final TeacherRegistrationRequestRepository
            teacherRegistrationRequestRepository;


    private final TeacherRepository
            teacherRepository;


    private final UserRepository
            userRepository;


    // =========================================================
    // SERVICES
    // =========================================================

    private final TeacherRegistrationMailService
            teacherRegistrationMailService;


    private final PasswordEncoder
            passwordEncoder;


    // =========================================================
    // CREATE TEACHER REQUEST
    //
    // Burada henüz Teacher hesabı oluşturulmaz.
    //
    // Başvuru:
    //
    // PENDING
    //
    // olarak oluşturulur.
    // =========================================================

    @Transactional
    public TeacherRegistrationRequestResponse
    createRequest(

            TeacherRegistrationCreateRequest request
    ) {


        // =====================================================
        // NAME NORMALIZATION
        // =====================================================

        String name =
                request
                        .name()
                        .trim();


        // =====================================================
        // EMAIL NORMALIZATION
        // =====================================================

        String email =
                request
                        .email()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        // =====================================================
        // USER ZATEN VAR MI?
        // =====================================================

        if (
                userRepository
                        .existsByEmailIgnoreCase(
                                email
                        )
        ) {


            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Bu e-posta adresiyle zaten bir kullanıcı hesabı bulunmaktadır."
            );
        }


        // =====================================================
        // AKTİF BAŞVURU VAR MI?
        //
        // PENDING
        // veya
        // CODE_SENT
        //
        // durumunda başka aktif başvuru varsa
        // yeni başvuru oluşturulmaz.
        // =====================================================

        boolean activeRequestExists =

                teacherRegistrationRequestRepository

                        .findFirstByEmailIgnoreCaseAndStatusInOrderByRequestedAtDesc(

                                email,

                                List.of(

                                        TeacherRegistrationStatus.PENDING,

                                        TeacherRegistrationStatus.CODE_SENT
                                )
                        )

                        .isPresent();


        if (
                activeRequestExists
        ) {


            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Bu e-posta adresi için zaten aktif bir öğretmen başvurusu bulunmaktadır."
            );
        }


        // =====================================================
        // CREATE REQUEST
        // =====================================================

        TeacherRegistrationRequest registrationRequest =
                new TeacherRegistrationRequest();


        registrationRequest.setName(
                name
        );


        registrationRequest.setEmail(
                email
        );


        registrationRequest.setStatus(
                TeacherRegistrationStatus.PENDING
        );


        registrationRequest.setRequestedAt(
                LocalDateTime.now()
        );


        TeacherRegistrationRequest savedRequest =

                teacherRegistrationRequestRepository.save(
                        registrationRequest
                );


        return toResponse(
                savedRequest
        );
    }


    // =========================================================
    // ADMIN - GET ALL
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public List<TeacherRegistrationRequestResponse>
    getAllRequests() {


        return teacherRegistrationRequestRepository

                .findAllByOrderByRequestedAtDesc()

                .stream()

                .map(
                        this::toResponse
                )

                .toList();
    }


    // =========================================================
    // ADMIN - GET PENDING
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public List<TeacherRegistrationRequestResponse>
    getPendingRequests() {


        return teacherRegistrationRequestRepository

                .findAllByStatusOrderByRequestedAtDesc(
                        TeacherRegistrationStatus.PENDING
                )

                .stream()

                .map(
                        this::toResponse
                )

                .toList();
    }


    // =========================================================
    // ADMIN - APPROVE + SEND CODE
    // =========================================================

    @Transactional
    public TeacherRegistrationRequestResponse
    approveAndSendCode(

            Long requestId
    ) {


        // =====================================================
        // BAŞVURUYU KİLİTLEYEREK AL
        // =====================================================

        TeacherRegistrationRequest registrationRequest =

                teacherRegistrationRequestRepository

                        .findByIdForUpdate(
                                requestId
                        )

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Öğretmenlik başvurusu bulunamadı."
                                )
                        );


        // =====================================================
        // SADECE PENDING ONAYLANABİLİR
        // =====================================================

        if (
                registrationRequest.getStatus()
                        != TeacherRegistrationStatus.PENDING
        ) {


            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Yalnızca bekleyen başvurular onaylanabilir."
            );
        }


        // =====================================================
        // USER BU ARADA OLUŞMUŞ MU?
        // =====================================================

        if (
                userRepository
                        .existsByEmailIgnoreCase(
                                registrationRequest.getEmail()
                        )
        ) {


            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Bu e-posta adresine ait bir kullanıcı hesabı zaten bulunmaktadır."
            );
        }


        // =====================================================
        // 6 HANELİ VERIFICATION CODE
        // =====================================================

        String verificationCode =
                generateVerificationCode();


        LocalDateTime now =
                LocalDateTime.now();


        LocalDateTime expiresAt =
                now.plusMinutes(
                        VERIFICATION_CODE_EXPIRATION_MINUTES
                );


        // =====================================================
        // KODU HASHLE
        //
        // Düz kod DB'de tutulmaz.
        // =====================================================

        String verificationCodeHash =

                passwordEncoder.encode(
                        verificationCode
                );


        registrationRequest.setVerificationCodeHash(
                verificationCodeHash
        );


        registrationRequest.setCodeExpiresAt(
                expiresAt
        );


        registrationRequest.setCodeSentAt(
                now
        );


        registrationRequest.setStatus(
                TeacherRegistrationStatus.CODE_SENT
        );


        // =====================================================
        // DB FLUSH
        // =====================================================

        teacherRegistrationRequestRepository
                .saveAndFlush(
                        registrationRequest
                );


        // =====================================================
        // EMAIL GÖNDER
        // =====================================================

        try {


            teacherRegistrationMailService
                    .sendVerificationCode(

                            registrationRequest.getEmail(),

                            registrationRequest.getName(),

                            verificationCode,

                            expiresAt
                    );


        } catch (
                MailException exception
        ) {


            throw new ResponseStatusException(

                    HttpStatus.BAD_GATEWAY,

                    "Doğrulama e-postası gönderilemedi. Başvuru onaylanmadı.",

                    exception
            );
        }


        return toResponse(
                registrationRequest
        );
    }


    // =========================================================
    // COMPLETE TEACHER REGISTRATION
    //
    // Öğretmen:
    //
    // email
    // verificationCode
    // password
    // confirmPassword
    //
    // gönderir.
    //
    // Başarılı olursa:
    //
    // CODE_SENT
    //      ↓
    // TEACHER USER
    //      ↓
    // COMPLETED
    //
    // =========================================================

    @Transactional
    public TeacherRegistrationRequestResponse
    completeRegistration(

            TeacherRegistrationCompleteRequest request
    ) {


        // =====================================================
        // EMAIL NORMALIZATION
        // =====================================================

        String email =
                request
                        .email()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        // =====================================================
        // PASSWORD CONFIRMATION
        // =====================================================

        if (
                !request
                        .password()
                        .equals(
                                request.confirmPassword()
                        )
        ) {


            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Şifre ve şifre tekrarı aynı olmalıdır."
            );
        }


        // =====================================================
        // CODE_SENT BAŞVURUYU BUL + ROW LOCK
        //
        // Burada PESSIMISTIC_WRITE kullanıyoruz.
        //
        // Böylece aynı başvurunun doğrulama kodu
        // aynı anda iki ayrı request tarafından
        // kullanılamaz.
        // =====================================================

        TeacherRegistrationRequest registrationRequest =

                teacherRegistrationRequestRepository

                        .findFirstByEmailIgnoreCaseAndStatusOrderByRequestedAtDesc(

                                email,

                                TeacherRegistrationStatus.CODE_SENT
                        )

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Bu e-posta adresi için doğrulanmayı bekleyen öğretmen başvurusu bulunamadı."
                                )
                        );


        // =====================================================
        // USER ZATEN VAR MI?
        // =====================================================

        if (
                userRepository
                        .existsByEmailIgnoreCase(
                                email
                        )
        ) {


            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Bu e-posta adresiyle zaten bir kullanıcı hesabı bulunmaktadır."
            );
        }


        // =====================================================
        // VERIFICATION DATA VAR MI?
        // =====================================================

        if (
                registrationRequest
                        .getVerificationCodeHash()
                        == null
                        ||
                        registrationRequest
                                .getCodeExpiresAt()
                                == null
        ) {


            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Bu başvuru için geçerli bir doğrulama kodu bulunmamaktadır."
            );
        }


        // =====================================================
        // KOD SÜRESİ DOLMUŞ MU?
        // =====================================================

        LocalDateTime now =
                LocalDateTime.now();


        if (
                !now.isBefore(
                        registrationRequest
                                .getCodeExpiresAt()
                )
        ) {


            throw new ResponseStatusException(

                    HttpStatus.GONE,

                    "Doğrulama kodunun süresi dolmuştur."
            );
        }


        // =====================================================
        // VERIFICATION CODE CHECK
        //
        // Kullanıcının gönderdiği düz kod:
        //
        // request.verificationCode()
        //
        // DB'deki BCrypt hash:
        //
        // registrationRequest.verificationCodeHash
        //
        // ile karşılaştırılır.
        // =====================================================

        boolean verificationCodeMatches =

                passwordEncoder.matches(

                        request.verificationCode(),

                        registrationRequest
                                .getVerificationCodeHash()
                );


        if (
                !verificationCodeMatches
        ) {


            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Doğrulama kodu hatalı."
            );
        }


        // =====================================================
        // PASSWORD HASH
        //
        // Öğretmenin gerçek şifresi de düz metin
        // olarak veritabanına yazılmaz.
        // =====================================================

        String encodedPassword =

                passwordEncoder.encode(
                        request.password()
                );


        // =====================================================
        // CREATE TEACHER
        //
        // Teacher extends User olduğu için:
        //
        // users
        // teachers
        //
        // tabloları JPA JOINED inheritance
        // yapısına göre oluşturulur.
        // =====================================================

        Teacher teacher =
                new Teacher();


        teacher.setName(
                registrationRequest.getName()
        );


        teacher.setEmail(
                registrationRequest.getEmail()
        );


        teacher.setPassword(
                encodedPassword
        );


        teacher.setRole(
                Role.TEACHER
        );


        // =====================================================
        // TEACHER SAVE
        // =====================================================

        teacherRepository.saveAndFlush(
                teacher
        );


        // =====================================================
        // BAŞVURUYU COMPLETED YAP
        // =====================================================

        registrationRequest.setStatus(
                TeacherRegistrationStatus.COMPLETED
        );


        registrationRequest.setCompletedAt(
                now
        );


        // =====================================================
        // KULLANILMIŞ DOĞRULAMA KODUNU TEMİZLE
        //
        // Kod artık tekrar doğrulanamaz.
        //
        // codeSentAt bilgisini audit amacıyla
        // bırakıyoruz.
        // =====================================================

        registrationRequest.setVerificationCodeHash(
                null
        );


        registrationRequest.setCodeExpiresAt(
                null
        );


        TeacherRegistrationRequest savedRequest =

                teacherRegistrationRequestRepository
                        .save(
                                registrationRequest
                        );


        // =====================================================
        // TRANSACTION
        //
        // Teacher oluşturma ve başvurunun COMPLETED
        // yapılması aynı transaction içerisindedir.
        //
        // İşlemlerden biri başarısız olursa tamamı
        // rollback edilir.
        // =====================================================

        return toResponse(
                savedRequest
        );
    }


    // =========================================================
    // ADMIN - REJECT
    // =========================================================

    @Transactional
    public TeacherRegistrationRequestResponse
    rejectRequest(

            Long requestId
    ) {


        TeacherRegistrationRequest registrationRequest =

                teacherRegistrationRequestRepository

                        .findByIdForUpdate(
                                requestId
                        )

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Öğretmenlik başvurusu bulunamadı."
                                )
                        );


        // =====================================================
        // SADECE PENDING REDDEDİLEBİLİR
        // =====================================================

        if (
                registrationRequest.getStatus()
                        != TeacherRegistrationStatus.PENDING
        ) {


            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Yalnızca bekleyen başvurular reddedilebilir."
            );
        }


        // =====================================================
        // REJECT
        // =====================================================

        registrationRequest.setStatus(
                TeacherRegistrationStatus.REJECTED
        );


        registrationRequest.setRejectedAt(
                LocalDateTime.now()
        );


        // =====================================================
        // OLASI CODE DATA TEMİZLE
        // =====================================================

        registrationRequest.setVerificationCodeHash(
                null
        );


        registrationRequest.setCodeExpiresAt(
                null
        );


        registrationRequest.setCodeSentAt(
                null
        );


        TeacherRegistrationRequest savedRequest =

                teacherRegistrationRequestRepository.save(
                        registrationRequest
                );


        return toResponse(
                savedRequest
        );
    }


    // =========================================================
    // GENERATE VERIFICATION CODE
    // =========================================================

    private String generateVerificationCode() {


        int code =
                SECURE_RANDOM.nextInt(
                        1_000_000
                );


        return String.format(
                "%06d",
                code
        );
    }


    // =========================================================
    // RESPONSE MAPPER
    // =========================================================

    private TeacherRegistrationRequestResponse
    toResponse(

            TeacherRegistrationRequest request
    ) {


        return new TeacherRegistrationRequestResponse(

                request.getId(),

                request.getName(),

                request.getEmail(),

                request
                        .getStatus()
                        .name(),

                request.getRequestedAt()
        );
    }
}