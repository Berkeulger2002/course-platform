package com.example.course_platform.controller;

import com.example.course_platform.dto.PublicCourseResponse;
import com.example.course_platform.service.FavoriteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;


    // =========================================================
    // ÖĞRENCİNİN FAVORİLERİ
    //
    // GET /api/favorites/student/8
    //
    // Course entity yerine güvenli DTO döner.
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PublicCourseResponse>>
    getFavorites(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                favoriteService
                        .getFavoriteCourses(
                                studentId
                        )
        );
    }


    // =========================================================
    // FAVORİYE EKLE
    //
    // POST /api/favorites/student/8/course/5
    // =========================================================

    @PostMapping(
            "/student/{studentId}/course/{courseId}"
    )
    public ResponseEntity<?> addFavorite(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        try {

            favoriteService.addFavorite(
                    studentId,
                    courseId
            );


            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Kurs favorilere eklendi."
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
    // FAVORİDEN ÇIKAR
    //
    // DELETE /api/favorites/student/8/course/5
    // =========================================================

    @DeleteMapping(
            "/student/{studentId}/course/{courseId}"
    )
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        try {

            favoriteService.removeFavorite(
                    studentId,
                    courseId
            );


            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Kurs favorilerden çıkarıldı."
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
    // FAVORİDE Mİ?
    //
    // GET /api/favorites/student/8/course/5
    // =========================================================

    @GetMapping(
            "/student/{studentId}/course/{courseId}"
    )
    public ResponseEntity<Map<String, Boolean>>
    isFavorite(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        boolean favorite =
                favoriteService.isFavorite(
                        studentId,
                        courseId
                );


        return ResponseEntity.ok(
                Map.of(
                        "favorite",
                        favorite
                )
        );
    }
}