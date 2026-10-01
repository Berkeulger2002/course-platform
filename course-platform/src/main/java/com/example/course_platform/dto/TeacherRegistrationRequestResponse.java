package com.example.course_platform.dto;


import java.time.LocalDateTime;


public record TeacherRegistrationRequestResponse(

        Long id,

        String name,

        String email,

        String status,

        LocalDateTime requestedAt

) {
}