import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';


export interface AdminUserSession {

  id: number;

  userId: number;

  userName: string;

  userEmail: string;

  role:
    'STUDENT'
    | 'TEACHER';

  loginAt: string;

  lastSeenAt: string;

  logoutAt:
    string | null;

  activeSeconds: number;

  status: string;

  online: boolean;
}


export interface AdminSessionSummary {

  totalSessions: number;

  onlineNow: number;

  onlineStudents: number;

  onlineTeachers: number;

  todayLogins: number;

  averageActiveSeconds: number;
}


@Injectable({
  providedIn: 'root'
})
export class AdminSessionsService {


  private readonly apiUrl =
    'http://localhost:8081/api/admin/sessions';


  constructor(
    private http: HttpClient
  ) {
  }


  getSessions():
    Observable<AdminUserSession[]> {


    return this.http.get<AdminUserSession[]>(

      this.apiUrl

    );
  }


  getSummary():
    Observable<AdminSessionSummary> {


    return this.http.get<AdminSessionSummary>(

      `${this.apiUrl}/summary`

    );
  }
}
