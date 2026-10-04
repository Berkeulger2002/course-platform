package com.example.course_platform.service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.MediaType;

import org.springframework.mail.MailSendException;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Map;


@Service
public class TeacherRegistrationMailService {


    // =========================================================
    // BREVO
    // =========================================================

    private static final String
            BREVO_API_URL =
            "https://api.brevo.com";


    private final String
            brevoApiKey;


    private final String
            senderEmail;


    private final String
            senderName;


    private final RestClient
            restClient;


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

            @Value(
                    "${app.brevo.api-key:}"
            )
            String brevoApiKey,

            @Value(
                    "${app.brevo.sender-email:}"
            )
            String senderEmail,

            @Value(
                    "${app.brevo.sender-name:Course Platform}"
            )
            String senderName
    ) {


        this.brevoApiKey =
                brevoApiKey;


        this.senderEmail =
                senderEmail;


        this.senderName =
                senderName;


        this.restClient =
                RestClient
                        .builder()
                        .baseUrl(
                                BREVO_API_URL
                        )
                        .build();
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


        // =====================================================
        // CONFIG CHECK
        // =====================================================

        if (
                brevoApiKey == null
                        ||
                        brevoApiKey.isBlank()
        ) {


            throw new MailSendException(
                    "BREVO_API_KEY tanımlı değil."
            );
        }


        if (
                senderEmail == null
                        ||
                        senderEmail.isBlank()
        ) {


            throw new MailSendException(
                    "BREVO_SENDER_EMAIL tanımlı değil."
            );
        }


        // =====================================================
        // EMAIL BODY
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


        // =====================================================
        // REQUEST BODY
        // =====================================================

        Map<String, Object> requestBody =
                Map.of(

                        "sender",
                        Map.of(
                                "name",
                                senderName,

                                "email",
                                senderEmail
                        ),

                        "to",
                        List.of(
                                Map.of(
                                        "email",
                                        recipientEmail,

                                        "name",
                                        teacherName
                                )
                        ),

                        "subject",
                        "Course Platform - Öğretmenlik Başvurunuz Onaylandı",

                        "textContent",
                        body
                );


        // =====================================================
        // BREVO API
        // =====================================================

        try {


            restClient
                    .post()

                    .uri(
                            "/v3/smtp/email"
                    )

                    .header(
                            "api-key",
                            brevoApiKey
                    )

                    .contentType(
                            MediaType.APPLICATION_JSON
                    )

                    .accept(
                            MediaType.APPLICATION_JSON
                    )

                    .body(
                            requestBody
                    )

                    .retrieve()

                    .toBodilessEntity();


        } catch (
                Exception exception
        ) {


            throw new MailSendException(

                    "Brevo üzerinden e-posta gönderilemedi.",

                    exception
            );
        }
    }
}