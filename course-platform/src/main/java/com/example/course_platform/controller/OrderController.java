package com.example.course_platform.controller;

import com.example.course_platform.dto.OrderResponse;
import com.example.course_platform.entity.Order;
import com.example.course_platform.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    // =========================================================
    // SİPARİŞİ TAMAMLA
    // =========================================================

    @PostMapping("/student/{studentId}/place")
    public ResponseEntity<OrderResponse> placeOrder(
            @PathVariable Long studentId) {


        Order order =
                orderService.placeOrder(
                        studentId
                );


        return ResponseEntity.ok(
                toResponse(
                        order
                )
        );
    }


    // =========================================================
    // SİPARİŞ KODUNA GÖRE GETİR
    // =========================================================

    @GetMapping("/code/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderForCode(
            @PathVariable String orderCode) {


        Order order =
                orderService.getOrderForCode(
                        orderCode
                );


        return ResponseEntity.ok(
                toResponse(
                        order
                )
        );
    }


    // =========================================================
    // ÖĞRENCİNİN TÜM SİPARİŞLERİ
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<OrderResponse>>
    getAllOrdersForCustomer(
            @PathVariable Long studentId) {


        List<OrderResponse> response =
                orderService
                        .getAllOrdersForCustomer(
                                studentId
                        )
                        .stream()
                        .map(
                                this::toResponse
                        )
                        .toList();


        return ResponseEntity.ok(
                response
        );
    }


    // =========================================================
    // ORDER ENTITY -> ORDER RESPONSE DTO
    //
    // Entity'yi doğrudan frontend'e vermiyoruz.
    //
    // Şimdilik:
    //
    // - id
    // - orderCode
    // - totalPrice
    //
    // dönüyor.
    //
    // OrderItem DB testi tamamlandıktan sonra
    // sipariş detaylarını DTO'ya ayrıca ekleyeceğiz.
    // =========================================================

    private OrderResponse toResponse(
            Order order
    ) {


        return new OrderResponse(

                order.getId(),

                order.getOrderCode(),

                order.getTotalPrice()
        );
    }
}