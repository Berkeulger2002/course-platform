package com.example.course_platform.dto;

public record StudentProfileResponse(

        Long id,

        String name,

        String email,

        String role,

        int enrolledCourseCount,

        long completedCourseCount,

        int certificateCount

) {
}