package com.example.course_platform.controller;

import com.example.course_platform.dto.CartResponse;
import com.example.course_platform.service.CartService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    // =========================================================
    // SEPETİ GETİR
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                cartService.getCart(
                        studentId
                )
        );
    }


    // =========================================================
    // KURSU SEPETE EKLE
    // =========================================================

    @PostMapping("/student/{studentId}/add-course/{courseId}")
    public ResponseEntity<CartResponse> addCourseToCart(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                cartService.addCourseToCart(
                        studentId,
                        courseId
                )
        );
    }


    // =========================================================
    // SEPETTEN CART ITEM ÇIKAR
    // =========================================================

    @DeleteMapping("/student/{studentId}/remove-item/{cartItemId}")
    public ResponseEntity<CartResponse> removeCourseFromCart(
            @PathVariable Long studentId,
            @PathVariable Long cartItemId) {

        return ResponseEntity.ok(
                cartService.removeCourseFromCart(
                        studentId,
                        cartItemId
                )
        );
    }


    // =========================================================
    // SEPETİ TAMAMEN BOŞALT
    // =========================================================

    @DeleteMapping("/student/{studentId}/empty")
    public ResponseEntity<CartResponse> emptyCart(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                cartService.emptyCart(
                        studentId
                )
        );
    }
}