package com.example.course_platform.service;

import com.example.course_platform.dto.TeacherProfileResponse;
import com.example.course_platform.dto.TeacherProfileUpdateRequest;

import com.example.course_platform.entity.Teacher;
import com.example.course_platform.entity.User;

import com.example.course_platform.repository.TeacherRepository;
import com.example.course_platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    private final UserRepository userRepository;

    private final CurrentUserService currentUserService;


    // =========================================================
    // TÜM ÖĞRETMENLER
    //
    // SecurityConfig tarafından dış erişime kapalı tutuluyor.
    // =========================================================

    public List<Teacher> getTeachers() {

        return teacherRepository
                .findAll();
    }


    // =========================================================
    // ÖĞRETMEN OLUŞTUR
    //
    // SecurityConfig tarafından dış erişime kapalı tutuluyor.
    // =========================================================

    public Teacher createTeacher(
            Teacher teacher) {

        return teacherRepository
                .save(
                        teacher
                );
    }


    // =========================================================
    // ESKİ GENEL UPDATE
    //
    // Mevcut sistemi bozmamak için korunuyor.
    // SecurityConfig tarafından dış erişime kapalı.
    // =========================================================

    @Transactional
    public Teacher updateTeacher(
            Long id,
            Teacher updatedTeacher) {

        Teacher existingTeacher =
                teacherRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğretmen bulunamadı."
                                )
                        );


        existingTeacher.setName(
                updatedTeacher.getName()
        );


        existingTeacher.setEmail(
                updatedTeacher.getEmail()
        );


        return teacherRepository.save(
                existingTeacher
        );
    }


    // =========================================================
    // ÖĞRETMEN SİL
    //
    // SecurityConfig tarafından dış erişime kapalı tutuluyor.
    // =========================================================

    public void deleteTeacher(
            Long id) {

        teacherRepository.deleteById(
                id
        );
    }


    // =========================================================
    // PROFİL GETİR
    // =========================================================

    @Transactional(readOnly = true)
    public TeacherProfileResponse getTeacherProfile(
            Long teacherId) {


        // =====================================================
        // IDOR / OWNERSHIP
        //
        // JWT'deki kullanıcı TEACHER olmalı ve
        // URL'deki teacherId kendi ID'si olmalı.
        // =====================================================

        currentUserService.requireTeacher(
                teacherId
        );


        Teacher teacher =
                teacherRepository
                        .findById(
                                teacherId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğretmen bulunamadı."
                                )
                        );


        return toProfileResponse(
                teacher
        );
    }


    // =========================================================
    // PROFİL GÜNCELLE
    // =========================================================

    @Transactional
    public TeacherProfileResponse updateTeacherProfile(
            Long teacherId,
            TeacherProfileUpdateRequest request) {


        // =====================================================
        // IDOR / OWNERSHIP
        // =====================================================

        currentUserService.requireTeacher(
                teacherId
        );


        Teacher teacher =
                teacherRepository
                        .findById(
                                teacherId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğretmen bulunamadı."
                                )
                        );


        // =====================================================
        // AD SOYAD
        // =====================================================

        String name =
                request.name() == null
                        ?
                        ""
                        :
                        request.name()
                                .trim();


        if (
                name.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Ad soyad alanı boş bırakılamaz."
            );
        }


        if (
                name.length() > 100
        ) {

            throw new IllegalArgumentException(
                    "Ad soyad en fazla 100 karakter olabilir."
            );
        }


        // =====================================================
        // EMAIL
        // =====================================================

        String email =
                request.email() == null
                        ?
                        ""
                        :
                        request.email()
                                .trim()
                                .toLowerCase();


        if (
                email.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "E-posta alanı boş bırakılamaz."
            );
        }


        // =====================================================
        // GMAIL-ONLY KURALI
        //
        // Registration sırasında uygulanan kural
        // profil güncellemesi üzerinden bypass edilemez.
        // =====================================================

        if (
                !email.matches(
                        "^[A-Za-z0-9._%+-]+@gmail\\.com$"
                )
        ) {

            throw new IllegalArgumentException(
                    "Yalnızca gmail.com uzantılı e-posta adresleri kullanılabilir."
            );
        }


        // =====================================================
        // TÜM KULLANICILAR ARASINDA EMAIL ÇAKIŞMASI
        //
        // Yalnız TeacherRepository kontrolü yetmez.
        // Öğrenci hesabıyla da çakışmamalı.
        // =====================================================

        Optional<User> existingUser =
                userRepository
                        .findByEmail(
                                email
                        );


        if (
                existingUser.isPresent()
                        &&
                        !existingUser
                                .get()
                                .getId()
                                .equals(
                                        teacherId
                                )
        ) {

            throw new IllegalStateException(
                    "Bu e-posta adresi başka bir hesap tarafından kullanılıyor."
            );
        }


        // =====================================================
        // GÜNCELLE
        // =====================================================

        teacher.setName(
                name
        );


        teacher.setEmail(
                email
        );


        Teacher savedTeacher =
                teacherRepository.save(
                        teacher
                );


        return toProfileResponse(
                savedTeacher
        );
    }


    // =========================================================
    // ENTITY -> PROFİL DTO
    // =========================================================

    private TeacherProfileResponse toProfileResponse(
            Teacher teacher) {

        return new TeacherProfileResponse(

                teacher.getId(),

                teacher.getName(),

                teacher.getEmail(),

                teacher.getRole() == null
                        ?
                        null
                        :
                        teacher.getRole()
                                .name()
        );
    }
}