package com.example.course_platform.service;


import com.example.course_platform.dto.AdminUserMailRequest;

import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.User;

import com.example.course_platform.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;


@Service
public class AdminUserMailService {


    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final UserRepository
            userRepository;


    private final JavaMailSender
            mailSender;


    private final String
            mailFrom;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AdminUserMailService(

            UserRepository userRepository,

            JavaMailSender mailSender,

            @Value(
                    "${app.mail.from:}"
            )
            String mailFrom
    ) {


        this.userRepository =
                userRepository;


        this.mailSender =
                mailSender;


        this.mailFrom =
                mailFrom;
    }


    // =========================================================
    // SEND ADMIN EMAIL TO USER
    // =========================================================

    public void sendEmail(

            Long userId,

            AdminUserMailRequest request
    ) {


        // =====================================================
        // USER
        // =====================================================

        User user =

                userRepository
                        .findById(
                                userId
                        )

                        .orElseThrow(
                                () ->

                                        new ResponseStatusException(

                                                HttpStatus.NOT_FOUND,

                                                "Kullanıcı bulunamadı."
                                        )
                        );


        // =====================================================
        // ONLY STUDENT / TEACHER
        // =====================================================

        if (
                user.getRole()
                        !=
                        Role.STUDENT

                        &&

                        user.getRole()
                                !=
                                Role.TEACHER
        ) {


            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Yalnızca öğrenci ve öğretmenlere mail gönderilebilir."
            );
        }


        // =====================================================
        // SUBJECT
        // =====================================================

        String subject =

                request
                        .subject()
                        .trim();


        // =====================================================
        // HEADER INJECTION PROTECTION
        // =====================================================

        if (
                subject.contains(
                        "\r"
                )

                        ||

                        subject.contains(
                                "\n"
                        )
        ) {


            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Geçersiz mail konusu."
            );
        }


        // =====================================================
        // MESSAGE
        // =====================================================

        String adminMessage =

                request
                        .message()
                        .trim();


        // =====================================================
        // MAIL
        // =====================================================

        SimpleMailMessage mailMessage =
                new SimpleMailMessage();


        // =====================================================
        // FROM
        // =====================================================

        if (
                mailFrom != null

                        &&

                        !mailFrom.isBlank()
        ) {


            mailMessage.setFrom(
                    mailFrom
            );
        }


        // =====================================================
        // TO
        //
        // Email frontend'den GELMİYOR.
        // Doğrudan DB'deki kullanıcı hesabından okunuyor.
        // =====================================================

        mailMessage.setTo(
                user.getEmail()
        );


        // =====================================================
        // SUBJECT
        // =====================================================

        mailMessage.setSubject(

                "Course Platform - "
                        +
                        subject
        );


        // =====================================================
        // BODY
        // =====================================================

        String body =

                "Merhaba "
                        +
                        user.getName()
                        +
                        ",\n\n"

                        +
                        adminMessage
                        +
                        "\n\n"

                        +
                        "Herhangi bir sorunuz olması durumunda "
                        +
                        "bu e-postaya dönüş yapabilirsiniz.\n\n"

                        +
                        "İyi günler,\n"
                        +
                        "Course Platform Yönetimi";


        mailMessage.setText(
                body
        );


        // =====================================================
        // SEND
        // =====================================================

        try {


            mailSender.send(
                    mailMessage
            );


        } catch (
                MailException exception
        ) {


            throw new ResponseStatusException(

                    HttpStatus.BAD_GATEWAY,

                    "Kullanıcıya e-posta gönderilemedi.",

                    exception
            );
        }
    }
}