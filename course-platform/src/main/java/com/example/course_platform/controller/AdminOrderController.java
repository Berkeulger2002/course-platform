package com.example.course_platform.controller;


import com.example.course_platform.dto.AdminOrderResponse;
import com.example.course_platform.dto.AdminOrderSummaryResponse;

import com.example.course_platform.service.AdminOrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(
        "/api/admin/orders"
)
@RequiredArgsConstructor
public class AdminOrderController {


    private final AdminOrderService
            adminOrderService;


    // =========================================================
    // TÜM SİPARİŞLER
    //
    // GET /api/admin/orders
    // =========================================================

    @GetMapping
    public ResponseEntity<
            List<AdminOrderResponse>
            >
    getOrders() {


        return ResponseEntity.ok(

                adminOrderService
                        .getOrders()
        );
    }


    // =========================================================
    // ORDER SUMMARY
    //
    // GET /api/admin/orders/summary
    // =========================================================

    @GetMapping(
            "/summary"
    )
    public ResponseEntity<
            AdminOrderSummaryResponse
            >
    getSummary() {


        return ResponseEntity.ok(

                adminOrderService
                        .getSummary()
        );
    }
}