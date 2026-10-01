package com.example.course_platform.controller;


import com.example.course_platform.dto.AdminEngagementResponse;

import com.example.course_platform.service.AdminEngagementService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(
        "/api/admin/engagement"
)
@RequiredArgsConstructor
public class AdminEngagementController {


    private final AdminEngagementService
            adminEngagementService;


    // =========================================================
    // ENGAGEMENT OVERVIEW
    //
    // GET /api/admin/engagement
    // =========================================================

    @GetMapping
    public ResponseEntity<
            AdminEngagementResponse
            >
    getEngagement() {


        return ResponseEntity.ok(

                adminEngagementService
                        .getEngagement()
        );
    }
}