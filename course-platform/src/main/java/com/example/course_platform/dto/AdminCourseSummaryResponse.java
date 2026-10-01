package com.example.course_platform.dto;


public record AdminCourseSummaryResponse(

        long totalCourses,

        long purchasableCourses,

        long closedCourses,

        long totalEnrollments

) {
}