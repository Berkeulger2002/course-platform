package com.example.course_platform.dto;


public record AdminSessionSummaryResponse(

        long totalSessions,

        long onlineNow,

        long onlineStudents,

        long onlineTeachers,

        long todayLogins,

        long averageActiveSeconds

) {
}