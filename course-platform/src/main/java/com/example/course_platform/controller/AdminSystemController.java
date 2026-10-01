package com.example.course_platform.controller;


import com.example.course_platform.dto.AdminSystemResponse;

import com.example.course_platform.service.AdminSystemService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(
        "/api/admin/system"
)
@RequiredArgsConstructor
public class AdminSystemController {


    private final AdminSystemService
            adminSystemService;


    // =========================================================
    // SYSTEM STATUS
    //
    // GET /api/admin/system
    // =========================================================

    @GetMapping
    public ResponseEntity<
            AdminSystemResponse
            >
    getSystemStatus() {


        return ResponseEntity.ok(

                adminSystemService
                        .getSystemStatus()
        );
    }
}