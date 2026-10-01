package com.example.course_platform.controller;


import com.example.course_platform.dto.AdminSessionSummaryResponse;
import com.example.course_platform.dto.AdminUserSessionResponse;

import com.example.course_platform.service.AdminSessionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@RequestMapping(
        "/api/admin/sessions"
)
@RequiredArgsConstructor
public class AdminSessionController {


    private final AdminSessionService
            adminSessionService;


    // =========================================================
    // SESSION HISTORY
    //
    // GET /api/admin/sessions
    // =========================================================

    @GetMapping
    public ResponseEntity<
            List<AdminUserSessionResponse>
            >
    getSessions() {


        return ResponseEntity.ok(

                adminSessionService
                        .getSessions()
        );
    }


    // =========================================================
    // SUMMARY
    //
    // GET /api/admin/sessions/summary
    // =========================================================

    @GetMapping(
            "/summary"
    )
    public ResponseEntity<
            AdminSessionSummaryResponse
            >
    getSummary() {


        return ResponseEntity.ok(

                adminSessionService
                        .getSummary()
        );
    }
}