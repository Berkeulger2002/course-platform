import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private apiUrl =
    'http://localhost:8081/api/orders';


  constructor(
    private http: HttpClient
  ) {}


  // =========================================================
  // SİPARİŞİ TAMAMLA
  // =========================================================
  placeOrder(
    studentId: number
  ): Observable<any> {

    return this.http.post(
      `${this.apiUrl}/student/${studentId}/place`,
      {}
    );
  }


  // =========================================================
  // ÖĞRENCİNİN SİPARİŞLERİ
  // =========================================================
  getStudentOrders(
    studentId: number
  ): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/student/${studentId}`
    );
  }


  // =========================================================
  // SİPARİŞ KODUNA GÖRE GETİR
  // =========================================================
  getOrderByCode(
    orderCode: string
  ): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/code/${orderCode}`
    );
  }
}
