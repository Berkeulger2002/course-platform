package com.example.course_platform.service;


import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;


@Service
public class TeacherRegistrationMailService {


    // =========================================================
    // MAIL SENDER
    // =========================================================

    private final JavaMailSender
            mailSender;


    // =========================================================
    // FROM ADDRESS
    // =========================================================

    private final String
            mailFrom;


    // =========================================================
    // DATE FORMAT
    // =========================================================

    private static final DateTimeFormatter
            DATE_TIME_FORMATTER =

            DateTimeFormatter.ofPattern(
                    "dd.MM.yyyy HH:mm"
            );


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TeacherRegistrationMailService(

            JavaMailSender mailSender,

            @Value(
                    "${app.mail.from:}"
            )
            String mailFrom
    ) {


        this.mailSender =
                mailSender;


        this.mailFrom =
                mailFrom;
    }


    // =========================================================
    // SEND VERIFICATION CODE
    // =========================================================

    public void sendVerificationCode(

            String recipientEmail,

            String teacherName,

            String verificationCode,

            LocalDateTime expiresAt
    ) {


        SimpleMailMessage message =
                new SimpleMailMessage();


        // =====================================================
        // FROM
        // =====================================================

        if (
                mailFrom != null
                        &&
                        !mailFrom.isBlank()
        ) {


            message.setFrom(
                    mailFrom
            );
        }


        // =====================================================
        // TO
        // =====================================================

        message.setTo(
                recipientEmail
        );


        // =====================================================
        // SUBJECT
        // =====================================================

        message.setSubject(
                "Course Platform - Öğretmenlik Başvurunuz Onaylandı"
        );


        // =====================================================
        // BODY
        // =====================================================

        String body =

                "Merhaba "
                        +
                        teacherName
                        +
                        ",\n\n"

                        +
                        "Course Platform öğretmenlik başvurunuz yönetici tarafından onaylandı.\n\n"

                        +
                        "Doğrulama kodunuz:\n\n"

                        +
                        verificationCode
                        +
                        "\n\n"

                        +
                        "Bu kod "
                        +
                        expiresAt.format(
                                DATE_TIME_FORMATTER
                        )
                        +
                        " tarihine kadar geçerlidir.\n\n"

                        +
                        "Bu kodu kullanarak öğretmen hesabınızın kaydını tamamlayabilirsiniz.\n\n"

                        +
                        "Bu işlemi siz talep etmediyseniz bu e-postayı dikkate almayınız.\n\n"

                        +
                        "Course Platform";


        message.setText(
                body
        );


        // =====================================================
        // SEND
        // =====================================================

        mailSender.send(
                message
        );
    }
}