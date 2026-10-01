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

  role:
    'STUDENT'
    | 'TEACHER'
    | 'ADMIN';
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


export interface TeacherRegistrationCreateRequest {

  name: string;

  email: string;
}


export interface TeacherRegistrationCompleteRequest {

  email: string;

  verificationCode: string;

  password: string;

  confirmPassword: string;
}


export interface TeacherRegistrationRequestResponse {

  id: number;

  name: string;

  email: string;

  status: string;

  requestedAt: string;
}


@Injectable({
  providedIn: 'root'
})
export class AuthService {


  // =========================================================
  // AUTH API
  // =========================================================

  private readonly authApiUrl =
    'http://localhost:8081/api/auth';


  // =========================================================
  // TEACHER REGISTRATION API
  // =========================================================

  private readonly teacherRegistrationApiUrl =
    'http://localhost:8081/api/teacher-registration';


  constructor(
    private http: HttpClient
  ) {
  }


  // =========================================================
  // STUDENT REGISTER
  // =========================================================

  register(
    userData: RegisterRequest
  ): Observable<string> {


    return this.http.post(

      `${this.authApiUrl}/register`,

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

      `${this.authApiUrl}/login`,

      credentials

    );
  }


  // =========================================================
  // TEACHER REGISTRATION REQUEST
  //
  // Henüz TEACHER hesabı oluşturmaz.
  //
  // Admin paneline PENDING başvuru gönderir.
  // =========================================================

  requestTeacherRegistration(
    request: TeacherRegistrationCreateRequest
  ): Observable<TeacherRegistrationRequestResponse> {


    return this.http.post<TeacherRegistrationRequestResponse>(

      `${this.teacherRegistrationApiUrl}/request`,

      request

    );
  }


  // =========================================================
  // COMPLETE TEACHER REGISTRATION
  //
  // Admin tarafından gönderilen doğrulama kodu kullanılır.
  //
  // Başarılı olduğunda gerçek TEACHER hesabı oluşturulur.
  // =========================================================

  completeTeacherRegistration(
    request: TeacherRegistrationCompleteRequest
  ): Observable<TeacherRegistrationRequestResponse> {


    return this.http.post<TeacherRegistrationRequestResponse>(

      `${this.teacherRegistrationApiUrl}/complete`,

      request

    );
  }


  // =========================================================
  // CURRENT USER
  // =========================================================

  me(): Observable<AuthUser> {


    return this.http.get<AuthUser>(

      `${this.authApiUrl}/me`

    );
  }


  // =========================================================
  // LOGOUT
  // =========================================================

  logout(): Observable<void> {


    return this.http.post<void>(

      `${this.authApiUrl}/logout`,

      {}

    );
  }
}
