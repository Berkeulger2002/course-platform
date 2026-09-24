package com.example.course_platform.service;

import com.example.course_platform.entity.Role;
import com.example.course_platform.entity.User;
import com.example.course_platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class CurrentUserService {


    // =========================================================
    // REPOSITORY
    // =========================================================

    private final UserRepository userRepository;


    // =========================================================
    // OTURUMDAKİ KULLANICIYI GETİR
    // =========================================================
    //
    // JwtAuthenticationFilter authentication.getName()
    // alanına kullanıcının e-posta adresini koyuyor.
    //
    // Burada o e-posta ile kullanıcıyı tekrar DB'den
    // getiriyoruz.
    //
    // Böylece:
    //
    // localStorage
    // query param
    // path variable
    //
    // gibi client tarafından değiştirilebilen bilgilere
    // güvenmiyoruz.
    //
    // =========================================================

    public User getCurrentUser() {


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (
                authentication == null
                        ||
                        !authentication.isAuthenticated()
                        ||
                        "anonymousUser".equals(
                                authentication.getPrincipal()
                        )
        ) {

            throw new AuthenticationCredentialsNotFoundException(
                    "Giriş yapmanız gerekiyor."
            );
        }


        String email =
                authentication.getName();


        return userRepository
                .findByEmailIgnoreCase(
                        email
                )
                .orElseThrow(() ->
                        new AuthenticationCredentialsNotFoundException(
                                "Oturum kullanıcısı bulunamadı."
                        )
                );
    }


    // =========================================================
    // CURRENT USER ID
    // =========================================================

    public Long getCurrentUserId() {

        return getCurrentUser()
                .getId();
    }


    // =========================================================
    // CURRENT USER ROLE
    // =========================================================

    public Role getCurrentUserRole() {

        return getCurrentUser()
                .getRole();
    }


    // =========================================================
    // STUDENT KONTROLÜ
    // =========================================================
    //
    // Örnek:
    //
    // Giriş yapan:
    // id = 8
    // role = STUDENT
    //
    // URL:
    // /student/8  -> OK
    // /student/9  -> 403
    //
    // =========================================================

    public User requireStudent(
            Long requestedStudentId) {


        User currentUser =
                getCurrentUser();


        if (
                currentUser.getRole()
                        != Role.STUDENT
        ) {

            throw new AccessDeniedException(
                    "Bu işlem yalnızca öğrenciler tarafından yapılabilir."
            );
        }


        if (
                requestedStudentId == null
                        ||
                        !Objects.equals(
                                currentUser.getId(),
                                requestedStudentId
                        )
        ) {

            throw new AccessDeniedException(
                    "Başka bir öğrencinin verilerine erişemezsiniz."
            );
        }


        return currentUser;
    }


    // =========================================================
    // TEACHER KONTROLÜ
    // =========================================================
    //
    // Örnek:
    //
    // Giriş yapan:
    // id = 1
    // role = TEACHER
    //
    // URL:
    // /teacher/1 -> OK
    // /teacher/2 -> 403
    //
    // =========================================================

    public User requireTeacher(
            Long requestedTeacherId) {


        User currentUser =
                getCurrentUser();


        if (
                currentUser.getRole()
                        != Role.TEACHER
        ) {

            throw new AccessDeniedException(
                    "Bu işlem yalnızca öğretmenler tarafından yapılabilir."
            );
        }


        if (
                requestedTeacherId == null
                        ||
                        !Objects.equals(
                                currentUser.getId(),
                                requestedTeacherId
                        )
        ) {

            throw new AccessDeniedException(
                    "Başka bir öğretmenin verilerine erişemezsiniz."
            );
        }


        return currentUser;
    }


    // =========================================================
    // SADECE STUDENT ROLÜ
    // =========================================================

    public User requireStudentRole() {


        User currentUser =
                getCurrentUser();


        if (
                currentUser.getRole()
                        != Role.STUDENT
        ) {

            throw new AccessDeniedException(
                    "Bu işlem yalnızca öğrenciler tarafından yapılabilir."
            );
        }


        return currentUser;
    }


    // =========================================================
    // SADECE TEACHER ROLÜ
    // =========================================================

    public User requireTeacherRole() {


        User currentUser =
                getCurrentUser();


        if (
                currentUser.getRole()
                        != Role.TEACHER
        ) {

            throw new AccessDeniedException(
                    "Bu işlem yalnızca öğretmenler tarafından yapılabilir."
            );
        }


        return currentUser;
    }
}