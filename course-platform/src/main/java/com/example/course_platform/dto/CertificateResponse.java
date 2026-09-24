package com.example.course_platform.dto;

import java.time.LocalDateTime;

public record CertificateResponse(

        Long certificateId,

        Long courseId,

        String courseName,

        String imageUrl,

        String certificateCode,

        LocalDateTime issuedAt

) {
}