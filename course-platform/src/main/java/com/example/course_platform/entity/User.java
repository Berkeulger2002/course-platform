package com.example.course_platform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;


@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {


    // =========================================================
    // ID
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // AD SOYAD
    // =========================================================

    @Column(nullable = false)
    private String name;


    // =========================================================
    // EMAIL
    // =========================================================

    @Column(
            nullable = false,
            unique = true
    )
    private String email;


    // =========================================================
    // PASSWORD
    // =========================================================
    //
    // WRITE_ONLY:
    //
    // Request içinde password kabul edilir:
    //
    // {
    //   "email": "...",
    //   "password": "123456"
    // }
    //
    // fakat response JSON içinde password
    // dışarı gönderilmez.
    //
    // Böylece Course -> Teacher gibi
    // nested response'larda şifre görünmez.
    //
    // =========================================================

    @JsonProperty(
            access = JsonProperty.Access.WRITE_ONLY
    )
    @Column(nullable = false)
    private String password;


    // =========================================================
    // ROLE
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    // =========================================================
    // GETTER / SETTER
    // =========================================================

    public Long getId() {

        return id;
    }


    public void setId(
            Long id) {

        this.id = id;
    }


    public String getName() {

        return name;
    }


    public void setName(
            String name) {

        this.name = name;
    }


    public String getEmail() {

        return email;
    }


    public void setEmail(
            String email) {

        this.email = email;
    }


    public String getPassword() {

        return password;
    }


    public void setPassword(
            String password) {

        this.password = password;
    }


    public Role getRole() {

        return role;
    }


    public void setRole(
            Role role) {

        this.role = role;
    }
}