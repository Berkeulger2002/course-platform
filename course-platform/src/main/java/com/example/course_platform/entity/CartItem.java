package com.example.course_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_items_cart_course",
                        columnNames = {
                                "cart_id",
                                "course_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CartItem extends BaseEntity {


    // =========================================================
    // HANGİ SEPETE AİT?
    // =========================================================

    @ManyToOne
    @JoinColumn(
            name = "cart_id",
            nullable = false
    )
    @JsonIgnore
    private Cart cart;


    // =========================================================
    // HANGİ KURSU TEMSİL EDİYOR?
    // =========================================================

    @ManyToOne
    @JoinColumn(
            name = "course_id",
            nullable = false
    )
    private Course course;


    // =========================================================
    // SEPETE EKLENDİĞİ ANDAKİ FİYAT
    // =========================================================

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal priceAtAddition;
}