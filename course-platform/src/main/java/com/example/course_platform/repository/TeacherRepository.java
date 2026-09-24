package com.example.course_platform.repository;

import com.example.course_platform.entity.Teacher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TeacherRepository
        extends JpaRepository<Teacher, Long> {


    // =========================================================
    // EMAIL BAŞKA BİR ÖĞRETMEN TARAFINDAN KULLANILIYOR MU?
    // =========================================================

    boolean existsByEmailAndIdNot(
            String email,
            Long id
    );
}