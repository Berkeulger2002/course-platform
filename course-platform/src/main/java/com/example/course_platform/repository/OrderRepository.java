package com.example.course_platform.repository;


import com.example.course_platform.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


import java.util.List;


@Repository
public interface OrderRepository
        extends JpaRepository<Order, Long> {


    // =========================================================
    // ÖĞRENCİNİN SİPARİŞLERİ
    // =========================================================

    List<Order> findAllByStudentId(
            Long studentId
    );


    // =========================================================
    // SİPARİŞ KODUNA GÖRE BUL
    // =========================================================

    Order findByOrderCode(
            String orderCode
    );


    // =========================================================
    // ADMIN - TÜM SİPARİŞLER
    //
    // En yeni sipariş önce gelir.
    // =========================================================

    List<Order> findAllByOrderByCreatedAtDesc();
}