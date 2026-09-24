import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  BehaviorSubject,
  Observable,
  tap
} from 'rxjs';


export interface TeacherProfile {

  id: number;

  name: string;

  email: string;

  role: string;
}


export interface TeacherProfileUpdateRequest {

  name: string;

  email: string;
}


@Injectable({
  providedIn: 'root'
})
export class TeacherService {

  private apiUrl =
    'http://localhost:8081/api/teachers';


  // =========================================================
  // PROFİL STATE
  // =========================================================
  //
  // Profil güncellendiğinde teacher layout da
  // anında yeni ismi görebilsin.
  //
  // =========================================================

  private profileSubject =
    new BehaviorSubject<TeacherProfile | null>(
      null
    );


  profile$ =
    this.profileSubject
      .asObservable();


  constructor(
    private http: HttpClient
  ) {}


  // =========================================================
  // PROFİL GETİR
  // =========================================================

  getProfile(
    teacherId: number
  ): Observable<TeacherProfile> {

    return this.http
      .get<TeacherProfile>(
        `${this.apiUrl}/${teacherId}/profile`
      )
      .pipe(

        tap(profile => {

          this.profileSubject
            .next(profile);

        })

      );
  }


  // =========================================================
  // PROFİL GÜNCELLE
  // =========================================================

  updateProfile(
    teacherId: number,
    request: TeacherProfileUpdateRequest
  ): Observable<TeacherProfile> {

    return this.http
      .put<TeacherProfile>(
        `${this.apiUrl}/${teacherId}/profile`,
        request
      )
      .pipe(

        tap(profile => {

          this.profileSubject
            .next(profile);

        })

      );
  }


  // =========================================================
  // FRONTEND'DEN PROFİL STATE GÜNCELLE
  // =========================================================

  setProfile(
    profile: TeacherProfile
  ): void {

    this.profileSubject
      .next(profile);
  }
}
