package com.example.course_platform.config;


import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.User;

import com.example.course_platform.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;


import java.util.Locale;
import java.util.Optional;


@Component
public class AdminBootstrap
        implements CommandLineRunner {


    // =========================================================
    // LOGGER
    // =========================================================

    private static final Logger log =
            LoggerFactory.getLogger(
                    AdminBootstrap.class
            );


    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final UserRepository
            userRepository;

    private final PasswordEncoder
            passwordEncoder;


    // =========================================================
    // ADMIN CONFIG
    //
    // Bunlar Git'e girmez.
    //
    // Local:
    // ~/.course-platform-secrets.properties
    //
    // Production:
    // Render Environment Variables
    // =========================================================

    @Value("${ADMIN_NAME:}")
    private String adminName;


    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;


    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AdminBootstrap(

            UserRepository userRepository,

            PasswordEncoder passwordEncoder
    ) {


        this.userRepository =
                userRepository;


        this.passwordEncoder =
                passwordEncoder;
    }


    // =========================================================
    // STARTUP
    // =========================================================

    @Override
    public void run(
            String... args
    ) {


        String name =
                adminName == null
                        ?
                        ""
                        :
                        adminName.trim();


        String email =
                adminEmail == null
                        ?
                        ""
                        :
                        adminEmail
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );


        String password =
                adminPassword == null
                        ?
                        ""
                        :
                        adminPassword;


        // =====================================================
        // ADMIN CONFIG YOKSA HİÇBİR ŞEY YAPMA
        // =====================================================

        if (
                name.isBlank()
                        &&
                        email.isBlank()
                        &&
                        password.isBlank()
        ) {


            return;
        }


        // =====================================================
        // ÜÇ DEĞER DE BİRLİKTE OLMALI
        // =====================================================

        if (
                name.isBlank()
                        ||
                        email.isBlank()
                        ||
                        password.isBlank()
        ) {


            throw new IllegalStateException(

                    "Admin bootstrap için "
                            +
                            "ADMIN_NAME, ADMIN_EMAIL ve ADMIN_PASSWORD "
                            +
                            "birlikte tanımlanmalıdır."
            );
        }


        // =====================================================
        // ADMIN ŞİFRE KONTROLÜ
        // =====================================================

        if (
                password.length() < 12
        ) {


            throw new IllegalStateException(

                    "ADMIN_PASSWORD en az 12 karakter olmalıdır."
            );
        }


        // =====================================================
        // SİSTEMDE ZATEN ADMIN VAR MI?
        //
        // Amaç:
        // yalnızca 1 admin hesabı.
        // =====================================================

        Optional<User> existingAdmin =

                userRepository
                        .findAll()
                        .stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                == Role.ADMIN
                        )
                        .findFirst();


        if (
                existingAdmin.isPresent()
        ) {


            User admin =
                    existingAdmin.get();


            // =================================================
            // AYNI ADMIN ZATEN VAR
            //
            // Tekrar password değiştirmiyoruz.
            // =================================================

            if (
                    admin.getEmail()
                            .equalsIgnoreCase(
                                    email
                            )
            ) {


                log.info(
                        "ADMIN account already exists. Bootstrap skipped."
                );


                return;
            }


            // =================================================
            // FARKLI BİR ADMIN ZATEN VAR
            // =================================================

            throw new IllegalStateException(

                    "Sistemde zaten başka bir ADMIN hesabı bulunmaktadır."
            );
        }


        // =====================================================
        // EMAIL BAŞKA BİR HESABA AİT Mİ?
        // =====================================================

        Optional<User> existingUser =

                userRepository
                        .findByEmailIgnoreCase(
                                email
                        );


        if (
                existingUser.isPresent()
        ) {


            throw new IllegalStateException(

                    "ADMIN_EMAIL başka bir kullanıcı hesabı tarafından kullanılıyor."
            );
        }


        // =====================================================
        // ADMIN OLUŞTUR
        // =====================================================

        User admin =
                new User();


        admin.setName(
                name
        );


        admin.setEmail(
                email
        );


        // =====================================================
        // ŞİFRE DÜZ METİN OLARAK DB'YE YAZILMAZ
        //
        // BCrypt hash olarak tutulur.
        // =====================================================

        admin.setPassword(

                passwordEncoder.encode(
                        password
                )
        );


        admin.setRole(
                Role.ADMIN
        );


        userRepository.save(
                admin
        );


        log.info(
                "ADMIN account created successfully."
        );
    }
}