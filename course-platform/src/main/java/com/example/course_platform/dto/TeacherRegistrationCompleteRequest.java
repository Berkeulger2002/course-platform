package com.example.course_platform.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record TeacherRegistrationCompleteRequest(


        // =========================================================
        // EMAIL
        // =========================================================

        @NotBlank(
                message = "E-posta adresi boş bırakılamaz."
        )
        @Email(
                message = "Geçerli bir e-posta adresi giriniz."
        )
        String email,


        // =========================================================
        // VERIFICATION CODE
        // =========================================================

        @NotBlank(
                message = "Doğrulama kodu boş bırakılamaz."
        )
        @Pattern(
                regexp = "^\\d{6}$",
                message = "Doğrulama kodu 6 haneli olmalıdır."
        )
        String verificationCode,


        // =========================================================
        // PASSWORD
        // =========================================================

        @NotBlank(
                message = "Şifre boş bırakılamaz."
        )
        @Size(
                min = 5,
                max = 72,
                message = "Şifre en az 5 karakter olmalıdır."
        )
        String password,


        // =========================================================
        // PASSWORD CONFIRMATION
        // =========================================================

        @NotBlank(
                message = "Şifre tekrarı boş bırakılamaz."
        )
        @Size(
                min = 5,
                max = 72,
                message = "Şifre tekrarı en az 5 karakter olmalıdır."
        )
        String confirmPassword

) {
}