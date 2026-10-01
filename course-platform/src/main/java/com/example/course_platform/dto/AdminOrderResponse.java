package com.example.course_platform.dto;


import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.List;


public record AdminOrderResponse(

        Long id,

        String orderCode,

        Long studentId,

        String studentName,

        String studentEmail,

        BigDecimal totalPrice,

        int itemCount,

        LocalDateTime createdAt,

        List<AdminOrderItemResponse> items

) {
}