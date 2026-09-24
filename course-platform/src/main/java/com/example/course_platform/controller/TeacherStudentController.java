package com.example.course_platform.controller;

import com.example.course_platform.dto.TeacherStudentResponse;
import com.example.course_platform.service.TeacherStudentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher-students")
@RequiredArgsConstructor
public class TeacherStudentController {

    private final TeacherStudentService teacherStudentService;


    // =========================================================
    // ÖĞRETMENİN TÜM ÖĞRENCİLERİ
    //
    // GET /api/teacher-students/teacher/1
    // =========================================================
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherStudentResponse>>
    getTeacherStudents(
            @PathVariable Long teacherId) {

        return ResponseEntity.ok(
                teacherStudentService
                        .getTeacherStudents(
                                teacherId
                        )
        );
    }


    // =========================================================
    // BELİRLİ KURSUN ÖĞRENCİLERİ
    // =========================================================
    @GetMapping(
            "/teacher/{teacherId}/course/{courseId}"
    )
    public ResponseEntity<?> getCourseStudents(
            @PathVariable Long teacherId,
            @PathVariable Long courseId) {

        try {

            return ResponseEntity.ok(
                    teacherStudentService
                            .getCourseStudents(
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