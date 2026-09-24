package com.example.course_platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_progress",
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
public class CourseProgress extends BaseEntity {

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


    // Öğrencinin videoda en son kaldığı saniye
    @Column(name = "last_position_seconds")
    private double lastPositionSeconds = 0;


    // Öğrencinin ulaştığı en ileri nokta
    @Column(name = "max_position_seconds")
    private double maxPositionSeconds = 0;


    // Videonun toplam süresi
    @Column(name = "duration_seconds")
    private double durationSeconds = 0;


    // 0 - 100 arasında ilerleme
    @Column(name = "progress_percentage")
    private double progressPercentage = 0;


    // Kurs tamamlandı mı?
    private boolean completed = false;


    // Son izleme zamanı
    @Column(name = "last_watched_at")
    private LocalDateTime lastWatchedAt;
}