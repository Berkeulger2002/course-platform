package com.example.course_platform.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TeacherRevenueResponse(

        Long paymentId,

        Long studentId,

        String studentName,

        String studentEmail,

        Long courseId,

        String courseName,

        BigDecimal amount,

        String status,

        String paymentMethod,

        String transactionCode,

        LocalDateTime purchasedAt

) {
}