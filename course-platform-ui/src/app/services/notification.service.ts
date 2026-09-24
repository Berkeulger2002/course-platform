import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import {
  BehaviorSubject,
  Observable
} from 'rxjs';


export interface AppNotification {

  id: number;

  title: string;

  message: string;

  type: string;

  read: boolean;

  createdAt: string;
}


@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private apiUrl =
    'http://localhost:8081/api/notifications';


  // =========================================================
  // ÖĞRENCİ SIDEBAR OKUNMAMIŞ SAYISI
  // =========================================================

  private unreadCountSubject =
    new BehaviorSubject<number>(0);


  unreadCount$ =
    this.unreadCountSubject
      .asObservable();


  // =========================================================
  // ÖĞRETMEN SIDEBAR OKUNMAMIŞ SAYISI
  // =========================================================

  private teacherUnreadCountSubject =
    new BehaviorSubject<number>(0);


  teacherUnreadCount$ =
    this.teacherUnreadCountSubject
      .asObservable();


  constructor(
    private http: HttpClient
  ) {}


  // =========================================================
  // =========================================================
  // ÖĞRENCİ BİLDİRİMLERİ
  // =========================================================
  // =========================================================


  // =========================================================
  // TÜM ÖĞRENCİ BİLDİRİMLERİ
  // =========================================================

  getNotifications(
    studentId: number
  ): Observable<AppNotification[]> {

    return this.http
      .get<AppNotification[]>(
        `${this.apiUrl}/student/${studentId}`
      );
  }


  // =========================================================
  // ÖĞRENCİ - TEK BİLDİRİMİ OKUNDU YAP
  // =========================================================

  markAsRead(
    studentId: number,
    notificationId: number
  ): Observable<AppNotification> {

    return this.http
      .put<AppNotification>(
        `${this.apiUrl}/student/${studentId}/${notificationId}/read`,
        {}
      );
  }


  // =========================================================
  // ÖĞRENCİ - TÜMÜNÜ OKUNDU YAP
  // =========================================================

  markAllAsRead(
    studentId: number
  ): Observable<void> {

    return this.http
      .put<void>(
        `${this.apiUrl}/student/${studentId}/read-all`,
        {}
      );
  }


  // =========================================================
  // ÖĞRENCİ - OKUNMAMIŞ SAYISI
  // =========================================================

  getUnreadCount(
    studentId: number
  ): Observable<{ count: number }> {

    return this.http
      .get<{ count: number }>(
        `${this.apiUrl}/student/${studentId}/unread-count`
      );
  }


  // =========================================================
  // ÖĞRENCİ SAYISINI YENİLE
  // =========================================================

  refreshUnreadCount(
    studentId: number
  ): void {

    this.getUnreadCount(
      studentId
    )
      .subscribe({

        next: response => {

          this.unreadCountSubject
            .next(
              response.count
            );
        },

        error: err => {

          console.error(
            'Okunmamış öğrenci bildirim sayısı alınamadı:',
            err
          );
        }

      });
  }


  // =========================================================
  // ÖĞRENCİ SAYISINI FRONTEND'DEN GÜNCELLE
  // =========================================================

  setUnreadCount(
    count: number
  ): void {

    this.unreadCountSubject
      .next(
        Math.max(
          count,
          0
        )
      );
  }


  // =========================================================
  // =========================================================
  // ÖĞRETMEN BİLDİRİMLERİ
  // =========================================================
  // =========================================================


  // =========================================================
  // ÖĞRETMEN - TÜM BİLDİRİMLER
  // =========================================================

  getTeacherNotifications(
    teacherId: number
  ): Observable<AppNotification[]> {

    return this.http
      .get<AppNotification[]>(
        `${this.apiUrl}/teacher/${teacherId}`
      );
  }


  // =========================================================
  // ÖĞRETMEN - TEK BİLDİRİMİ OKUNDU YAP
  // =========================================================

  markTeacherNotificationAsRead(
    teacherId: number,
    notificationId: number
  ): Observable<AppNotification> {

    return this.http
      .put<AppNotification>(
        `${this.apiUrl}/teacher/${teacherId}/${notificationId}/read`,
        {}
      );
  }


  // =========================================================
  // ÖĞRETMEN - TÜMÜNÜ OKUNDU YAP
  // =========================================================

  markAllTeacherNotificationsAsRead(
    teacherId: number
  ): Observable<void> {

    return this.http
      .put<void>(
        `${this.apiUrl}/teacher/${teacherId}/read-all`,
        {}
      );
  }


  // =========================================================
  // ÖĞRETMEN - OKUNMAMIŞ SAYISI
  // =========================================================

  getTeacherUnreadCount(
    teacherId: number
  ): Observable<{ count: number }> {

    return this.http
      .get<{ count: number }>(
        `${this.apiUrl}/teacher/${teacherId}/unread-count`
      );
  }


  // =========================================================
  // ÖĞRETMEN SAYISINI API'DEN YENİLE
  // =========================================================

  refreshTeacherUnreadCount(
    teacherId: number
  ): void {

    this.getTeacherUnreadCount(
      teacherId
    )
      .subscribe({

        next: response => {

          this.teacherUnreadCountSubject
            .next(
              response.count
            );
        },

        error: err => {

          console.error(
            'Okunmamış öğretmen bildirim sayısı alınamadı:',
            err
          );
        }

      });
  }


  // =========================================================
  // ÖĞRETMEN SAYISINI FRONTEND'DEN GÜNCELLE
  // =========================================================

  setTeacherUnreadCount(
    count: number
  ): void {

    this.teacherUnreadCountSubject
      .next(
        Math.max(
          count,
          0
        )
      );
  }
}
