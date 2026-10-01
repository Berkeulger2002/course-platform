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
// COURSE ENGAGEMENT
// =========================================================

export interface AdminCourseEngagement {

  courseId: number;

  courseName: string;

  progressRecords: number;

  completedRecords: number;

  completionRate: number;

  averageProgress: number;

  reviewCount: number;

  averageRating: number;
}


// =========================================================
// ENGAGEMENT RESPONSE
// =========================================================

export interface AdminEngagement {

  totalProgressRecords: number;

  completedProgressRecords: number;

  completionRate: number;

  averageProgressPercentage: number;

  activeStudentsLast7Days: number;

  totalReviews: number;

  averageRating: number;

  reviewsLast7Days: number;

  totalNotifications: number;

  unreadNotifications: number;

  notificationsLast7Days: number;

  courses: AdminCourseEngagement[];
}


@Injectable({
  providedIn: 'root'
})
export class AdminEngagementService {


  private readonly apiUrl =
    'http://localhost:8081/api/admin/engagement';


  constructor(
    private http: HttpClient
  ) {
  }


  // =========================================================
  // GET ENGAGEMENT
  // =========================================================

  getEngagement():
    Observable<AdminEngagement> {


    return this.http.get<AdminEngagement>(

      this.apiUrl

    );
  }
}
