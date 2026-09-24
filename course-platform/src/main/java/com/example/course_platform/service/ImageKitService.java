package com.example.course_platform.service;

import io.imagekit.client.ImageKitClient;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageKitService {

    private final ImageKitClient imageKitClient;

    public ImageKitService(ImageKitClient imageKitClient) {
        this.imageKitClient = imageKitClient;
    }

    /*
     * =========================================================
     * KURS KAPAK GÖRSELİ
     * =========================================================
     *
     * Public olarak yüklenir.
     * Öğrenci kurs kartında doğrudan görebilir.
     */
    public String uploadFile(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Yüklenecek görsel bulunamadı.");
        }

        FileUploadParams params = FileUploadParams.builder()
                .file(file.getBytes())
                .fileName(file.getOriginalFilename())
                .folder("/course-platform")
                .build();

        FileUploadResponse response =
                imageKitClient.files().upload(params);

        return response.url()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ImageKit görsel URL'si döndürmedi."
                        )
                );
    }


    /*
     * =========================================================
     * KURS VİDEOSU
     * =========================================================
     *
     * PRIVATE olarak yüklenir.
     *
     * Bu yüzden video normal ImageKit URL'siyle
     * doğrudan izlenemez.
     *
     * Öğrenci kursu satın aldıktan sonra backend
     * signed URL oluşturacak.
     */
    public String uploadPrivateVideo(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Yüklenecek video bulunamadı.");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException(
                    "Lütfen geçerli bir video dosyası yükleyin."
            );
        }

        FileUploadParams params = FileUploadParams.builder()
                .file(file.getBytes())
                .fileName(file.getOriginalFilename())

                // Videoları ayrı klasörde tutuyoruz
                .folder("/course-videos")

                // En önemli satır:
                // Videoyu herkese açık olmaktan çıkarır.
                .isPrivateFile(true)

                .build();

        FileUploadResponse response =
                imageKitClient.files().upload(params);

        /*
         * Veritabanına signed URL kaydetmiyoruz.
         *
         * Bunun yerine ImageKit içindeki kalıcı yolu:
         *
         * /course-videos/java-course_xxx.mp4
         *
         * kaydediyoruz.
         */
        return response.filePath()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ImageKit video yolu döndürmedi."
                        )
                );
    }
}