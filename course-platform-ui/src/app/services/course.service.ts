import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class CourseService {

  private apiUrl =
    'http://localhost:8081/api/courses';


  constructor(
    private http: HttpClient
  ) {}


  // =========================================================
  // TÜM KURSLAR
  // =========================================================
  getAllCourses(): Observable<any> {

    return this.http.get(
      this.apiUrl
    );
  }


  // =========================================================
  // YENİ KURS
  // =========================================================
  createCourseForTeacher(
    teacherId: number,
    courseData: any
  ): Observable<any> {

    return this.http.post(
      `${this.apiUrl}/teacher/${teacherId}`,
      courseData
    );
  }


  // =========================================================
  // ÖĞRETMENİN TÜM KURSLARI
  // =========================================================
  getAllCoursesForTeacher(
    teacherId: number
  ): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/teacher/${teacherId}`
    );
  }


  // =========================================================
  // ÖĞRETMENİN TEK KURSU
  // =========================================================
  getCourseForTeacher(
    teacherId: number,
    courseId: number
  ): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/${courseId}/teacher/${teacherId}`
    );
  }


  // =========================================================
  // KURS GÜNCELLE
  // =========================================================
  updateCourseForTeacher(
    teacherId: number,
    courseId: number,
    courseData: any
  ): Observable<any> {

    return this.http.put(
      `${this.apiUrl}/${courseId}/teacher/${teacherId}`,
      courseData
    );
  }


  // =========================================================
  // SATIŞA AÇ / KAPAT
  // =========================================================
  setCoursePurchasable(
    teacherId: number,
    courseId: number,
    purchasable: boolean
  ): Observable<any> {

    return this.http.patch(
      `${this.apiUrl}/${courseId}/teacher/${teacherId}/purchasable`,
      {
        purchasable
      }
    );
  }


  // =========================================================
  // VIDEO DEĞİŞTİR
  // =========================================================
  updateCourseVideo(
    teacherId: number,
    courseId: number,
    videoPath: string
  ): Observable<any> {

    return this.http.patch(
      `${this.apiUrl}/${courseId}/teacher/${teacherId}/video`,
      {
        videoPath
      }
    );
  }


  // =========================================================
  // KURS SİL
  // =========================================================
  deleteCourseForTeacher(
    teacherId: number,
    courseId: number
  ): Observable<any> {

    return this.http.delete(
      `${this.apiUrl}/${courseId}/teacher/${teacherId}`
    );
  }


  // =========================================================
  // ÖĞRENCİNİN SATIN ALDIĞI KURSLAR
  // =========================================================
  getEnrolledCourses(
    studentId: number
  ): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/student/${studentId}`
    );
  }


  // =========================================================
  // PRIVATE VIDEO SIGNED URL
  // =========================================================
  getCourseVideo(
    courseId: number,
    studentId: number
  ): Observable<{ videoUrl: string }> {

    return this.http.get<{ videoUrl: string }>(
      `${this.apiUrl}/${courseId}/video/student/${studentId}`
    );
  }


  // =========================================================
  // YORUM EKLE
  // =========================================================
  addReview(
    review: any
  ): Observable<any> {

    return this.http.post(
      'http://localhost:8081/api/reviews',
      review
    );
  }


  // =========================================================
  // KURS YORUMLARINI GETİR
  // =========================================================
  getCourseReviews(
    courseId: number
  ): Observable<any> {

    return this.http.get(
      `http://localhost:8081/api/reviews/course/${courseId}`
    );
  }
}
