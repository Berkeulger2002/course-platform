package com.example.course_platform.dto;


public record AuthMeResponse(

        Long id,

        String name,

        String email,

        String role

) {
}