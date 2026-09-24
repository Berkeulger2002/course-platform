package com.example.course_platform.repository;

import com.example.course_platform.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {


    // =========================================================
    // EMAIL İLE KULLANICI BUL
    // =========================================================

    Optional<User> findByEmail(String email);


    // =========================================================
    // BÜYÜK / KÜÇÜK HARF DUYARSIZ EMAIL ARAMA
    // =========================================================
    //
    // Örnek:
    //
    // osman@gmail.com
    // OSMAN@GMAIL.COM
    //
    // aynı kullanıcı olarak değerlendirilir.
    //
    // =========================================================

    Optional<User> findByEmailIgnoreCase(String email);


    // =========================================================
    // EMAIL DAHA ÖNCE KULLANILMIŞ MI?
    // =========================================================

    boolean existsByEmailIgnoreCase(String email);
}