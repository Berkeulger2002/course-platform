package com.example.course_platform.controller;

import com.example.course_platform.dto.CertificateResponse;
import com.example.course_platform.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CertificateResponse>> getCertificates(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                certificateService.getCertificatesForStudent(studentId)
        );
    }
}