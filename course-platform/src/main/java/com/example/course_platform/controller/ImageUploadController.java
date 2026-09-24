package com.example.course_platform.controller;

import com.example.course_platform.service.ImageKitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    private final ImageKitService imageKitService;

    public ImageUploadController(ImageKitService imageKitService) {
        this.imageKitService = imageKitService;
    }

    /*
     * =========================================================
     * PUBLIC KURS GÖRSELİ YÜKLEME
     * =========================================================
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file) {

        try {

            String imageUrl = imageKitService.uploadFile(file);

            return ResponseEntity.ok(
                    Map.of("url", imageUrl)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", "Invalid image file",
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "error", "Image upload failed",
                            "message", e.getMessage()
                    ));
        }
    }


    /*
     * =========================================================
     * PRIVATE KURS VİDEOSU YÜKLEME
     * =========================================================
     *
     * Video ImageKit'e private olarak yüklenir.
     * Buradan public URL değil, videoPath döner.
     *
     * Örnek:
     * /course-videos/java-course_xyz.mp4
     */
    @PostMapping("/upload-video")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile file) {

        try {

            String videoPath =
                    imageKitService.uploadPrivateVideo(file);

            return ResponseEntity.ok(
                    Map.of(
                            "videoPath", videoPath
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", "Invalid video file",
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "error", "Video upload failed",
                            "message", e.getMessage()
                    ));
        }
    }
}