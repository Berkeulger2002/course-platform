import {
  Component,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  Router
} from '@angular/router';

import {
  forkJoin
} from 'rxjs';

import {
  AdminService,
  AdminUserActivity,
  AdminUserActivitySummary,
  TeacherRegistrationRequest
} from '../../services/admin.service';


@Component({
  selector: 'app-admin-overview',

  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './admin-overview.component.html',

  styleUrl:
    './admin-overview.component.css'
})
export class AdminOverviewComponent
  implements OnInit {


  // =========================================================
  // STATE
  // =========================================================

  isLoading =
    true;


  errorMessage =
    '';


  // =========================================================
  // TEACHER ONBOARDING
  // =========================================================

  totalApplications =
    0;


  pendingCount =
    0;


  codeSentCount =
    0;


  completedCount =
    0;


  rejectedCount =
    0;


  recentRequests:
    TeacherRegistrationRequest[] = [];


  // =========================================================
  // USER ACTIVITY
  // =========================================================

  activitySummary:
    AdminUserActivitySummary = {

    onlineNow: 0,

    onlineStudents: 0,

    onlineTeachers: 0,

    todayLogins: 0,

    averageActiveSeconds: 0
  };


  recentActivities:
    AdminUserActivity[] = [];


  constructor(

    private adminService:
    AdminService,

    private router:
    Router

  ) {
  }


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {


    this.loadOverview();
  }


  // =========================================================
  // LOAD OVERVIEW
  // =========================================================

  loadOverview(): void {


    this.isLoading =
      true;


    this.errorMessage =
      '';


    forkJoin({

      requests:
        this.adminService
          .getTeacherRequests(),

      activitySummary:
        this.adminService
          .getUserActivitySummary(),

      recentActivities:
        this.adminService
          .getRecentUserActivities()

    })
      .subscribe({


        next: (
          result
        ) => {


          // ===============================================
          // TEACHER APPLICATIONS
          // ===============================================

          const requests =
            result.requests;


          this.totalApplications =
            requests.length;


          this.pendingCount =

            requests.filter(

              request =>
                request.status ===
                'PENDING'

            ).length;


          this.codeSentCount =

            requests.filter(

              request =>
                request.status ===
                'CODE_SENT'

            ).length;


          this.completedCount =

            requests.filter(

              request =>
                request.status ===
                'COMPLETED'

            ).length;


          this.rejectedCount =

            requests.filter(

              request =>
                request.status ===
                'REJECTED'

            ).length;


          this.recentRequests =

            requests.slice(
              0,
              5
            );


          // ===============================================
          // USER ACTIVITY
          // ===============================================

          this.activitySummary =
            result.activitySummary;


          this.recentActivities =
            result.recentActivities;


          this.isLoading =
            false;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin overview hatası:',
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
            'Overview verileri yüklenemedi.';
        }

      });
  }


  // =========================================================
  // TEACHER STATUS
  // =========================================================

  getStatusLabel(
    status: string
  ): string {


    switch (
      status
      ) {


      case 'PENDING':

        return 'Onay Bekliyor';


      case 'CODE_SENT':

        return 'Kod Gönderildi';


      case 'COMPLETED':

        return 'Tamamlandı';


      case 'REJECTED':

        return 'Reddedildi';


      default:

        return status;
    }
  }


  // =========================================================
  // ACTIVITY STATUS
  // =========================================================

  getActivityStatusLabel(
    status: string
  ): string {


    switch (
      status
      ) {


      case 'ONLINE':

        return 'Online';


      case 'OFFLINE':

        return 'Offline';


      case 'LOGGED_OUT':

        return 'Çıkış Yaptı';


      case 'EXPIRED':

        return 'Süresi Doldu';


      default:

        return status;
    }
  }


  // =========================================================
  // ROLE
  // =========================================================

  getRoleLabel(
    role: string
  ): string {


    if (
      role === 'STUDENT'
    ) {


      return 'Öğrenci';
    }


    if (
      role === 'TEACHER'
    ) {


      return 'Öğretmen';
    }


    return role;
  }


  // =========================================================
  // ACTIVE DURATION
  // =========================================================

  formatDuration(
    seconds:
      number
      | null
      | undefined
  ): string {


    const safeSeconds =

      Math.max(
        0,
        seconds ?? 0
      );


    const hours =

      Math.floor(
        safeSeconds
        /
        3600
      );


    const minutes =

      Math.floor(
        (
          safeSeconds % 3600
        )
        /
        60
      );


    const remainingSeconds =

      Math.floor(
        safeSeconds % 60
      );


    if (
      hours > 0
    ) {


      return `${hours} sa ${minutes} dk`;
    }


    if (
      minutes > 0
    ) {


      return `${minutes} dk ${remainingSeconds} sn`;
    }


    return `${remainingSeconds} sn`;
  }
}
