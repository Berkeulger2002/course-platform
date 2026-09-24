import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface PaymentHistoryRecord {

  id: number;

  courseId: number;

  courseName: string;

  imageUrl: string | null;

  amount: number;

  status: string;

  paymentMethod: string;

  transactionCode: string;

  purchasedAt: string;
}


@Injectable({
  providedIn: 'root'
})
export class PaymentHistoryService {

  private apiUrl =
    'http://localhost:8081/api/payment-history';


  constructor(
    private http: HttpClient
  ) {}


  // =========================================================
  // ÖĞRENCİNİN ÖDEME GEÇMİŞİ
  // =========================================================
  getStudentPaymentHistory(
    studentId: number
  ): Observable<PaymentHistoryRecord[]> {

    return this.http.get<PaymentHistoryRecord[]>(
      `${this.apiUrl}/student/${studentId}`
    );
  }


  // =========================================================
  // SATIN ALMA KAYDI OLUŞTUR
  // =========================================================
  createPaymentRecord(
    studentId: number,
    courseId: number
  ): Observable<PaymentHistoryRecord> {

    return this.http.post<PaymentHistoryRecord>(
      `${this.apiUrl}/student/${studentId}/course/${courseId}`,
      {}
    );
  }
}
