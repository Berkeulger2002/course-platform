package com.example.course_platform.controller;

import com.example.course_platform.dto.StudentProfileResponse;
import com.example.course_platform.dto.StudentProfileUpdateRequest;
import com.example.course_platform.entity.Student;
import com.example.course_platform.service.StudentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;


    // =========================================================
    // ÖĞRENCİ EKLE
    // =========================================================
    @PostMapping("/add")
    public ResponseEntity<Student> addCustomer(
            @RequestBody Student student) {

        return ResponseEntity.ok(
                studentService.addCustomer(
                        student
                )
        );
    }


    // =========================================================
    // PROFİL GETİR
    // =========================================================
    @GetMapping("/{studentId}/profile")
    public ResponseEntity<StudentProfileResponse> getProfile(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                studentService.getProfile(
                        studentId
                )
        );
    }


    // =========================================================
    // PROFİL GÜNCELLE
    // =========================================================
    @PutMapping("/{studentId}/profile")
    public ResponseEntity<StudentProfileResponse> updateProfile(
            @PathVariable Long studentId,
            @RequestBody StudentProfileUpdateRequest request) {

        return ResponseEntity.ok(
                studentService.updateProfile(
                        studentId,
                        request
                )
        );
    }
}