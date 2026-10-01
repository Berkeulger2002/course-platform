package com.example.course_platform.dto;


import java.math.BigDecimal;


public record AdminOrderItemResponse(

        Long id,

        Long courseId,

        String courseName,

        Long teacherId,

        String teacherName,

        BigDecimal priceAtPurchase

) {
}