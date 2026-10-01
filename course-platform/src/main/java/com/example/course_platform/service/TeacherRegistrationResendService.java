package com.example.course_platform.service;


import com.example.course_platform.dto.TeacherRegistrationRequestResponse;

import com.example.course_platform.entity.TeacherRegistrationRequest;
import com.example.course_platform.entity.TeacherRegistrationStatus;

import com.example.course_platform.repository.TeacherRegistrationRequestRepository;
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


@Service
@RequiredArgsConstructor
public class TeacherRegistrationResendService {


    // =========================================================
    // SETTINGS
    // =========================================================

    private static final SecureRandom
            SECURE_RANDOM =
            new SecureRandom();


    private static final int
            VERIFICATION_CODE_EXPIRATION_MINUTES =
            15;


    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final TeacherRegistrationRequestRepository
            teacherRegistrationRequestRepository;


    private final UserRepository
            userRepository;


    private final TeacherRegistrationMailService
            teacherRegistrationMailService;


    private final PasswordEncoder
            passwordEncoder;


    // =========================================================
    // RESEND VERIFICATION CODE
    // =========================================================

    @Transactional
    public TeacherRegistrationRequestResponse
    resendVerificationCode(

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
        // YALNIZCA CODE_SENT
        // =====================================================

        if (
                registrationRequest.getStatus()
                        != TeacherRegistrationStatus.CODE_SENT
        ) {


            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Yalnızca doğrulama kodu gönderilmiş başvurular için yeni kod gönderilebilir."
            );
        }


        // =====================================================
        // USER ZATEN OLUŞMUŞ MU?
        // =====================================================

        if (
                userRepository
                        .existsByEmailIgnoreCase(
                                registrationRequest.getEmail()
                        )
        ) {


            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Bu e-posta adresine ait kullanıcı hesabı zaten bulunmaktadır."
            );
        }


        // =====================================================
        // YENİ 6 HANELİ KOD
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
        // YENİ KOD HASH
        // =====================================================

        String verificationCodeHash =

                passwordEncoder.encode(
                        verificationCode
                );


        registrationRequest.setVerificationCodeHash(
                verificationCodeHash
        );


        registrationRequest.setCodeSentAt(
                now
        );


        registrationRequest.setCodeExpiresAt(
                expiresAt
        );


        // =====================================================
        // DB FLUSH
        // =====================================================

        teacherRegistrationRequestRepository
                .saveAndFlush(
                        registrationRequest
                );


        // =====================================================
        // EMAIL
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

                    "Yeni doğrulama kodu e-posta ile gönderilemedi.",

                    exception
            );
        }


        return toResponse(
                registrationRequest
        );
    }


    // =========================================================
    // GENERATE CODE
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
    // RESPONSE
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