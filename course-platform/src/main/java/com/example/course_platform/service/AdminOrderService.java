package com.example.course_platform.service;


import com.example.course_platform.dto.AdminOrderItemResponse;
import com.example.course_platform.dto.AdminOrderResponse;
import com.example.course_platform.dto.AdminOrderSummaryResponse;

import com.example.course_platform.entity.Order;
import com.example.course_platform.entity.OrderItem;
import com.example.course_platform.entity.Student;

import com.example.course_platform.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminOrderService {


    private final OrderRepository
            orderRepository;


    // =========================================================
    // TÜM SİPARİŞLER
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public List<AdminOrderResponse>
    getOrders() {


        return orderRepository

                .findAllByOrderByCreatedAtDesc()

                .stream()

                .map(
                        this::toResponse
                )

                .toList();
    }


    // =========================================================
    // ORDER SUMMARY
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public AdminOrderSummaryResponse
    getSummary() {


        List<Order> orders =

                orderRepository
                        .findAllByOrderByCreatedAtDesc();


        // =====================================================
        // TOTAL ORDER
        // =====================================================

        long totalOrders =
                orders.size();


        // =====================================================
        // TOTAL REVENUE
        // =====================================================

        BigDecimal totalRevenue =

                orders
                        .stream()

                        .map(
                                order ->

                                        order.getTotalPrice()
                                                != null
                                                ?
                                                order.getTotalPrice()
                                                :
                                                BigDecimal.ZERO
                        )

                        .reduce(

                                BigDecimal.ZERO,

                                BigDecimal::add
                        );


        // =====================================================
        // TOTAL SOLD ITEMS
        //
        // Bir OrderItem = bir kurs satışı.
        // =====================================================

        long totalItemsSold =

                orders
                        .stream()

                        .mapToLong(
                                order ->

                                        order.getItems()
                                                != null
                                                ?
                                                order.getItems()
                                                        .size()
                                                :
                                                0
                        )

                        .sum();


        // =====================================================
        // AVERAGE ORDER VALUE
        // =====================================================

        BigDecimal averageOrderValue;


        if (
                totalOrders == 0
        ) {


            averageOrderValue =
                    BigDecimal.ZERO;


        } else {


            averageOrderValue =

                    totalRevenue.divide(

                            BigDecimal.valueOf(
                                    totalOrders
                            ),

                            2,

                            RoundingMode.HALF_UP
                    );
        }


        return new AdminOrderSummaryResponse(

                totalOrders,

                totalRevenue,

                totalItemsSold,

                averageOrderValue
        );
    }


    // =========================================================
    // ORDER RESPONSE
    // =========================================================

    private AdminOrderResponse
    toResponse(

            Order order
    ) {


        Student student =
                order.getStudent();


        Long studentId =
                null;


        String studentName =
                "Bilinmiyor";


        String studentEmail =
                "";


        if (
                student != null
        ) {


            studentId =
                    student.getId();


            studentName =
                    student.getName();


            studentEmail =
                    student.getEmail();
        }


        // =====================================================
        // ITEMS
        // =====================================================

        List<AdminOrderItemResponse> items =

                order.getItems()
                        == null
                        ?
                        List.of()
                        :
                        order.getItems()

                                .stream()

                                .map(
                                        this::toItemResponse
                                )

                                .toList();


        return new AdminOrderResponse(

                order.getId(),

                order.getOrderCode(),

                studentId,

                studentName,

                studentEmail,

                order.getTotalPrice(),

                items.size(),

                order.getCreatedAt(),

                items
        );
    }


    // =========================================================
    // ORDER ITEM RESPONSE
    // =========================================================

    private AdminOrderItemResponse
    toItemResponse(

            OrderItem item
    ) {


        return new AdminOrderItemResponse(

                item.getId(),

                item.getCourseId(),

                item.getCourseName(),

                item.getTeacherId(),

                item.getTeacherName(),

                item.getPriceAtPurchase()
        );
    }
}