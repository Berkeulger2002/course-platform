package com.example.course_platform.document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "course_reviews")
@Getter
@Setter
@NoArgsConstructor
public class CourseReview {

    @Id
    private String id;


    private Long courseId;


    /*
     * MongoDB içerisinde tutulmaya devam eder.
     *
     * Fakat API JSON response'unda dışarı çıkmaz.
     */
    @JsonIgnore
    private Long studentId;


    /*
     * Arayüzde gösterilecek isim.
     *
     * Bu değer artık frontend'den güvenilir kabul edilmiyor.
     * Service tarafından gerçek öğrenci kaydından atanıyor.
     */
    private String studentName;


    private String comment;


    private int rating;


    private LocalDateTime createdAt =
            LocalDateTime.now();
}