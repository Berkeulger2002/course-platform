package com.example.course_platform.repository;

import com.example.course_platform.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository
        extends JpaRepository<Certificate, Long> {

    Optional<Certificate>
    findByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );


    boolean existsByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );


    List<Certificate>
    findAllByStudentIdOrderByIssuedAtDesc(
            Long studentId
    );
}