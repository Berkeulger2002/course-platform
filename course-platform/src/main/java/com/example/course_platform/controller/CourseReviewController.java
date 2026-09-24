package com.example.course_platform.controller;

import com.example.course_platform.document.CourseReview;
import com.example.course_platform.service.CourseReviewService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class CourseReviewController {

    private final CourseReviewService reviewService;


    // =========================================================
    // YENİ YORUM EKLE
    //
    // Sadece STUDENT.
    //
    // studentId / studentName istemciden güvenilir
    // kabul edilmez.
    // =========================================================

    @PostMapping
    public ResponseEntity<?> addReview(
            @RequestBody CourseReview review) {

        try {

            return ResponseEntity.ok(
                    reviewService.addReview(
                            review
                    )
            );


        } catch (
                AccessDeniedException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.FORBIDDEN
                    )
                    .body(
                            Map.of(
                                    "error",
                                    "Forbidden",
                                    "message",
                                    e.getMessage()
                            )
                    );


        } catch (
                IllegalArgumentException e
        ) {

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
    // KURSA AİT YORUMLARI LİSTELE
    //
    // PUBLIC
    // =========================================================

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseReview>>
    getReviewsByCourse(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                reviewService
                        .getReviewsByCourse(
                                courseId
                        )
        );
    }
}