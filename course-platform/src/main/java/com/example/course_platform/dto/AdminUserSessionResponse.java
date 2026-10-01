package com.example.course_platform.dto;


import java.time.LocalDateTime;


public record AdminUserSessionResponse(

        Long id,

        Long userId,

        String userName,

        String userEmail,

        String role,

        LocalDateTime loginAt,

        LocalDateTime lastSeenAt,

        LocalDateTime logoutAt,

        long activeSeconds,

        String status,

        boolean online

) {
}