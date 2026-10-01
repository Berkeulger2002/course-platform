package com.example.course_platform.controller;


import com.example.course_platform.dto.AdminCourseResponse;
import com.example.course_platform.dto.AdminCourseSummaryResponse;

import com.example.course_platform.service.AdminCourseService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(
        "/api/admin/courses"
)
@RequiredArgsConstructor
public class AdminCourseController {


    private final AdminCourseService
            adminCourseService;


    // =========================================================
    // TÜM KURSLAR
    //
    // GET /api/admin/courses
    // =========================================================

    @GetMapping
    public ResponseEntity<
            List<AdminCourseResponse>
            >
    getCourses() {


        return ResponseEntity.ok(

                adminCourseService
                        .getCourses()
        );
    }


    // =========================================================
    // COURSE SUMMARY
    //
    // GET /api/admin/courses/summary
    // =========================================================

    @GetMapping(
            "/summary"
    )
    public ResponseEntity<
            AdminCourseSummaryResponse
            >
    getSummary() {


        return ResponseEntity.ok(

                adminCourseService
                        .getSummary()
        );
    }
}