package com.example.course_platform.dto;


import java.util.List;


public record AdminEngagementResponse(

        long totalProgressRecords,

        long completedProgressRecords,

        double completionRate,

        double averageProgressPercentage,

        long activeStudentsLast7Days,

        long totalReviews,

        double averageRating,

        long reviewsLast7Days,

        long totalNotifications,

        long unreadNotifications,

        long notificationsLast7Days,

        List<AdminCourseEngagementResponse> courses

) {
}