package com.example.course_platform.dto;

import jakarta.validation.constraints.NotBlank;


public record LoginRequest(

        @NotBlank(
                message = "E-posta adresi boş bırakılamaz."
        )
        String email,

        @NotBlank(
                message = "Şifre boş bırakılamaz."
        )
        String password

) {
}