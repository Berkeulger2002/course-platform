package com.example.course_platform.dto;

import java.math.BigDecimal;

public record OrderResponse(

        Long id,

        String orderCode,

        BigDecimal totalPrice

) {
}