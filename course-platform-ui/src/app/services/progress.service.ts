import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CourseProgress {
  courseId: number;
  courseName: string;
  imageUrl: string | null;

  lastPositionSeconds: number;
  maxPositionSeconds: number;
  durationSeconds: number;

  progressPercentage: number;
  completed: boolean;

  lastWatchedAt: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ProgressService {

  private apiUrl =
    'http://localhost:8081/api/progress';

  constructor(
    private http: HttpClient
  ) {}


  // İlerlemeyi güncelle
  updateProgress(
    studentId: number,
    courseId: number,
    currentTime: number,
    duration: number
  ): Observable<CourseProgress> {

    return this.http.put<CourseProgress>(
      `${this.apiUrl}/student/${studentId}/course/${courseId}`,
      {
        currentTime,
        duration
      }
    );
  }


  // Tek kursun ilerlemesini getir
  getCourseProgress(
    studentId: number,
    courseId: number
  ): Observable<CourseProgress> {

    return this.http.get<CourseProgress>(
      `${this.apiUrl}/student/${studentId}/course/${courseId}`
    );
  }


  // Öğrencinin bütün ilerlemelerini getir
  getStudentProgress(
    studentId: number
  ): Observable<CourseProgress[]> {

    return this.http.get<CourseProgress[]>(
      `${this.apiUrl}/student/${studentId}`
    );
  }
}
