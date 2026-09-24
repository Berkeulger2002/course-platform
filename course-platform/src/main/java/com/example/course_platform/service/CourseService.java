package com.example.course_platform.service;

import com.example.course_platform.dto.PublicCourseResponse;

import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.Student;
import com.example.course_platform.entity.Teacher;

import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.StudentRepository;
import com.example.course_platform.repository.TeacherRepository;

import io.imagekit.client.ImageKitClient;
import io.imagekit.models.SrcOptions;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    private final TeacherRepository teacherRepository;

    private final StudentRepository studentRepository;

    private final ImageKitClient imageKitClient;

    private final CurrentUserService currentUserService;


    @Value("${imagekit.url-endpoint}")
    private String imageKitUrlEndpoint;


    // =========================================================
    // PUBLIC KURS LİSTESİ
    // =========================================================

    @Transactional(readOnly = true)
    public List<PublicCourseResponse> getAllCourses() {

        return courseRepository
                .findAll()
                .stream()
                .map(
                        this::toSafeCourseResponse
                )
                .toList();
    }


    // =========================================================
    // GÜVENLİ COURSE DTO DÖNÜŞÜMÜ
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


    // =========================================================
    // ÖĞRETMENİN KURSLARI
    // =========================================================

    public List<Course> getAllCoursesForTeacher(
            Long teacherId) {


        currentUserService.requireTeacher(
                teacherId
        );


        return courseRepository
                .findAllByTeacherId(
                        teacherId
                );
    }


    // =========================================================
    // ÖĞRETMENİN TEK KURSUNU GETİR
    // =========================================================

    public Course getCourseForTeacher(
            Long teacherId,
            Long courseId) {


        currentUserService.requireTeacher(
                teacherId
        );


        return getTeacherOwnedCourse(
                teacherId,
                courseId
        );
    }


    // =========================================================
    // KURS OLUŞTUR
    // =========================================================

    public Course createCourseForTeacher(
            Long teacherId,
            Course course) {


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


        course.setTeacher(
                teacher
        );


        if (
                course.getMaxCapacity() > 0
        ) {

            course.setPurchasable(
                    course.getCurrentEnrolled()
                            <
                            course.getMaxCapacity()
            );

        } else {

            course.setPurchasable(
                    true
            );
        }


        return courseRepository.save(
                course
        );
    }


    // =========================================================
    // KURS GÜNCELLE
    // =========================================================

    public Course updateCourseForTeacher(
            Long teacherId,
            Long courseId,
            Course updatedCourse) {


        currentUserService.requireTeacher(
                teacherId
        );


        Course existingCourse =
                getTeacherOwnedCourse(
                        teacherId,
                        courseId
                );


        if (
                updatedCourse.getName() != null
                        &&
                        !updatedCourse.getName().isBlank()
        ) {

            existingCourse.setName(
                    updatedCourse.getName()
            );
        }


        existingCourse.setDescription(
                updatedCourse.getDescription()
        );


        if (
                updatedCourse.getPrice() != null
        ) {

            existingCourse.setPrice(
                    updatedCourse.getPrice()
            );
        }


        int newCapacity =
                updatedCourse.getMaxCapacity();


        if (
                newCapacity > 0
                        &&
                        newCapacity
                                <
                                existingCourse
                                        .getCurrentEnrolled()
        ) {

            throw new IllegalArgumentException(
                    "Kurs kapasitesi mevcut öğrenci sayısından düşük olamaz."
            );
        }


        existingCourse.setMaxCapacity(
                newCapacity
        );


        if (
                updatedCourse.getImageUrl() != null
                        &&
                        !updatedCourse.getImageUrl().isBlank()
        ) {

            existingCourse.setImageUrl(
                    updatedCourse.getImageUrl()
            );
        }


        if (
                updatedCourse.getVideoPath() != null
                        &&
                        !updatedCourse.getVideoPath().isBlank()
        ) {

            existingCourse.setVideoPath(
                    updatedCourse.getVideoPath()
            );
        }


        if (
                existingCourse.getMaxCapacity() > 0
                        &&
                        existingCourse.getCurrentEnrolled()
                                >=
                                existingCourse.getMaxCapacity()
        ) {

            existingCourse.setPurchasable(
                    false
            );
        }


        return courseRepository.save(
                existingCourse
        );
    }


    // =========================================================
    // SATIŞA AÇ / KAPAT
    // =========================================================

    public Course setCoursePurchasable(
            Long teacherId,
            Long courseId,
            boolean purchasable) {


        currentUserService.requireTeacher(
                teacherId
        );


        Course course =
                getTeacherOwnedCourse(
                        teacherId,
                        courseId
                );


        if (
                purchasable
                        &&
                        course.getMaxCapacity() > 0
                        &&
                        course.getCurrentEnrolled()
                                >=
                                course.getMaxCapacity()
        ) {

            throw new IllegalStateException(
                    "Kurs kontenjanı dolu olduğu için satışa açılamaz."
            );
        }


        course.setPurchasable(
                purchasable
        );


        return courseRepository.save(
                course
        );
    }


    // =========================================================
    // VIDEO EKLE / DEĞİŞTİR
    // =========================================================

    public Course updateCourseVideo(
            Long teacherId,
            Long courseId,
            String videoPath) {


        currentUserService.requireTeacher(
                teacherId
        );


        if (
                videoPath == null
                        ||
                        videoPath.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Video yolu boş olamaz."
            );
        }


        Course course =
                getTeacherOwnedCourse(
                        teacherId,
                        courseId
                );


        course.setVideoPath(
                videoPath
        );


        return courseRepository.save(
                course
        );
    }


    // =========================================================
    // KURS SİL
    // =========================================================

    public void deleteCourseForTeacher(
            Long teacherId,
            Long courseId) {


        currentUserService.requireTeacher(
                teacherId
        );


        Course course =
                getTeacherOwnedCourse(
                        teacherId,
                        courseId
                );


        if (
                course.getCurrentEnrolled() > 0
        ) {

            throw new IllegalStateException(
                    "Öğrencisi bulunan bir kurs doğrudan silinemez. Kursu satışa kapatabilirsiniz."
            );
        }


        courseRepository.delete(
                course
        );
    }


    // =========================================================
    // ÖĞRENCİNİN KURSLARI
    // =========================================================

    @Transactional(readOnly = true)
    public List<PublicCourseResponse> getCoursesForStudent(
            Long studentId) {


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


        return student
                .getEnrolledCourses()
                .stream()
                .map(
                        this::toSafeCourseResponse
                )
                .toList();
    }


    // =========================================================
    // ÖĞRENCİ İÇİN SIGNED VIDEO URL
    // =========================================================

    @Transactional(readOnly = true)
    public String getSignedVideoUrlForStudent(
            Long studentId,
            Long courseId) {


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


        Course course =
                courseRepository
                        .findById(
                                courseId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Kurs bulunamadı."
                                )
                        );


        boolean enrolled =
                student
                        .getEnrolledCourses()
                        .stream()
                        .anyMatch(
                                studentCourse ->
                                        Objects.equals(
                                                studentCourse.getId(),
                                                courseId
                                        )
                        );


        if (
                !enrolled
        ) {

            throw new SecurityException(
                    "Bu videoyu izlemek için kursu satın almalısınız."
            );
        }


        if (
                course.getVideoPath() == null
                        ||
                        course.getVideoPath().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Bu kurs için henüz video yüklenmemiş."
            );
        }


        return imageKitClient
                .helper()
                .buildUrl(

                        SrcOptions.builder()

                                .urlEndpoint(
                                        imageKitUrlEndpoint
                                )

                                .src(
                                        course.getVideoPath()
                                )

                                .signed(
                                        true
                                )

                                .expiresIn(
                                        3600.0
                                )

                                .build()
                );
    }


    // =========================================================
    // ÖĞRETMEN KURS SAHİPLİĞİ KONTROLÜ
    // =========================================================

    private Course getTeacherOwnedCourse(
            Long teacherId,
            Long courseId) {


        Course course =
                courseRepository
                        .findById(
                                courseId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Kurs bulunamadı."
                                )
                        );


        if (
                course.getTeacher() == null
                        ||
                        !Objects.equals(
                                course
                                        .getTeacher()
                                        .getId(),
                                teacherId
                        )
        ) {

            throw new SecurityException(
                    "Bu kurs üzerinde işlem yapma yetkiniz yok."
            );
        }


        return course;
    }
}