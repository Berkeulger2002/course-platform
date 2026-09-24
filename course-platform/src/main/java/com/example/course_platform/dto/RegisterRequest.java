package com.example.course_platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterRequest(

        @NotBlank(
                message = "Ad soyad boş bırakılamaz."
        )
        String name,


        @NotBlank(
                message = "E-posta adresi boş bırakılamaz."
        )
        @Email(
                message = "Geçerli bir e-posta adresi giriniz."
        )
        String email,


        @NotBlank(
                message = "Şifre boş bırakılamaz."
        )
        @Size(
                min = 5,
                max = 72,
                message = "Şifre en az 5 karakter olmalıdır."
        )
        String password

) {
}