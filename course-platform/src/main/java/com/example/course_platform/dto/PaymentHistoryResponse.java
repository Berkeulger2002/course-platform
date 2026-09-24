package com.example.course_platform.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentHistoryResponse(

        Long id,

        Long courseId,

        String courseName,

        String imageUrl,

        BigDecimal amount,

        String status,

        String paymentMethod,

        String transactionCode,

        LocalDateTime purchasedAt

) {
}