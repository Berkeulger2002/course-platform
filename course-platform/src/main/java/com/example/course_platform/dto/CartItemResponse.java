package com.example.course_platform.dto;

import java.math.BigDecimal;


public record CartItemResponse(

        Long id,

        PublicCourseResponse course,

        BigDecimal priceAtAddition

) {
}