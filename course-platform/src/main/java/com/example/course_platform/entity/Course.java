package com.example.course_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
public class Course extends BaseEntity {

    // Kursun adı
    private String name;

    // Kursun açıklaması
    @Column(columnDefinition = "TEXT")
    private String description;

    // Kursun fiyatı
    private BigDecimal price;

    // ImageKit üzerinde bulunan kurs kapak görselinin URL'si
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    // ImageKit üzerinde bulunan PRIVATE kurs videosunun yolu
    // Signed URL oluştururken bu yolu kullanacağız.
    @Column(name = "video_path", length = 1000)
    private String videoPath;

    // Kursun kabul edebileceği maksimum öğrenci sayısı
    private int maxCapacity;

    // Şu an kursa kayıtlı olan aktif öğrenci sayısı
    private int currentEnrolled = 0;

    // Kurs sepete eklenebilir durumda mı?
    private boolean isPurchasable = true;

    // Her kurs bir öğretmene aittir
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    // Bu kursa kayıtlı öğrenciler
    @JsonIgnore
    @ManyToMany(mappedBy = "enrolledCourses")
    private List<Student> students;
}