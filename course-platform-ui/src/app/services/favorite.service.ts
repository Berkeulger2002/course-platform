import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FavoriteService {

  private apiUrl =
    'http://localhost:8081/api/favorites';

  constructor(
    private http: HttpClient
  ) {}


  // =========================================================
  // ÖĞRENCİNİN FAVORİ KURSLARINI GETİR
  // =========================================================
  getFavorites(
    studentId: number
  ): Observable<any[]> {

    return this.http.get<any[]>(
      `${this.apiUrl}/student/${studentId}`
    );
  }


  // =========================================================
  // FAVORİYE EKLE
  // =========================================================
  addFavorite(
    studentId: number,
    courseId: number
  ): Observable<any> {

    return this.http.post(
      `${this.apiUrl}/student/${studentId}/course/${courseId}`,
      {}
    );
  }


  // =========================================================
  // FAVORİDEN ÇIKAR
  // =========================================================
  removeFavorite(
    studentId: number,
    courseId: number
  ): Observable<any> {

    return this.http.delete(
      `${this.apiUrl}/student/${studentId}/course/${courseId}`
    );
  }


  // =========================================================
  // KURS FAVORİDE Mİ?
  // =========================================================
  isFavorite(
    studentId: number,
    courseId: number
  ): Observable<{ favorite: boolean }> {

    return this.http.get<{ favorite: boolean }>(
      `${this.apiUrl}/student/${studentId}/course/${courseId}`
    );
  }
}
