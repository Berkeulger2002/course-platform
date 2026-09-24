package com.example.course_platform.repository;

import com.example.course_platform.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Müşterinin (Öğrencinin) sipariş geçmişini listelemek için özel metodumuz
    List<Order> findAllByStudentId(Long studentId);

    // Sipariş koduna göre arama yapmak için özel metodumuz
    Order findByOrderCode(String orderCode);
}