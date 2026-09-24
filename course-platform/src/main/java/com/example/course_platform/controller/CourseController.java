package com.example.course_platform.controller;

import com.example.course_platform.dto.PublicCourseResponse;
import com.example.course_platform.entity.Course;
import com.example.course_platform.service.CourseService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;


    // =========================================================
    // PUBLIC KURS LİSTESİ
    // =========================================================

    @GetMapping
    public ResponseEntity<List<PublicCourseResponse>> getAllCourses() {

        return ResponseEntity.ok(
                courseService.getAllCourses()
        );
    }


    // =========================================================
    // ÖĞRETMENİN TÜM KURSLARI
    // =========================================================

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Course>> getAllCoursesForTeacher(
            @PathVariable Long teacherId) {

        return ResponseEntity.ok(
                courseService.getAllCoursesForTeacher(
                        teacherId
                )
        );
    }


    // =========================================================
    // ÖĞRETMENİN TEK KURSUNU GETİR
    // =========================================================

    @GetMapping("/{courseId}/teacher/{teacherId}")
    public ResponseEntity<?> getCourseForTeacher(
            @PathVariable Long courseId,
            @PathVariable Long teacherId) {

        try {

            return ResponseEntity.ok(
                    courseService.getCourseForTeacher(
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


    // =========================================================
    // YENİ KURS OLUŞTUR
    // =========================================================

    @PostMapping("/teacher/{teacherId}")
    public ResponseEntity<Course> createCourseForTeacher(
            @PathVariable Long teacherId,
            @RequestBody Course course) {

        return ResponseEntity.ok(
                courseService.createCourseForTeacher(
                        teacherId,
                        course
                )
        );
    }


    // =========================================================
    // KURS GÜNCELLE
    // =========================================================

    @PutMapping("/{courseId}/teacher/{teacherId}")
    public ResponseEntity<?> updateCourseForTeacher(
            @PathVariable Long courseId,
            @PathVariable Long teacherId,
            @RequestBody Course course) {

        try {

            return ResponseEntity.ok(
                    courseService.updateCourseForTeacher(
                            teacherId,
                            courseId,
                            course
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
    // KURSU SATIŞA AÇ / KAPAT
    // =========================================================

    @PatchMapping("/{courseId}/teacher/{teacherId}/purchasable")
    public ResponseEntity<?> setCoursePurchasable(
            @PathVariable Long courseId,
            @PathVariable Long teacherId,
            @RequestBody Map<String, Boolean> body) {

        Boolean purchasable =
                body.get("purchasable");


        if (purchasable == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "purchasable alanı zorunludur."
                            )
                    );
        }


        try {

            return ResponseEntity.ok(
                    courseService.setCoursePurchasable(
                            teacherId,
                            courseId,
                            purchasable
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

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
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


    // =========================================================
    // KURSA VIDEO EKLE / VIDEO DEĞİŞTİR
    // =========================================================

    @PatchMapping("/{courseId}/teacher/{teacherId}/video")
    public ResponseEntity<?> updateCourseVideo(
            @PathVariable Long courseId,
            @PathVariable Long teacherId,
            @RequestBody Map<String, String> body) {

        String videoPath =
                body.get("videoPath");


        if (
                videoPath == null
                        ||
                        videoPath.isBlank()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "videoPath alanı zorunludur."
                            )
                    );
        }


        try {

            return ResponseEntity.ok(
                    courseService.updateCourseVideo(
                            teacherId,
                            courseId,
                            videoPath
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


    // =========================================================
    // KURS SİL
    // =========================================================

    @DeleteMapping("/{courseId}/teacher/{teacherId}")
    public ResponseEntity<?> deleteCourseForTeacher(
            @PathVariable Long courseId,
            @PathVariable Long teacherId) {

        try {

            courseService.deleteCourseForTeacher(
                    teacherId,
                    courseId
            );

            return ResponseEntity.ok().build();

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
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


    // =========================================================
    // ÖĞRENCİNİN KURSLARI
    //
    // Course entity yerine güvenli DTO döner.
    // Ownership kontrolü CourseService içerisinde yapılır.
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PublicCourseResponse>>
    getCoursesForStudent(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                courseService.getCoursesForStudent(
                        studentId
                )
        );
    }


    // =========================================================
    // PRIVATE KURS VİDEOSU
    //
    // 1) Giriş yapan kullanıcı STUDENT olmalı.
    // 2) studentId JWT kullanıcısının ID'siyle eşleşmeli.
    // 3) Öğrenci kursa kayıtlı olmalı.
    // =========================================================

    @GetMapping("/{courseId}/video/student/{studentId}")
    public ResponseEntity<?> getCourseVideo(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {

        try {

            String videoUrl =
                    courseService.getSignedVideoUrlForStudent(
                            studentId,
                            courseId
                    );


            return ResponseEntity.ok(
                    Map.of(
                            "videoUrl",
                            videoUrl
                    )
            );

        } catch (AccessDeniedException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "error",
                                    "Forbidden",
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "error",
                                    "Access denied",
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "error",
                                    "Not found",
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    "Video access failed",
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}