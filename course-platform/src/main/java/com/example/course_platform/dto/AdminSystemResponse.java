package com.example.course_platform.dto;


import java.time.LocalDateTime;

import java.util.List;


public record AdminSystemResponse(

        String overallStatus,

        LocalDateTime serverTime,

        long uptimeSeconds,

        String javaVersion,

        long usedMemoryMb,

        long maxMemoryMb,

        String flywayVersion,

        List<AdminSystemComponentResponse> components

) {
}