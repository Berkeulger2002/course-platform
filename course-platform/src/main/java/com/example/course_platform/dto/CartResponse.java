package com.example.course_platform.dto;

import java.math.BigDecimal;
import java.util.List;


public record CartResponse(

        Long id,

        List<CartItemResponse> items,

        BigDecimal totalPrice

) {
}