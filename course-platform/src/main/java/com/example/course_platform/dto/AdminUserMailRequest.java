package com.example.course_platform.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record AdminUserMailRequest(

        @NotBlank(
                message = "Mail konusu zorunludur."
        )
        @Size(
                max = 150,
                message = "Mail konusu en fazla 150 karakter olabilir."
        )
        String subject,


        @NotBlank(
                message = "Mail mesajı zorunludur."
        )
        @Size(
                max = 5000,
                message = "Mail mesajı en fazla 5000 karakter olabilir."
        )
        String message

) {
}