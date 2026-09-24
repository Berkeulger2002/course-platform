import {
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

import {
  Observable
} from 'rxjs';

import {
  NotificationService
} from '../../services/notification.service';

import {
  AuthService,
  AuthUser
} from '../../services/auth.service';


@Component({
  selector: 'app-student-layout',

  standalone: true,

  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],

  templateUrl:
    './student-layout.component.html',

  styleUrl:
    './student-layout.component.css'
})
export class StudentLayoutComponent
  implements OnInit, OnDestroy {


  currentStudent:
    AuthUser | null = null;


  unreadCount$:
    Observable<number>;


  /*
   * Okunmamış bildirim sayısını
   * otomatik kontrol etmek için.
   */
  private notificationPollingId:
    ReturnType<typeof setInterval>
    | null = null;


  constructor(

    private router:
    Router,

    private notificationService:
    NotificationService,

    private authService:
    AuthService

  ) {


    this.unreadCount$ =
      this.notificationService
        .unreadCount$;
  }


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {


    this.authService
      .me()
      .subscribe({


        next: (
          user
        ) => {


          // =================================================
          // ROLE KONTROLÜ
          // =================================================

          if (
            user.role !==
            'STUDENT'
          ) {


            this.router.navigate([
              '/teacher/dashboard'
            ]);


            return;
          }


          // =================================================
          // CURRENT STUDENT
          // =================================================

          this.currentStudent =
            user;


          // =================================================
          // UI CACHE
          // =================================================

          localStorage.setItem(

            'currentUser',

            JSON.stringify(
              user
            )

          );


          // =================================================
          // İLK SAYIYI HEMEN AL
          // =================================================

          this.refreshNotificationCount();


          // =================================================
          // OTOMATİK BİLDİRİM KONTROLÜ
          //
          // Bildirimler sayfasına girmeye gerek kalmadan
          // sidebar sayısı güncellenir.
          //
          // 2000 ms = 2 saniye
          // =================================================

          this.notificationPollingId =
            setInterval(

              () => {

                this.refreshNotificationCount();

              },

              2000

            );
        },


        error: () => {


          localStorage.removeItem(
            'currentUser'
          );


          this.router.navigate(

            ['/auth'],

            {
              queryParams: {
                role: 'student'
              }
            }

          );
        }

      });
  }


  // =========================================================
  // BİLDİRİM SAYISINI BACKEND'DEN YENİLE
  // =========================================================

  refreshNotificationCount(): void {


    if (
      !this.currentStudent?.id
    ) {

      return;
    }


    this.notificationService
      .refreshUnreadCount(
        this.currentStudent.id
      );
  }


  // =========================================================
  // LOGOUT
  // =========================================================

  logout(): void {


    this.authService
      .logout()
      .subscribe({


        next: () => {

          this.finishLogout();

        },


        error: () => {

          this.finishLogout();

        }

      });
  }


  // =========================================================
  // LOGOUT CLEANUP
  // =========================================================

  private finishLogout(): void {


    localStorage.removeItem(
      'currentUser'
    );


    this.notificationService
      .setUnreadCount(
        0
      );


    this.router.navigate([
      '/'
    ]);
  }


  // =========================================================
  // COMPONENT KAPANIRKEN POLLING'İ DURDUR
  // =========================================================

  ngOnDestroy(): void {


    if (
      this.notificationPollingId
      !==
      null
    ) {


      clearInterval(
        this.notificationPollingId
      );


      this.notificationPollingId =
        null;
    }
  }
}
