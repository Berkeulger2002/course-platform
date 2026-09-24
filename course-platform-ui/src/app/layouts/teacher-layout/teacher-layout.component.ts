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
  Observable,
  Subscription
} from 'rxjs';

import {
  NotificationService
} from '../../services/notification.service';

import {
  TeacherService
} from '../../services/teacher.service';

import {
  AuthService,
  AuthUser
} from '../../services/auth.service';


@Component({
  selector:
    'app-teacher-layout',

  standalone:
    true,

  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],

  templateUrl:
    './teacher-layout.component.html',

  styleUrl:
    './teacher-layout.component.css'
})
export class TeacherLayoutComponent
  implements OnInit, OnDestroy {


  currentTeacher:
    AuthUser | null = null;


  teacherUnreadCount$:
    Observable<number>;


  private notificationPollingId:
    ReturnType<typeof setInterval>
    | null = null;


  private profileSubscription:
    Subscription | null = null;


  constructor(

    private router:
    Router,

    private notificationService:
    NotificationService,

    private teacherService:
    TeacherService,

    private authService:
    AuthService

  ) {


    this.teacherUnreadCount$ =
      this.notificationService
        .teacherUnreadCount$;
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
            'TEACHER'
          ) {


            this.router.navigate([

              '/student/dashboard'

            ]);


            return;
          }


          this.currentTeacher =
            user;


          localStorage.setItem(

            'currentUser',

            JSON.stringify(
              user
            )

          );


          this.initializeTeacherPanel();
        },


        error: () => {


          localStorage.removeItem(
            'currentUser'
          );


          this.router.navigate(

            ['/auth'],

            {
              queryParams: {
                role:
                  'teacher'
              }
            }

          );
        }

      });
  }


  // =========================================================
  // PANEL INITIALIZATION
  // =========================================================

  private initializeTeacherPanel(): void {


    // =====================================================
    // PROFILE
    // =====================================================

    this.profileSubscription =
      this.teacherService
        .profile$
        .subscribe(profile => {


          if (
            !profile
            ||
            !this.currentTeacher
          ) {

            return;
          }


          this.currentTeacher = {

            ...this.currentTeacher,

            name:
            profile.name,

            email:
            profile.email

          };


          localStorage.setItem(

            'currentUser',

            JSON.stringify(
              this.currentTeacher
            )

          );
        });


    // =====================================================
    // FIRST NOTIFICATION COUNT
    // =====================================================

    this.refreshNotificationCount();


    // =====================================================
    // POLLING
    // =====================================================

    this.notificationPollingId =
      setInterval(

        () => {

          this.refreshNotificationCount();

        },

        15000

      );
  }


  // =========================================================
  // NOTIFICATION COUNT
  // =========================================================

  refreshNotificationCount(): void {


    if (
      !this.currentTeacher?.id
    ) {

      return;
    }


    this.notificationService
      .refreshTeacherUnreadCount(
        this.currentTeacher.id
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


    this.notificationService
      .setTeacherUnreadCount(
        0
      );


    localStorage.removeItem(
      'currentUser'
    );


    this.router.navigate([
      '/'
    ]);
  }


  // =========================================================
  // DESTROY
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


    if (
      this.profileSubscription
    ) {


      this.profileSubscription
        .unsubscribe();


      this.profileSubscription =
        null;
    }
  }
}
