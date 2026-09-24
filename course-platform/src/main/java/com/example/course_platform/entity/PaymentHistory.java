package com.example.course_platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_history")
@Getter
@Setter
@NoArgsConstructor
public class PaymentHistory extends BaseEntity {

    @ManyToOne
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private Student student;


    @ManyToOne
    @JoinColumn(
            name = "course_id",
            nullable = false
    )
    private Course course;


    /*
     * Kurs adı daha sonra değiştirilse bile
     * satın alma anındaki isim geçmişte kalsın.
     */
    @Column(
            name = "course_name",
            nullable = false
    )
    private String courseName;


    /*
     * Satın alma anındaki fiyat.
     */
    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amount;


    @Column(
            nullable = false,
            length = 50
    )
    private String status;


    @Column(
            name = "payment_method",
            nullable = false,
            length = 50
    )
    private String paymentMethod;


    @Column(
            name = "transaction_code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String transactionCode;


    @Column(
            name = "purchased_at",
            nullable = false
    )
    private LocalDateTime purchasedAt;
}