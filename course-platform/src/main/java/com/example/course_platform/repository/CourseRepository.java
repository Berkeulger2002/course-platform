package com.example.course_platform.repository;


import com.example.course_platform.entity.Course;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
public interface CourseRepository
        extends JpaRepository<Course, Long> {


    // =========================================================
    // ÖĞRETMENİN KURSLARI
    // =========================================================

    List<Course> findAllByTeacherId(
            Long teacherId
    );


    // =========================================================
    // ADMIN - TÜM KURSLAR
    // =========================================================

    List<Course> findAllByOrderByIdDesc();


    // =========================================================
    // CHECKOUT İÇİN KURSU KİLİTLEYEREK GETİR
    // =========================================================

    @Lock(
            LockModeType.PESSIMISTIC_WRITE
    )
    @Query(
            "SELECT c " +
                    "FROM Course c " +
                    "WHERE c.id = :courseId"
    )
    Optional<Course> findByIdForUpdate(

            @Param("courseId")
            Long courseId
    );
}