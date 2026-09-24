package com.example.course_platform.service;

import com.example.course_platform.dto.StudentProfileResponse;
import com.example.course_platform.dto.StudentProfileUpdateRequest;

import com.example.course_platform.entity.Cart;
import com.example.course_platform.entity.CourseProgress;
import com.example.course_platform.entity.Student;
import com.example.course_platform.entity.User;

import com.example.course_platform.repository.CertificateRepository;
import com.example.course_platform.repository.CourseProgressRepository;
import com.example.course_platform.repository.StudentRepository;
import com.example.course_platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    private final UserRepository userRepository;

    private final CourseProgressRepository courseProgressRepository;

    private final CertificateRepository certificateRepository;

    private final CurrentUserService currentUserService;


    // =========================================================
    // ÖĞRENCİ EKLE
    //
    // Bu metot mevcut sistem içi kullanım için korunuyor.
    //
    // /api/students/add endpointi SecurityConfig içerisinde
    // zaten dışarıdan erişime kapalı.
    // =========================================================

    public Student addCustomer(
            Student student) {

        Cart emptyCart =
                new Cart();


        emptyCart.setStudent(
                student
        );


        student.setCart(
                emptyCart
        );


        return studentRepository.save(
                student
        );
    }


    // =========================================================
    // PROFİL BİLGİLERİNİ GETİR
    // =========================================================

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(
            Long studentId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        //
        // JWT'deki kullanıcı STUDENT olmalı ve
        // URL'deki studentId kendi ID'si olmalı.
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        Student student =
                studentRepository
                        .findById(
                                studentId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğrenci bulunamadı."
                                )
                        );


        return createProfileResponse(
                student
        );
    }


    // =========================================================
    // PROFİL GÜNCELLE
    // =========================================================

    @Transactional
    public StudentProfileResponse updateProfile(
            Long studentId,
            StudentProfileUpdateRequest request) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        //
        // Student 8 yalnızca Student 8 profilini
        // güncelleyebilir.
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        Student student =
                studentRepository
                        .findById(
                                studentId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğrenci bulunamadı."
                                )
                        );


        // =====================================================
        // AD KONTROLÜ
        // =====================================================

        if (
                request.name() == null
                        ||
                        request.name()
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Ad boş bırakılamaz."
            );
        }


        // =====================================================
        // E-POSTA KONTROLÜ
        // =====================================================

        if (
                request.email() == null
                        ||
                        request.email()
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "E-posta boş bırakılamaz."
            );
        }


        String normalizedEmail =
                request
                        .email()
                        .trim()
                        .toLowerCase();


        // =====================================================
        // GMAIL KONTROLÜ
        //
        // Kayıt olurken uyguladığımız Gmail-only kuralının
        // profil güncellemesi üzerinden bypass edilmesini
        // engelliyoruz.
        // =====================================================

        if (
                !normalizedEmail.matches(
                        "^[A-Za-z0-9._%+-]+@gmail\\.com$"
                )
        ) {

            throw new IllegalArgumentException(
                    "Yalnızca gmail.com uzantılı e-posta adresleri kullanılabilir."
            );
        }


        // =====================================================
        // E-POSTA BAŞKA KULLANICIYA AİT Mİ?
        // =====================================================

        Optional<User> existingUser =
                userRepository
                        .findByEmail(
                                normalizedEmail
                        );


        if (
                existingUser.isPresent()
                        &&
                        !existingUser
                                .get()
                                .getId()
                                .equals(
                                        studentId
                                )
        ) {

            throw new IllegalArgumentException(
                    "Bu e-posta adresi başka bir kullanıcı tarafından kullanılıyor."
            );
        }


        // =====================================================
        // PROFİLİ GÜNCELLE
        // =====================================================

        student.setName(
                request
                        .name()
                        .trim()
        );


        student.setEmail(
                normalizedEmail
        );


        Student savedStudent =
                studentRepository.save(
                        student
                );


        return createProfileResponse(
                savedStudent
        );
    }


    // =========================================================
    // PROFILE RESPONSE
    // =========================================================

    private StudentProfileResponse createProfileResponse(
            Student student) {


        int enrolledCourseCount =
                0;


        if (
                student.getEnrolledCourses()
                        !=
                        null
        ) {

            enrolledCourseCount =
                    student
                            .getEnrolledCourses()
                            .size();
        }


        long completedCourseCount =
                courseProgressRepository
                        .findAllByStudentIdOrderByLastWatchedAtDesc(
                                student.getId()
                        )
                        .stream()
                        .filter(
                                CourseProgress::isCompleted
                        )
                        .count();


        int certificateCount =
                certificateRepository
                        .findAllByStudentIdOrderByIssuedAtDesc(
                                student.getId()
                        )
                        .size();


        return new StudentProfileResponse(

                student.getId(),

                student.getName(),

                student.getEmail(),

                student
                        .getRole()
                        .name(),

                enrolledCourseCount,

                completedCourseCount,

                certificateCount
        );
    }
}