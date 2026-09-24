package com.example.course_platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order extends BaseEntity {


    // =========================================================
    // SİPARİŞ KODU
    // =========================================================

    @Column(
            nullable = false,
            unique = true
    )
    private String orderCode;


    // =========================================================
    // SİPARİŞİ VEREN ÖĞRENCİ
    // =========================================================

    @ManyToOne
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private Student student;


    // =========================================================
    // SİPARİŞİN TOPLAM TUTARI
    // =========================================================

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalPrice =
            BigDecimal.ZERO;


    // =========================================================
    // SİPARİŞİN İÇİNDEKİ KURSLAR
    //
    // CartItem geçicidir.
    // OrderItem satın alma geçmişidir.
    // =========================================================

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL
    )
    private List<OrderItem> items =
            new ArrayList<>();
}