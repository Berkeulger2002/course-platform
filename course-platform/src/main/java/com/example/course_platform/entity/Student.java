package com.example.course_platform.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "students")
public class Student extends User {

    // Öğrencinin Sepeti (Bire Bir İlişki)
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    // Öğrencinin Kayıtlı Olduğu Kurslar (Çoka Çok İlişki)
    @ManyToMany
    @JoinTable(
            name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> enrolledCourses;

    // Öğrencinin Siparişleri (Bire Çok İlişki)
    @OneToMany(mappedBy = "student")
    private List<Order> orders;

    // --- GETTER VE SETTER METOTLARI ---

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public List<Course> getEnrolledCourses() { return enrolledCourses; }
    public void setEnrolledCourses(List<Course> enrolledCourses) { this.enrolledCourses = enrolledCourses; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}