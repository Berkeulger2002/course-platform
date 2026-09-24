import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Certificate {
  certificateId: number;
  courseId: number;
  courseName: string;
  imageUrl: string | null;
  certificateCode: string;
  issuedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class CertificateService {

  private apiUrl =
    'http://localhost:8081/api/certificates';

  constructor(
    private http: HttpClient
  ) {}


  // Öğrencinin tüm sertifikalarını getir
  getCertificates(
    studentId: number
  ): Observable<Certificate[]> {

    return this.http.get<Certificate[]>(
      `${this.apiUrl}/student/${studentId}`
    );
  }
}
