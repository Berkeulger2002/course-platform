import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';


export interface AuthUser {

  id: number;

  name: string;

  email: string;

  role: 'STUDENT' | 'TEACHER';
}


export interface RegisterRequest {

  name: string;

  email: string;

  password: string;
}


export interface LoginRequest {

  email: string;

  password: string;
}


@Injectable({
  providedIn: 'root'
})
export class AuthService {


  // =========================================================
  // API
  //
  // Production'da credentials interceptor
  // localhost URL'sini gerçek API origin'ine dönüştürür.
  // =========================================================

  private readonly apiUrl =
    'http://localhost:8081/api/auth';


  constructor(
    private http: HttpClient
  ) {
  }


  // =========================================================
  // REGISTER
  //
  // Public register yalnızca STUDENT oluşturur.
  //
  // Frontend artık role göndermez.
  // =========================================================

  register(
    userData: RegisterRequest
  ): Observable<string> {


    return this.http.post(

      `${this.apiUrl}/register`,

      userData,

      {
        responseType: 'text'
      }

    );
  }


  // =========================================================
  // LOGIN
  // =========================================================

  login(
    credentials: LoginRequest
  ): Observable<AuthUser> {


    return this.http.post<AuthUser>(

      `${this.apiUrl}/login`,

      credentials

    );
  }


  // =========================================================
  // CURRENT USER
  // =========================================================

  me(): Observable<AuthUser> {


    return this.http.get<AuthUser>(

      `${this.apiUrl}/me`

    );
  }


  // =========================================================
  // LOGOUT
  // =========================================================

  logout(): Observable<void> {


    return this.http.post<void>(

      `${this.apiUrl}/logout`,

      {}

    );
  }
}
