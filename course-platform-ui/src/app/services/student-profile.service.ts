import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface StudentProfile {

  id: number;

  name: string;

  email: string;

  role: string;

  enrolledCourseCount: number;

  completedCourseCount: number;

  certificateCount: number;
}


@Injectable({
  providedIn: 'root'
})
export class StudentProfileService {

  private apiUrl =
    'http://localhost:8081/api/students';


  constructor(
    private http: HttpClient
  ) {}


  getProfile(
    studentId: number
  ): Observable<StudentProfile> {

    return this.http.get<StudentProfile>(
      `${this.apiUrl}/${studentId}/profile`
    );
  }


  updateProfile(
    studentId: number,
    name: string,
    email: string
  ): Observable<StudentProfile> {

    return this.http.put<StudentProfile>(
      `${this.apiUrl}/${studentId}/profile`,
      {
        name,
        email
      }
    );
  }
}
