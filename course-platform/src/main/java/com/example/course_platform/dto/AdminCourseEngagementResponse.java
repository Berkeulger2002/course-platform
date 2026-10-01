package com.example.course_platform.dto;


public record AdminCourseEngagementResponse(

        Long courseId,

        String courseName,

        long progressRecords,

        long completedRecords,

        double completionRate,

        double averageProgress,

        long reviewCount,

        double averageRating

) {
}