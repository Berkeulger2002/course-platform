package com.example.course_platform.dto;


import java.math.BigDecimal;


public record AdminCourseResponse(

        Long id,

        String name,

        BigDecimal price,

        int maxCapacity,

        int currentEnrolled,

        boolean purchasable,

        Long teacherId,

        String teacherName

) {
}