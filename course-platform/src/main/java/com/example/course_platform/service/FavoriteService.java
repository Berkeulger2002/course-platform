package com.example.course_platform.service;

import com.example.course_platform.dto.PublicCourseResponse;

import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.Favorite;
import com.example.course_platform.entity.Student;

import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.FavoriteRepository;
import com.example.course_platform.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    private final StudentRepository studentRepository;

    private final CourseRepository courseRepository;

    private final CurrentUserService currentUserService;


    // =========================================================
    // ÖĞRENCİNİN FAVORİ KURSLARI
    // =========================================================

    public List<PublicCourseResponse> getFavoriteCourses(
            Long studentId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        //
        // JWT'deki öğrenci ile URL'deki studentId
        // aynı olmak zorunda.
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        /*
         * Öğrenci gerçekten var mı kontrol ediyoruz.
         */
        studentRepository
                .findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Öğrenci bulunamadı."
                        )
                );


        return favoriteRepository
                .findAllByStudentId(
                        studentId
                )
                .stream()
                .map(Favorite::getCourse)
                .map(this::toSafeCourseResponse)
                .toList();
    }


    // =========================================================
    // FAVORİYE EKLE
    // =========================================================

    @Transactional
    public void addFavorite(
            Long studentId,
            Long courseId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Öğrenci bulunamadı."
                                )
                        );


        Course course =
                courseRepository
                        .findById(courseId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Kurs bulunamadı."
                                )
                        );


        /*
         * Zaten favorideyse tekrar ekleme.
         */
        boolean alreadyFavorite =
                favoriteRepository
                        .existsByStudentIdAndCourseId(
                                studentId,
                                courseId
                        );


        if (alreadyFavorite) {

            return;
        }


        Favorite favorite =
                new Favorite();


        favorite.setStudent(
                student
        );


        favorite.setCourse(
                course
        );


        favoriteRepository.save(
                favorite
        );
    }


    // =========================================================
    // FAVORİDEN ÇIKAR
    // =========================================================

    @Transactional
    public void removeFavorite(
            Long studentId,
            Long courseId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        Favorite favorite =
                favoriteRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Bu kurs favorilerde bulunamadı."
                                )
                        );


        favoriteRepository.delete(
                favorite
        );
    }


    // =========================================================
    // FAVORİDE Mİ?
    // =========================================================

    public boolean isFavorite(
            Long studentId,
            Long courseId) {


        // =====================================================
        // IDOR / OWNERSHIP KONTROLÜ
        // =====================================================

        currentUserService.requireStudent(
                studentId
        );


        return favoriteRepository
                .existsByStudentIdAndCourseId(
                        studentId,
                        courseId
                );
    }


    // =========================================================
    // COURSE -> SAFE DTO
    //
    // Course entity doğrudan frontend'e çıkmaz.
    //
    // videoPath
    // teacher.email
    // teacher.id
    // teacher.role
    // createdAt
    // updatedAt
    //
    // response içerisinde bulunmaz.
    // =========================================================

    private PublicCourseResponse toSafeCourseResponse(
            Course course) {

        PublicCourseResponse.TeacherSummary teacherSummary =
                null;


        if (
                course.getTeacher() != null
        ) {

            teacherSummary =
                    new PublicCourseResponse.TeacherSummary(
                            course
                                    .getTeacher()
                                    .getName()
                    );
        }


        return new PublicCourseResponse(

                course.getId(),

                course.getName(),

                course.getDescription(),

                course.getPrice(),

                course.getImageUrl(),

                course.getMaxCapacity(),

                course.getCurrentEnrolled(),

                course.isPurchasable(),

                teacherSummary
        );
    }
}