package com.example.course_platform.controller;

import com.example.course_platform.dto.NotificationResponse;
import com.example.course_platform.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    // =========================================================
    // ÖĞRENCİ - TÜM BİLDİRİMLER
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<NotificationResponse>>
    getNotifications(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                notificationService
                        .getStudentNotifications(
                                studentId
                        )
        );
    }


    // =========================================================
    // ÖĞRENCİ - TEK BİLDİRİMİ OKUNDU YAP
    // =========================================================

    @PutMapping(
            "/student/{studentId}/{notificationId}/read"
    )
    public ResponseEntity<NotificationResponse>
    markAsRead(
            @PathVariable Long studentId,
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                notificationService
                        .markAsRead(
                                studentId,
                                notificationId
                        )
        );
    }


    // =========================================================
    // ÖĞRENCİ - TÜMÜNÜ OKUNDU YAP
    // =========================================================

    @PutMapping(
            "/student/{studentId}/read-all"
    )
    public ResponseEntity<Void>
    markAllAsRead(
            @PathVariable Long studentId) {

        notificationService
                .markAllAsRead(
                        studentId
                );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =========================================================
    // ÖĞRENCİ - OKUNMAMIŞ SAYISI
    // =========================================================

    @GetMapping(
            "/student/{studentId}/unread-count"
    )
    public ResponseEntity<Map<String, Long>>
    getUnreadCount(
            @PathVariable Long studentId) {

        long count =
                notificationService
                        .getUnreadCount(
                                studentId
                        );


        return ResponseEntity.ok(
                Map.of(
                        "count",
                        count
                )
        );
    }


    // =========================================================
    // ÖĞRETMEN - TÜM BİLDİRİMLER
    // =========================================================

    @GetMapping(
            "/teacher/{teacherId}"
    )
    public ResponseEntity<?>
    getTeacherNotifications(
            @PathVariable Long teacherId) {

        try {

            return ResponseEntity.ok(
                    notificationService
                            .getTeacherNotifications(
                                    teacherId
                            )
            );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // ÖĞRETMEN - TEK BİLDİRİMİ OKUNDU YAP
    // =========================================================

    @PutMapping(
            "/teacher/{teacherId}/{notificationId}/read"
    )
    public ResponseEntity<?>
    markTeacherNotificationAsRead(
            @PathVariable Long teacherId,
            @PathVariable Long notificationId) {

        try {

            return ResponseEntity.ok(
                    notificationService
                            .markTeacherNotificationAsRead(
                                    teacherId,
                                    notificationId
                            )
            );

        } catch (
                SecurityException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.FORBIDDEN
                    )
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // ÖĞRETMEN - TÜMÜNÜ OKUNDU YAP
    // =========================================================

    @PutMapping(
            "/teacher/{teacherId}/read-all"
    )
    public ResponseEntity<Void>
    markAllTeacherNotificationsAsRead(
            @PathVariable Long teacherId) {

        notificationService
                .markAllTeacherNotificationsAsRead(
                        teacherId
                );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =========================================================
    // ÖĞRETMEN - OKUNMAMIŞ SAYISI
    // =========================================================

    @GetMapping(
            "/teacher/{teacherId}/unread-count"
    )
    public ResponseEntity<Map<String, Long>>
    getTeacherUnreadCount(
            @PathVariable Long teacherId) {

        long count =
                notificationService
                        .getTeacherUnreadCount(
                                teacherId
                        );


        return ResponseEntity.ok(
                Map.of(
                        "count",
                        count
                )
        );
    }
}