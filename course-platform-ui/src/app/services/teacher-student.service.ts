import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface TeacherStudent {

  studentId: number;

  studentName: string;

  studentEmail: string;

  courseId: number;

  courseName: string;

  progressPercentage: number;

  completed: boolean;

  lastWatchedAt: string | null;
}


@Injectable({
  providedIn: 'root'
})
export class TeacherStudentService {

  private apiUrl =
    'http://localhost:8081/api/teacher-students';


  constructor(
    private http: HttpClient
  ) {}


  getTeacherStudents(
    teacherId: number
  ): Observable<TeacherStudent[]> {

    return this.http.get<TeacherStudent[]>(
      `${this.apiUrl}/teacher/${teacherId}`
    );
  }


  getCourseStudents(
    teacherId: number,
    courseId: number
  ): Observable<TeacherStudent[]> {

    return this.http.get<TeacherStudent[]>(
      `${this.apiUrl}/teacher/${teacherId}/course/${courseId}`
    );
  }
}
