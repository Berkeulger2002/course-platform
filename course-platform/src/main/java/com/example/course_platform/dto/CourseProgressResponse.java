package com.example.course_platform.dto;

import java.time.LocalDateTime;

public record CourseProgressResponse(

        Long courseId,

        String courseName,

        String imageUrl,

        double lastPositionSeconds,

        double maxPositionSeconds,

        double durationSeconds,

        double progressPercentage,

        boolean completed,

        LocalDateTime lastWatchedAt

) {
}