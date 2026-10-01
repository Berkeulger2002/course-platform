package com.example.course_platform.controller;


import com.example.course_platform.dto.TeacherRegistrationCompleteRequest;
import com.example.course_platform.dto.TeacherRegistrationCreateRequest;
import com.example.course_platform.dto.TeacherRegistrationRequestResponse;

import com.example.course_platform.service.TeacherRegistrationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(
        "/api/teacher-registration"
)
@RequiredArgsConstructor
public class TeacherRegistrationController {


    // =========================================================
    // SERVICE
    // =========================================================

    private final TeacherRegistrationService
            teacherRegistrationService;


    // =========================================================
    // TEACHER REGISTRATION REQUEST
    //
    // POST /api/teacher-registration/request
    //
    // Öğretmen adayı:
    //
    // name
    // email
    //
    // gönderir.
    //
    // Bu aşamada Teacher hesabı oluşturulmaz.
    //
    // Başvuru:
    //
    // PENDING
    //
    // durumuna gelir.
    // =========================================================

    @PostMapping(
            "/request"
    )
    public ResponseEntity<
            TeacherRegistrationRequestResponse
            >
    createRequest(

            @Valid
            @RequestBody
            TeacherRegistrationCreateRequest request
    ) {


        TeacherRegistrationRequestResponse response =

                teacherRegistrationService
                        .createRequest(
                                request
                        );


        return ResponseEntity

                .status(
                        HttpStatus.CREATED
                )

                .body(
                        response
                );
    }


    // =========================================================
    // COMPLETE TEACHER REGISTRATION
    //
    // POST /api/teacher-registration/complete
    //
    // Admin başvuruyu onaylayıp doğrulama kodunu
    // gönderdikten sonra öğretmen:
    //
    // email
    // verificationCode
    // password
    // confirmPassword
    //
    // gönderir.
    //
    // Kod doğru ve süresi geçmemişse:
    //
    // Teacher hesabı oluşturulur.
    //
    // Başvuru:
    //
    // CODE_SENT
    //      ↓
    // COMPLETED
    //
    // durumuna gelir.
    // =========================================================

    @PostMapping(
            "/complete"
    )
    public ResponseEntity<
            TeacherRegistrationRequestResponse
            >
    completeRegistration(

            @Valid
            @RequestBody
            TeacherRegistrationCompleteRequest request
    ) {


        TeacherRegistrationRequestResponse response =

                teacherRegistrationService
                        .completeRegistration(
                                request
                        );


        return ResponseEntity

                .status(
                        HttpStatus.CREATED
                )

                .body(
                        response
                );
    }
}