package com.example.course_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(
        name = "carts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_carts_student",
                        columnNames = {
                                "student_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Cart extends BaseEntity {


    // =========================================================
    // BU SEPET HANGİ ÖĞRENCİYE AİT?
    //
    // Bir öğrencinin yalnızca bir aktif Cart kaydı olabilir.
    // =========================================================

    @OneToOne
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    @JsonIgnore
    private Student student;


    // =========================================================
    // SEPETİN İÇİNDEKİ KURSLAR
    // =========================================================

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items =
            new ArrayList<>();


    // =========================================================
    // AKTİF SEPET TOPLAMI
    // =========================================================

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalPrice =
            BigDecimal.ZERO;
}