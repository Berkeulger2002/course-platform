package com.example.course_platform.dto;

import java.math.BigDecimal;

public record PublicCourseResponse(

        Long id,

        String name,

        String description,

        BigDecimal price,

        String imageUrl,

        int maxCapacity,

        int currentEnrolled,

        boolean purchasable,

        TeacherSummary teacher

) {

    public record TeacherSummary(
            String name
    ) {
    }
}