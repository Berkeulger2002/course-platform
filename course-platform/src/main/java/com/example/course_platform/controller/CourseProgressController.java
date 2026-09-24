package com.example.course_platform.controller;

import com.example.course_platform.dto.CourseProgressResponse;
import com.example.course_platform.dto.CourseProgressUpdateRequest;
import com.example.course_platform.service.CourseProgressService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class CourseProgressController {

    private final CourseProgressService progressService;


    // =========================================================
    // İLERLEME GÜNCELLE
    //
    // PUT
    // /api/progress/student/5/course/10
    // =========================================================
    @PutMapping(
            "/student/{studentId}/course/{courseId}"
    )
    public ResponseEntity<?> updateProgress(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestBody
            CourseProgressUpdateRequest request) {

        try {

            CourseProgressResponse response =
                    progressService.updateProgress(
                            studentId,
                            courseId,
                            request
                    );


            return ResponseEntity.ok(
                    response
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
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // TEK KURSUN İLERLEMESİ
    // =========================================================
    @GetMapping(
            "/student/{studentId}/course/{courseId}"
    )
    public ResponseEntity<CourseProgressResponse>
    getCourseProgress(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                progressService
                        .getCourseProgress(
                                studentId,
                                courseId
                        )
        );
    }


    // =========================================================
    // TÜM İLERLEMELER / İZLEME GEÇMİŞİ
    // =========================================================
    @GetMapping(
            "/student/{studentId}"
    )
    public ResponseEntity<
            List<CourseProgressResponse>
            >
    getStudentProgress(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                progressService
                        .getStudentProgress(
                                studentId
                        )
        );
    }
}