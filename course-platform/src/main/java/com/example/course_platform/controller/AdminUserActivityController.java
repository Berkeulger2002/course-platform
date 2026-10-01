package com.example.course_platform.controller;


import com.example.course_platform.dto.AdminUserActivityResponse;
import com.example.course_platform.dto.AdminUserActivitySummaryResponse;

import com.example.course_platform.service.AdminUserActivityService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(
        "/api/admin/user-activity"
)
@RequiredArgsConstructor
public class AdminUserActivityController {


    private final AdminUserActivityService
            adminUserActivityService;


    // =========================================================
    // SUMMARY
    // =========================================================

    @GetMapping(
            "/summary"
    )
    public ResponseEntity<
            AdminUserActivitySummaryResponse
            >
    getSummary() {


        return ResponseEntity.ok(

                adminUserActivityService
                        .getSummary()
        );
    }


    // =========================================================
    // RECENT
    // =========================================================

    @GetMapping(
            "/recent"
    )
    public ResponseEntity<
            List<AdminUserActivityResponse>
            >
    getRecentActivities() {


        return ResponseEntity.ok(

                adminUserActivityService
                        .getRecentActivities()
        );
    }
}