package com.example.course_platform.repository;

import com.example.course_platform.document.CourseReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CourseReviewRepository extends MongoRepository<CourseReview, String> {

    // Spring Boot bu ismi görünce arka planda tüm yorumları getirecek!
    List<CourseReview> findByCourseId(Long courseId);
}