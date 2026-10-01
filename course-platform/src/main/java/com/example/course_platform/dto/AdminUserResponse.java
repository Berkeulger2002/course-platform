package com.example.course_platform.dto;


public record AdminUserResponse(

        Long id,

        String name,

        String email,

        String role

) {
}