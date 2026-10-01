package com.example.course_platform.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record TeacherRegistrationCreateRequest(

        @NotBlank(
                message = "Ad soyad boş bırakılamaz."
        )
        @Size(
                max = 255,
                message = "Ad soyad en fazla 255 karakter olabilir."
        )
        String name,


        @NotBlank(
                message = "E-posta adresi boş bırakılamaz."
        )
        @Email(
                message = "Geçerli bir e-posta adresi giriniz."
        )
        @Size(
                max = 255,
                message = "E-posta adresi en fazla 255 karakter olabilir."
        )
        String email

) {
}