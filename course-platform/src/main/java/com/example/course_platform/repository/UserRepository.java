package com.example.course_platform.repository;


import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {


    // =========================================================
    // EMAIL İLE KULLANICI BUL
    // =========================================================

    Optional<User> findByEmail(
            String email
    );


    // =========================================================
    // CASE INSENSITIVE EMAIL
    // =========================================================

    Optional<User> findByEmailIgnoreCase(
            String email
    );


    // =========================================================
    // EMAIL VAR MI?
    // =========================================================

    boolean existsByEmailIgnoreCase(
            String email
    );


    // =========================================================
    // ADMIN - TÜM KULLANICILAR
    //
    // En yeni ID önce gelir.
    // =========================================================

    List<User> findAllByOrderByIdDesc();


    // =========================================================
    // ADMIN - ROLE COUNT
    // =========================================================

    long countByRole(
            Role role
    );
}