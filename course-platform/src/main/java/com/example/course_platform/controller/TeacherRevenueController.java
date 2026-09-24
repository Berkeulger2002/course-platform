package com.example.course_platform.controller;

import com.example.course_platform.dto.TeacherRevenueResponse;
import com.example.course_platform.service.TeacherRevenueService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher-revenue")
@RequiredArgsConstructor
public class TeacherRevenueController {

    private final TeacherRevenueService teacherRevenueService;


    // =========================================================
    // ÖĞRETMENİN TÜM SATIŞLARI
    //
    // GET /api/teacher-revenue/teacher/1
    // =========================================================

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherRevenueResponse>>
    getTeacherPayments(
            @PathVariable Long teacherId) {

        return ResponseEntity.ok(
                teacherRevenueService
                        .getTeacherPayments(
                                teacherId
                        )
        );
    }


    // =========================================================
    // BELİRLİ KURSUN SATIŞLARI
    //
    // GET
    // /api/teacher-revenue/teacher/1/course/5
    // =========================================================

    @GetMapping(
            "/teacher/{teacherId}/course/{courseId}"
    )
    public ResponseEntity<?> getCoursePayments(
            @PathVariable Long teacherId,
            @PathVariable Long courseId) {

        try {

            return ResponseEntity.ok(
                    teacherRevenueService
                            .getCoursePayments(
                                    teacherId,
                                    courseId
                            )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}