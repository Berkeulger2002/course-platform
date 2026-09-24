package com.example.course_platform.dto;

import java.time.LocalDateTime;

public record TeacherStudentResponse(

        Long studentId,

        String studentName,

        String studentEmail,

        Long courseId,

        String courseName,

        double progressPercentage,

        boolean completed,

        LocalDateTime lastWatchedAt
) {
}