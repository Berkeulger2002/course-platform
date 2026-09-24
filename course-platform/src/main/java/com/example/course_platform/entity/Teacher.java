package com.example.course_platform.entity;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore; // 1. BİRİNCİ EKLEME: Gerekli paketi içeri aldık

@Entity
@Table(name = "teachers")
public class Teacher extends User {

    // Öğretmenin Verdiği Kurslar (Bire Çok İlişki)
    @JsonIgnore // 2. İKİNCİ EKLEME: Sonsuz döngüyü (Infinite Recursion) kestiğimiz kalkan!
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<Course> courses;

    // --- GETTER VE SETTER METOTLARI ---

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }
}