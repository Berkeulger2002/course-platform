package com.example.course_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem extends BaseEntity {


    // =========================================================
    // HANGİ SİPARİŞE AİT?
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    @JsonIgnore
    private Order order;


    // =========================================================
    // SATIN ALINAN KURSUN SNAPSHOT BİLGİLERİ
    //
    // Bunları doğrudan Course ilişkisine bırakmıyoruz.
    // Çünkü kursun adı/fiyatı ileride değişse bile
    // eski siparişin geçmişi değişmemeli.
    // =========================================================

    @Column(
            name = "course_id",
            nullable = false
    )
    private Long courseId;


    @Column(
            name = "course_name",
            nullable = false
    )
    private String courseName;


    // =========================================================
    // SATIN ALMA ANINDAKİ ÖĞRETMEN
    // =========================================================

    @Column(name = "teacher_id")
    private Long teacherId;


    @Column(name = "teacher_name")
    private String teacherName;


    // =========================================================
    // SATIN ALMA ANINDAKİ FİYAT
    // =========================================================

    @Column(
            name = "price_at_purchase",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal priceAtPurchase;
}