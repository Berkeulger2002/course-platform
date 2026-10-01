package com.example.course_platform.dto;


public record AdminUserSummaryResponse(

        long totalUsers,

        long students,

        long teachers,

        long admins

) {
}