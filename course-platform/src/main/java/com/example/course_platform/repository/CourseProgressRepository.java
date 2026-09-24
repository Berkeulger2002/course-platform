package com.example.course_platform.repository;

import com.example.course_platform.entity.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseProgressRepository
        extends JpaRepository<CourseProgress, Long> {

    Optional<CourseProgress>
    findByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );


    List<CourseProgress>
    findAllByStudentIdOrderByLastWatchedAtDesc(
            Long studentId
    );
}