package com.example.course_platform.dto;


import java.math.BigDecimal;


public record AdminOrderSummaryResponse(

        long totalOrders,

        BigDecimal totalRevenue,

        long totalItemsSold,

        BigDecimal averageOrderValue

) {
}