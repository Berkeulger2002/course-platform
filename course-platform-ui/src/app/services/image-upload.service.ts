import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ImageUploadService {

  private apiUrl = 'http://localhost:8081/api/images';

  constructor(private http: HttpClient) {}

  // Kurs kapak görselini yükler
  uploadImage(file: File): Observable<{ url: string }> {

    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<{ url: string }>(
      `${this.apiUrl}/upload`,
      formData
    );
  }

  // Kurs videosunu PRIVATE olarak yükler
  uploadVideo(file: File): Observable<{ videoPath: string }> {

    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<{ videoPath: string }>(
      `${this.apiUrl}/upload-video`,
      formData
    );
  }
}
