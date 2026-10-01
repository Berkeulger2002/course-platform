package com.example.course_platform.service;


import com.example.course_platform.document.CourseReview;

import com.example.course_platform.dto.AdminCourseEngagementResponse;
import com.example.course_platform.dto.AdminEngagementResponse;

import com.example.course_platform.entity.Course;
import com.example.course_platform.entity.CourseProgress;
import com.example.course_platform.entity.Notification;

import com.example.course_platform.repository.CourseProgressRepository;
import com.example.course_platform.repository.CourseRepository;
import com.example.course_platform.repository.CourseReviewRepository;
import com.example.course_platform.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AdminEngagementService {


    private final CourseProgressRepository
            courseProgressRepository;


    private final CourseReviewRepository
            courseReviewRepository;


    private final NotificationRepository
            notificationRepository;


    private final CourseRepository
            courseRepository;


    // =========================================================
    // ADMIN ENGAGEMENT
    // =========================================================

    @Transactional(
            readOnly = true
    )
    public AdminEngagementResponse
    getEngagement() {


        // =====================================================
        // DATA
        // =====================================================

        List<CourseProgress> progressRecords =

                courseProgressRepository
                        .findAll();


        List<CourseReview> reviews =

                courseReviewRepository
                        .findAll();


        List<Notification> notifications =

                notificationRepository
                        .findAll();


        List<Course> courses =

                courseRepository
                        .findAll();


        LocalDateTime sevenDaysAgo =

                LocalDateTime
                        .now()
                        .minusDays(
                                7
                        );


        // =====================================================
        // PROGRESS
        // =====================================================

        long totalProgressRecords =
                progressRecords.size();


        long completedProgressRecords =

                progressRecords
                        .stream()

                        .filter(
                                CourseProgress::isCompleted
                        )

                        .count();


        double completionRate =

                totalProgressRecords == 0
                        ?
                        0
                        :
                        (
                                completedProgressRecords
                                        *
                                        100.0
                                        /
                                        totalProgressRecords
                        );


        double averageProgressPercentage =

                progressRecords
                        .stream()

                        .mapToDouble(
                                CourseProgress::getProgressPercentage
                        )

                        .average()

                        .orElse(
                                0
                        );


        // =====================================================
        // ACTIVE STUDENTS - LAST 7 DAYS
        //
        // Aynı öğrenci farklı kurslarda ilerleme kaydı
        // oluşturmuş olsa bile yalnızca 1 kez sayılır.
        // =====================================================

        long activeStudentsLast7Days =

                progressRecords
                        .stream()

                        .filter(
                                progress ->

                                        progress.getLastWatchedAt()
                                                != null
                                                &&
                                                !progress
                                                        .getLastWatchedAt()
                                                        .isBefore(
                                                                sevenDaysAgo
                                                        )
                        )

                        .map(
                                CourseProgress::getStudent
                        )

                        .filter(
                                Objects::nonNull
                        )

                        .map(
                                student ->
                                        student.getId()
                        )

                        .filter(
                                Objects::nonNull
                        )

                        .distinct()

                        .count();


        // =====================================================
        // REVIEWS
        // =====================================================

        long totalReviews =
                reviews.size();


        double averageRating =

                reviews
                        .stream()

                        .mapToInt(
                                CourseReview::getRating
                        )

                        .average()

                        .orElse(
                                0
                        );


        long reviewsLast7Days =

                reviews
                        .stream()

                        .filter(
                                review ->

                                        review.getCreatedAt()
                                                != null
                                                &&
                                                !review
                                                        .getCreatedAt()
                                                        .isBefore(
                                                                sevenDaysAgo
                                                        )
                        )

                        .count();


        // =====================================================
        // NOTIFICATIONS
        // =====================================================

        long totalNotifications =
                notifications.size();


        long unreadNotifications =

                notifications
                        .stream()

                        .filter(
                                notification ->
                                        !notification.isRead()
                        )

                        .count();


        long notificationsLast7Days =

                notifications
                        .stream()

                        .filter(
                                notification ->

                                        notification.getCreatedAt()
                                                != null
                                                &&
                                                !notification
                                                        .getCreatedAt()
                                                        .isBefore(
                                                                sevenDaysAgo
                                                        )
                        )

                        .count();


        // =====================================================
        // COURSE MAP
        // =====================================================

        Map<Long, Course>
                courseById =
                new HashMap<>();


        for (
                Course course
                :
                courses
        ) {


            if (
                    course.getId()
                            != null
            ) {


                courseById.put(

                        course.getId(),

                        course
                );
            }
        }


        // =====================================================
        // COURSE ENGAGEMENT
        // =====================================================

        List<AdminCourseEngagementResponse>
                courseEngagement =
                new ArrayList<>();


        for (
                Course course
                :
                courses
        ) {


            Long courseId =
                    course.getId();


            // =================================================
            // PROGRESS FOR COURSE
            // =================================================

            List<CourseProgress>
                    courseProgressRecords =

                    progressRecords
                            .stream()

                            .filter(
                                    progress ->

                                            progress.getCourse()
                                                    != null
                                                    &&
                                                    Objects.equals(

                                                            progress
                                                                    .getCourse()
                                                                    .getId(),

                                                            courseId
                                                    )
                            )

                            .toList();


            long courseProgressCount =
                    courseProgressRecords.size();


            long courseCompletedCount =

                    courseProgressRecords
                            .stream()

                            .filter(
                                    CourseProgress::isCompleted
                            )

                            .count();


            double courseCompletionRate =

                    courseProgressCount == 0
                            ?
                            0
                            :
                            (
                                    courseCompletedCount
                                            *
                                            100.0
                                            /
                                            courseProgressCount
                            );


            double courseAverageProgress =

                    courseProgressRecords
                            .stream()

                            .mapToDouble(
                                    CourseProgress::getProgressPercentage
                            )

                            .average()

                            .orElse(
                                    0
                            );


            // =================================================
            // REVIEWS FOR COURSE
            // =================================================

            List<CourseReview>
                    courseReviews =

                    reviews
                            .stream()

                            .filter(
                                    review ->

                                            Objects.equals(

                                                    review.getCourseId(),

                                                    courseId
                                            )
                            )

                            .toList();


            long reviewCount =
                    courseReviews.size();


            double courseAverageRating =

                    courseReviews
                            .stream()

                            .mapToInt(
                                    CourseReview::getRating
                            )

                            .average()

                            .orElse(
                                    0
                            );


            courseEngagement.add(

                    new AdminCourseEngagementResponse(

                            courseId,

                            course.getName(),

                            courseProgressCount,

                            courseCompletedCount,

                            round(
                                    courseCompletionRate
                            ),

                            round(
                                    courseAverageProgress
                            ),

                            reviewCount,

                            round(
                                    courseAverageRating
                            )
                    )
            );
        }


        // =====================================================
        // EN ÇOK ENGAGEMENT OLAN KURS ÖNCE
        //
        // Öncelik:
        // progress record sayısı
        // =====================================================

        courseEngagement.sort(

                (
                        first,
                        second
                ) ->

                        Long.compare(

                                second.progressRecords(),

                                first.progressRecords()
                        )
        );


        // =====================================================
        // RESPONSE
        // =====================================================

        return new AdminEngagementResponse(

                totalProgressRecords,

                completedProgressRecords,

                round(
                        completionRate
                ),

                round(
                        averageProgressPercentage
                ),

                activeStudentsLast7Days,

                totalReviews,

                round(
                        averageRating
                ),

                reviewsLast7Days,

                totalNotifications,

                unreadNotifications,

                notificationsLast7Days,

                courseEngagement
        );
    }


    // =========================================================
    // ROUND
    // =========================================================

    private double round(
            double value
    ) {


        return Math.round(
                value * 100.0
        )
                /
                100.0;
    }
}