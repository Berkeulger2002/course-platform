import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';


export interface AdminUserMailRequest {

  subject: string;

  message: string;
}


@Injectable({
  providedIn: 'root'
})
export class AdminUserMailService {


  private readonly apiUrl =
    'http://localhost:8081/api/admin/users';


  constructor(
    private http: HttpClient
  ) {
  }


  // =========================================================
  // SEND EMAIL
  // =========================================================

  sendEmail(

    userId: number,

    request: AdminUserMailRequest

  ): Observable<void> {


    return this.http.post<void>(

      `${this.apiUrl}/${userId}/email`,

      request

    );
  }
}
