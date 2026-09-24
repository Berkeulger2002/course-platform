import { Injectable } from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';


export interface TeacherRevenueRecord {

  paymentId: number;

  studentId: number;

  studentName: string;

  studentEmail: string;

  courseId: number;

  courseName: string;

  amount: number;

  status: string;

  paymentMethod: string;

  transactionCode: string;

  purchasedAt: string;
}


@Injectable({
  providedIn: 'root'
})
export class TeacherRevenueService {

  private apiUrl =
    'http://localhost:8081/api/teacher-revenue';


  constructor(
    private http: HttpClient
  ) {}


  // =========================================================
  // TÜM SATIŞLAR
  // =========================================================

  getTeacherPayments(
    teacherId: number
  ): Observable<TeacherRevenueRecord[]> {

    return this.http
      .get<TeacherRevenueRecord[]>(
        `${this.apiUrl}/teacher/${teacherId}`
      );
  }


  // =========================================================
  // KURS SATIŞLARI
  // =========================================================

  getCoursePayments(
    teacherId: number,
    courseId: number
  ): Observable<TeacherRevenueRecord[]> {

    return this.http
      .get<TeacherRevenueRecord[]>(
        `${this.apiUrl}/teacher/${teacherId}/course/${courseId}`
      );
  }
}
