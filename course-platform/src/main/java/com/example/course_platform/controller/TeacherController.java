package com.example.course_platform.controller;

import com.example.course_platform.dto.TeacherProfileResponse;
import com.example.course_platform.dto.TeacherProfileUpdateRequest;

import com.example.course_platform.entity.Teacher;

import com.example.course_platform.service.TeacherService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;


    // =========================================================
    // TÜM ÖĞRETMENLER
    //
    // SecurityConfig tarafından dış erişime kapalı.
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Teacher>>
    getTeachers() {

        return ResponseEntity.ok(
                teacherService
                        .getTeachers()
        );
    }


    // =========================================================
    // ÖĞRETMEN OLUŞTUR
    //
    // SecurityConfig tarafından dış erişime kapalı.
    // =========================================================

    @PostMapping
    public ResponseEntity<Teacher>
    createTeacher(
            @RequestBody Teacher teacher) {

        Teacher createdTeacher =
                teacherService
                        .createTeacher(
                                teacher
                        );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        createdTeacher
                );
    }


    // =========================================================
    // ESKİ GENEL ÖĞRETMEN GÜNCELLEME
    //
    // SecurityConfig tarafından dış erişime kapalı.
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?>
    updateTeacher(
            @PathVariable Long id,
            @RequestBody Teacher teacher) {

        try {

            Teacher updatedTeacher =
                    teacherService
                            .updateTeacher(
                                    id,
                                    teacher
                            );


            return ResponseEntity.ok(
                    updatedTeacher
            );


        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );


        } catch (
                RuntimeException e
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
    // ÖĞRETMEN SİL
    //
    // SecurityConfig tarafından dış erişime kapalı.
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?>
    deleteTeacher(
            @PathVariable Long id) {

        try {

            teacherService
                    .deleteTeacher(
                            id
                    );


            return ResponseEntity
                    .noContent()
                    .build();


        } catch (
                RuntimeException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // ÖĞRETMEN PROFİLİNİ GETİR
    // =========================================================

    @GetMapping("/{teacherId}/profile")
    public ResponseEntity<?>
    getTeacherProfile(
            @PathVariable Long teacherId) {

        try {

            TeacherProfileResponse response =
                    teacherService
                            .getTeacherProfile(
                                    teacherId
                            );


            return ResponseEntity.ok(
                    response
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
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );


        } catch (
                RuntimeException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "message",
                                    "Öğretmen profili alınırken bir hata oluştu."
                            )
                    );
        }
    }


    // =========================================================
    // ÖĞRETMEN PROFİLİNİ GÜNCELLE
    // =========================================================

    @PutMapping("/{teacherId}/profile")
    public ResponseEntity<?>
    updateTeacherProfile(
            @PathVariable Long teacherId,
            @RequestBody TeacherProfileUpdateRequest request) {

        try {

            TeacherProfileResponse response =
                    teacherService
                            .updateTeacherProfile(
                                    teacherId,
                                    request
                            );


            return ResponseEntity.ok(
                    response
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
                IllegalStateException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.CONFLICT
                    )
                    .body(
                            Map.of(
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


        } catch (
                RuntimeException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "message",
                                    "Profil güncellenirken beklenmeyen bir hata oluştu."
                            )
                    );
        }
    }
}