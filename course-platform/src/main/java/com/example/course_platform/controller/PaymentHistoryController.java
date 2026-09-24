package com.example.course_platform.controller;

import com.example.course_platform.dto.PaymentHistoryResponse;
import com.example.course_platform.service.PaymentHistoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/payment-history")
@RequiredArgsConstructor
public class PaymentHistoryController {


    private final PaymentHistoryService paymentHistoryService;


    // =========================================================
    // ÖĞRENCİNİN ÖDEME GEÇMİŞİ
    //
    // Sadece görüntüleme endpointidir.
    //
    // PaymentHistory kaydı HTTP üzerinden doğrudan
    // oluşturulamaz.
    //
    // Yeni ödeme kaydı yalnızca başarılı checkout sırasında
    // OrderService -> PaymentHistoryService üzerinden oluşur.
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PaymentHistoryResponse>>
    getPaymentHistory(
            @PathVariable Long studentId
    ) {


        return ResponseEntity.ok(

                paymentHistoryService
                        .getStudentPaymentHistory(
                                studentId
                        )
        );
    }
}