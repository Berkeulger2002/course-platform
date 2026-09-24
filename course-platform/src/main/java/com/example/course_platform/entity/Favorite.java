package com.example.course_platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "student_id",
                                "course_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Favorite extends BaseEntity {

    // Favoriye ekleyen öğrenci
    @ManyToOne
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private Student student;


    // Favoriye eklenen kurs
    @ManyToOne
    @JoinColumn(
            name = "course_id",
            nullable = false
    )
    private Course course;
}