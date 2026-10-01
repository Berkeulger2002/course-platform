package com.example.course_platform.dto;


public record AdminSystemComponentResponse(

        String name,

        String status,

        Long responseTimeMs,

        String detail

) {
}