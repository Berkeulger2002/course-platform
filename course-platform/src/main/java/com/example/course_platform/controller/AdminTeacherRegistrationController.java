package com.example.course_platform.controller;


import com.example.course_platform.dto.TeacherRegistrationRequestResponse;

import com.example.course_platform.service.TeacherRegistrationResendService;
import com.example.course_platform.service.TeacherRegistrationService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(
        "/api/admin/teacher-requests"
)
@RequiredArgsConstructor
public class AdminTeacherRegistrationController {


    // =========================================================
    // SERVICES
    // =========================================================

    private final TeacherRegistrationService
            teacherRegistrationService;


    private final TeacherRegistrationResendService
            teacherRegistrationResendService;


    // =========================================================
    // GET ALL
    //
    // GET /api/admin/teacher-requests
    // =========================================================

    @GetMapping
    public ResponseEntity<
            List<TeacherRegistrationRequestResponse>
            >
    getAllRequests() {


        return ResponseEntity.ok(

                teacherRegistrationService
                        .getAllRequests()

        );
    }


    // =========================================================
    // GET PENDING
    //
    // GET /api/admin/teacher-requests/pending
    // =========================================================

    @GetMapping(
            "/pending"
    )
    public ResponseEntity<
            List<TeacherRegistrationRequestResponse>
            >
    getPendingRequests() {


        return ResponseEntity.ok(

                teacherRegistrationService
                        .getPendingRequests()

        );
    }


    // =========================================================
    // APPROVE + SEND CODE
    //
    // POST /api/admin/teacher-requests/{id}/approve
    // =========================================================

    @PostMapping(
            "/{requestId}/approve"
    )
    public ResponseEntity<
            TeacherRegistrationRequestResponse
            >
    approveRequest(

            @PathVariable
            Long requestId
    ) {


        return ResponseEntity.ok(

                teacherRegistrationService
                        .approveAndSendCode(
                                requestId
                        )

        );
    }


    // =========================================================
    // RESEND CODE
    //
    // POST /api/admin/teacher-requests/{id}/resend-code
    //
    // Yalnızca CODE_SENT durumundaki başvurularda çalışır.
    // =========================================================

    @PostMapping(
            "/{requestId}/resend-code"
    )
    public ResponseEntity<
            TeacherRegistrationRequestResponse
            >
    resendVerificationCode(

            @PathVariable
            Long requestId
    ) {


        return ResponseEntity.ok(

                teacherRegistrationResendService
                        .resendVerificationCode(
                                requestId
                        )

        );
    }


    // =========================================================
    // REJECT
    //
    // POST /api/admin/teacher-requests/{id}/reject
    // =========================================================

    @PostMapping(
            "/{requestId}/reject"
    )
    public ResponseEntity<
            TeacherRegistrationRequestResponse
            >
    rejectRequest(

            @PathVariable
            Long requestId
    ) {


        return ResponseEntity.ok(

                teacherRegistrationService
                        .rejectRequest(
                                requestId
                        )

        );
    }
}