import {
  Component,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  AdminCourseEngagement,
  AdminEngagement,
  AdminEngagementService
} from './admin-engagement.service';


@Component({
  selector: 'app-admin-engagement',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './admin-engagement.component.html',

  styleUrl:
    './admin-engagement.component.css'
})
export class AdminEngagementComponent
  implements OnInit {


  // =========================================================
  // DATA
  // =========================================================

  engagement:
    AdminEngagement = {

    totalProgressRecords: 0,

    completedProgressRecords: 0,

    completionRate: 0,

    averageProgressPercentage: 0,

    activeStudentsLast7Days: 0,

    totalReviews: 0,

    averageRating: 0,

    reviewsLast7Days: 0,

    totalNotifications: 0,

    unreadNotifications: 0,

    notificationsLast7Days: 0,

    courses: []
  };


  filteredCourses:
    AdminCourseEngagement[] = [];


  searchTerm =
    '';


  isLoading =
    true;


  errorMessage =
    '';


  constructor(

    private engagementService:
    AdminEngagementService,

    private router:
    Router

  ) {
  }


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {


    this.loadEngagement();
  }


  // =========================================================
  // LOAD
  // =========================================================

  loadEngagement(): void {


    this.isLoading =
      true;


    this.errorMessage =
      '';


    this.engagementService
      .getEngagement()
      .subscribe({


        next: (
          engagement
        ) => {


          this.engagement =
            engagement;


          this.applyFilter();


          this.isLoading =
            false;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin engagement hatası:',
            error
          );


          this.isLoading =
            false;


          if (
            error?.status === 401
          ) {


            this.router.navigate([
              '/admin/login'
            ]);


            return;
          }


          if (
            error?.status === 403
          ) {


            this.errorMessage =
              'Bu sayfaya erişim yetkiniz bulunmuyor.';


            return;
          }


          this.errorMessage =
            'Engagement verileri yüklenemedi.';
        }

      });
  }


  // =========================================================
  // FILTER
  // =========================================================

  applyFilter(): void {


    const search =
      this.searchTerm
        .trim()
        .toLowerCase();


    if (
      !search
    ) {


      this.filteredCourses =
        this.engagement.courses;


      return;
    }


    this.filteredCourses =

      this.engagement.courses
        .filter(

          course =>

            course.courseName
              ?.toLowerCase()
              .includes(
                search
              )

            ||

            String(
              course.courseId
            ).includes(
              search
            )
        );
  }


  // =========================================================
  // PERCENTAGE
  // =========================================================

  formatPercentage(
    value: number | null | undefined
  ): string {


    return `${this.safeNumber(value).toFixed(1)}%`;
  }


  // =========================================================
  // RATING
  // =========================================================

  formatRating(
    value: number | null | undefined
  ): string {


    return this.safeNumber(
      value
    ).toFixed(
      1
    );
  }


  // =========================================================
  // PROGRESS WIDTH
  // =========================================================

  getProgressWidth(
    value: number | null | undefined
  ): number {


    const safe =
      this.safeNumber(
        value
      );


    if (
      safe < 0
    ) {

      return 0;
    }


    if (
      safe > 100
    ) {

      return 100;
    }


    return safe;
  }


  // =========================================================
  // NUMBER SAFETY
  // =========================================================

  private safeNumber(
    value: number | null | undefined
  ): number {


    if (
      value === null
      ||
      value === undefined
      ||
      Number.isNaN(
        value
      )
    ) {


      return 0;
    }


    return value;
  }


  // =========================================================
  // REFRESH
  // =========================================================

  refresh(): void {


    this.loadEngagement();
  }
}
