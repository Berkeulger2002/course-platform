import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';


// =========================================================
// ORDER ITEM
// =========================================================

export interface AdminOrderItem {

  id: number;

  courseId: number;

  courseName: string;

  teacherId: number | null;

  teacherName: string | null;

  priceAtPurchase: number;
}


// =========================================================
// ORDER
// =========================================================

export interface AdminOrder {

  id: number;

  orderCode: string;

  studentId: number | null;

  studentName: string;

  studentEmail: string;

  totalPrice: number;

  itemCount: number;

  createdAt: string;

  items: AdminOrderItem[];
}


// =========================================================
// ORDER SUMMARY
// =========================================================

export interface AdminOrderSummary {

  totalOrders: number;

  totalRevenue: number;

  totalItemsSold: number;

  averageOrderValue: number;
}


@Injectable({
  providedIn: 'root'
})
export class AdminOrdersService {


  private readonly apiUrl =
    'http://localhost:8081/api/admin/orders';


  constructor(
    private http: HttpClient
  ) {
  }


  // =========================================================
  // GET ALL ORDERS
  // =========================================================

  getOrders():
    Observable<AdminOrder[]> {


    return this.http.get<AdminOrder[]>(

      this.apiUrl

    );
  }


  // =========================================================
  // GET SUMMARY
  // =========================================================

  getSummary():
    Observable<AdminOrderSummary> {


    return this.http.get<AdminOrderSummary>(

      `${this.apiUrl}/summary`

    );
  }
}
