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

import {
  UserActivityService
} from '../../services/user-activity.service';


@Component({
  selector:
    'app-student-layout',

  standalone:
    true,

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


  constructor(

    private router:
    Router,

    private notificationService:
    NotificationService,

    private authService:
    AuthService,

    private userActivityService:
    UserActivityService

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


          if (
            user.role !==
            'STUDENT'
          ) {


            this.router.navigate([

              '/teacher/dashboard'

            ]);


            return;
          }


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
          // USER ACTIVITY TRACKING
          // =================================================

          this.userActivityService
            .start();


          // =================================================
          // NOTIFICATION COUNT
          // =================================================

          this.notificationService
            .refreshUnreadCount(
              user.id
            );
        },


        error: () => {


          this.userActivityService
            .stop();


          localStorage.removeItem(
            'currentUser'
          );


          this.router.navigate(

            ['/auth'],

            {
              queryParams: {
                role:
                  'student'
              }
            }

          );
        }

      });
  }


  // =========================================================
  // LOGOUT
  // =========================================================

  logout(): void {


    // =====================================================
    // FRONTEND HEARTBEAT DURDUR
    //
    // Backend logout son heartbeat ile logout arasındaki
    // son makul süreyi ayrıca hesaplayacak.
    // =====================================================

    this.userActivityService
      .stop();


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
  // DESTROY
  // =========================================================

  ngOnDestroy(): void {


    this.userActivityService
      .stop();
  }
}
