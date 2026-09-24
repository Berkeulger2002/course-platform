package com.example.course_platform.dto;

public record CourseProgressUpdateRequest(

        double currentTime,

        double duration

) {
}