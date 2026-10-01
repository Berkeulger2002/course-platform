package com.example.course_platform.dto;


public record AdminUserActivitySummaryResponse(

        long onlineNow,

        long onlineStudents,

        long onlineTeachers,

        long todayLogins,

        long averageActiveSeconds

) {
}