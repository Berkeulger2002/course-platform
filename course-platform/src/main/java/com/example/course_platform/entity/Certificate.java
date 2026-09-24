package com.example.course_platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "certificates",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "student_id",
                                "course_id"
                        }
                ),
                @UniqueConstraint(
                        columnNames = {
                                "certificate_code"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Certificate extends BaseEntity {

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


    @Column(
            name = "certificate_code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String certificateCode;


    @Column(
            name = "issued_at",
            nullable = false
    )
    private LocalDateTime issuedAt;
}