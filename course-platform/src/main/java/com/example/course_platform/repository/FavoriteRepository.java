package com.example.course_platform.repository;

import com.example.course_platform.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository
        extends JpaRepository<Favorite, Long> {

    List<Favorite> findAllByStudentId(
            Long studentId
    );

    Optional<Favorite> findByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );

    boolean existsByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );
}