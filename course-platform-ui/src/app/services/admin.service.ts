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
// TEACHER REGISTRATION
// =========================================================

export interface TeacherRegistrationRequest {

  id: number;

  name: string;

  email: string;

  status: string;

  requestedAt: string;
}


// =========================================================
// ADMIN USER
// =========================================================

export interface AdminUser {

  id: number;

  name: string;

  email: string;

  role:
    'STUDENT'
    | 'TEACHER'
    | 'ADMIN';
}


// =========================================================
// ADMIN USER SUMMARY
// =========================================================

export interface AdminUserSummary {

  totalUsers: number;

  students: number;

  teachers: number;

  admins: number;
}


// =========================================================
// ADMIN COURSE
// =========================================================

export interface AdminCourse {

  id: number;

  name: string;

  price: number;

  maxCapacity: number;

  currentEnrolled: number;

  purchasable: boolean;

  teacherId: number | null;

  teacherName: string;
}


// =========================================================
// ADMIN COURSE SUMMARY
// =========================================================

export interface AdminCourseSummary {

  totalCourses: number;

  purchasableCourses: number;

  closedCourses: number;

  totalEnrollments: number;
}


// =========================================================
// USER ACTIVITY SUMMARY
// =========================================================

export interface AdminUserActivitySummary {

  onlineNow: number;

  onlineStudents: number;

  onlineTeachers: number;

  todayLogins: number;

  averageActiveSeconds: number;
}


// =========================================================
// USER ACTIVITY
// =========================================================

export interface AdminUserActivity {

  sessionId: number;

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


@Injectable({
  providedIn: 'root'
})
export class AdminService {


  private readonly apiUrl =
    'http://localhost:8081/api/admin';


  constructor(
    private http: HttpClient
  ) {
  }


  // =========================================================
  // TEACHER REQUESTS
  // =========================================================

  getTeacherRequests():
    Observable<TeacherRegistrationRequest[]> {


    return this.http.get<TeacherRegistrationRequest[]>(

      `${this.apiUrl}/teacher-requests`

    );
  }


  getPendingTeacherRequests():
    Observable<TeacherRegistrationRequest[]> {


    return this.http.get<TeacherRegistrationRequest[]>(

      `${this.apiUrl}/teacher-requests/pending`

    );
  }


  approveTeacherRequest(
    requestId: number
  ): Observable<TeacherRegistrationRequest> {


    return this.http.post<TeacherRegistrationRequest>(

      `${this.apiUrl}/teacher-requests/${requestId}/approve`,

      {}

    );
  }


  resendTeacherVerificationCode(
    requestId: number
  ): Observable<TeacherRegistrationRequest> {


    return this.http.post<TeacherRegistrationRequest>(

      `${this.apiUrl}/teacher-requests/${requestId}/resend-code`,

      {}

    );
  }


  rejectTeacherRequest(
    requestId: number
  ): Observable<TeacherRegistrationRequest> {


    return this.http.post<TeacherRegistrationRequest>(

      `${this.apiUrl}/teacher-requests/${requestId}/reject`,

      {}

    );
  }


  // =========================================================
  // USERS
  // =========================================================

  getUsers():
    Observable<AdminUser[]> {


    return this.http.get<AdminUser[]>(

      `${this.apiUrl}/users`

    );
  }


  getUserSummary():
    Observable<AdminUserSummary> {


    return this.http.get<AdminUserSummary>(

      `${this.apiUrl}/users/summary`

    );
  }


  // =========================================================
  // COURSES
  // =========================================================

  getCourses():
    Observable<AdminCourse[]> {


    return this.http.get<AdminCourse[]>(

      `${this.apiUrl}/courses`

    );
  }


  getCourseSummary():
    Observable<AdminCourseSummary> {


    return this.http.get<AdminCourseSummary>(

      `${this.apiUrl}/courses/summary`

    );
  }


  // =========================================================
  // USER ACTIVITY
  // =========================================================

  getUserActivitySummary():
    Observable<AdminUserActivitySummary> {


    return this.http.get<AdminUserActivitySummary>(

      `${this.apiUrl}/user-activity/summary`

    );
  }


  getRecentUserActivities():
    Observable<AdminUserActivity[]> {


    return this.http.get<AdminUserActivity[]>(

      `${this.apiUrl}/user-activity/recent`

    );
  }
}
