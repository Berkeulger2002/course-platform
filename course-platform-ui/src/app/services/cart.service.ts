import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class CartService {

  private apiUrl =
    'http://localhost:8081/api/carts';


  constructor(
    private http: HttpClient
  ) {}


  // =========================================================
  // ÖĞRENCİNİN SEPETİNİ GETİR
  // =========================================================
  getCart(
    studentId: number
  ): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/student/${studentId}`
    );
  }


  // =========================================================
  // KURSU BACKEND SEPETİNE EKLE
  // =========================================================
  addCourseToCart(
    studentId: number,
    courseId: number
  ): Observable<any> {

    return this.http.post(
      `${this.apiUrl}/student/${studentId}/add-course/${courseId}`,
      {}
    );
  }


  // =========================================================
  // SEPETTEN CART ITEM ÇIKAR
  // =========================================================
  removeCartItem(
    studentId: number,
    cartItemId: number
  ): Observable<any> {

    return this.http.delete(
      `${this.apiUrl}/student/${studentId}/remove-item/${cartItemId}`
    );
  }


  // =========================================================
  // SEPETİ TAMAMEN BOŞALT
  // =========================================================
  emptyCart(
    studentId: number
  ): Observable<any> {

    return this.http.delete(
      `${this.apiUrl}/student/${studentId}/empty`
    );
  }
}
