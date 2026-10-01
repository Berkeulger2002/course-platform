package com.example.course_platform.service;


import com.example.course_platform.dto.AdminCourseResponse;
import com.example.course_platform.dto.AdminCourseSummaryResponse;

import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.Teacher;

import com.example.course_platform.repository.CourseRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminCourseService {


    private final CourseRepository
            courseRepository;


    // =========================================================
    // TÜM KURSLAR
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public List<AdminCourseResponse>
    getCourses() {


        return courseRepository

                .findAllByOrderByIdDesc()

                .stream()

                .map(
                        this::toResponse
                )

                .toList();
    }


    // =========================================================
    // COURSE SUMMARY
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public AdminCourseSummaryResponse
    getSummary() {


        List<Course> courses =

                courseRepository
                        .findAllByOrderByIdDesc();


        // =====================================================
        // TOTAL
        // =====================================================

        long totalCourses =
                courses.size();


        // =====================================================
        // PURCHASABLE
        // =====================================================

        long purchasableCourses =

                courses
                        .stream()

                        .filter(
                                Course::isPurchasable
                        )

                        .count();


        // =====================================================
        // CLOSED
        // =====================================================

        long closedCourses =

                totalCourses
                        -
                        purchasableCourses;


        // =====================================================
        // TOTAL ENROLLMENTS
        //
        // Her kursun currentEnrolled değerlerinin toplamı.
        // Bu unique öğrenci sayısı değildir.
        //
        // Aynı öğrenci 3 farklı kursa kayıtlıysa:
        // toplam kayıt = 3
        // =====================================================

        long totalEnrollments =

                courses
                        .stream()

                        .mapToLong(
                                Course::getCurrentEnrolled
                        )

                        .sum();


        return new AdminCourseSummaryResponse(

                totalCourses,

                purchasableCourses,

                closedCourses,

                totalEnrollments
        );
    }


    // =========================================================
    // MAPPER
    // =========================================================

    private AdminCourseResponse
    toResponse(

            Course course
    ) {


        Teacher teacher =
                course.getTeacher();


        Long teacherId =
                null;


        String teacherName =
                "Atanmamış";


        if (
                teacher != null
        ) {


            teacherId =
                    teacher.getId();


            teacherName =
                    teacher.getName();
        }


        return new AdminCourseResponse(

                course.getId(),

                course.getName(),

                course.getPrice(),

                course.getMaxCapacity(),

                course.getCurrentEnrolled(),

                course.isPurchasable(),

                teacherId,

                teacherName
        );
    }
}