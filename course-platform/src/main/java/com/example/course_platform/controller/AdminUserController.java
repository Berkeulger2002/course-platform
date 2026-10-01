package com.example.course_platform.controller;


import com.example.course_platform.dto.AdminUserMailRequest;
import com.example.course_platform.dto.AdminUserResponse;
import com.example.course_platform.dto.AdminUserSummaryResponse;

import com.example.course_platform.service.AdminUserMailService;
import com.example.course_platform.service.AdminUserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(
        "/api/admin/users"
)
@RequiredArgsConstructor
public class AdminUserController {


    private final AdminUserService
            adminUserService;


    private final AdminUserMailService
            adminUserMailService;


    // =========================================================
    // GET ALL USERS
    //
    // GET /api/admin/users
    // =========================================================

    @GetMapping
    public ResponseEntity<
            List<AdminUserResponse>
            >
    getUsers() {


        return ResponseEntity.ok(

                adminUserService
                        .getUsers()
        );
    }


    // =========================================================
    // USER SUMMARY
    //
    // GET /api/admin/users/summary
    // =========================================================

    @GetMapping(
            "/summary"
    )
    public ResponseEntity<
            AdminUserSummaryResponse
            >
    getSummary() {


        return ResponseEntity.ok(

                adminUserService
                        .getSummary()
        );
    }


    // =========================================================
    // SEND EMAIL TO STUDENT / TEACHER
    //
    // POST /api/admin/users/{userId}/email
    // =========================================================

    @PostMapping(
            "/{userId}/email"
    )
    public ResponseEntity<Void>
    sendEmail(

            @PathVariable
            Long userId,

            @Valid
            @RequestBody
            AdminUserMailRequest request
    ) {


        adminUserMailService
                .sendEmail(

                        userId,

                        request
                );


        return ResponseEntity
                .noContent()
                .build();
    }
}