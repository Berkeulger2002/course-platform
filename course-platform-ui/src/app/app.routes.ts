import {
  Routes
} from '@angular/router';


import {
  HomeComponent
} from './home/home.component';

import {
  AuthComponent
} from './components/auth/auth.component';


import {
  TeacherDashboardComponent
} from './teacher-dashboard/teacher-dashboard.component';

import {
  StudentDashboardComponent
} from './student-dashboard/student-dashboard.component';


import {
  StudentLayoutComponent
} from './layouts/student-layout/student-layout.component';

import {
  TeacherLayoutComponent
} from './layouts/teacher-layout/teacher-layout.component';


import {
  studentGuard,
  studentChildGuard,
  teacherGuard,
  teacherChildGuard
} from './guards/auth.guard';


// =========================================================
// STUDENT
// =========================================================

import {
  MyCoursesComponent
} from './student/my-courses/my-courses.component';

import {
  MarketplaceComponent
} from './student/marketplace/marketplace.component';

import {
  ProfileComponent
} from './student/profile/profile.component';

import {
  WatchHistoryComponent
} from './student/watch-history/watch-history.component';

import {
  ProgressComponent
} from './student/progress/progress.component';

import {
  CertificatesComponent
} from './student/certificates/certificates.component';

import {
  CertificateViewComponent
} from './student/certificate-view/certificate-view.component';

import {
  FavoritesComponent
} from './student/favorites/favorites.component';

import {
  NotificationsComponent
} from './student/notifications/notifications.component';

import {
  PaymentHistoryComponent
} from './student/payment-history/payment-history.component';


// =========================================================
// TEACHER
// =========================================================

import {
  MyCoursesComponent as TeacherMyCoursesComponent
} from './teacher/my-courses/my-courses.component';

import {
  CreateCourseComponent
} from './teacher/create-course/create-course.component';

import {
  EditCourseComponent
} from './teacher/edit-course/edit-course.component';

import {
  StudentsComponent
} from './teacher/students/students.component';

import {
  AnalyticsComponent
} from './teacher/analytics/analytics.component';

import {
  ReviewsComponent
} from './teacher/reviews/reviews.component';

import {
  RevenueComponent
} from './teacher/revenue/revenue.component';

import {
  NotificationsComponent as TeacherNotificationsComponent
} from './teacher/notifications/notifications.component';

import {
  ProfileComponent as TeacherProfileComponent
} from './teacher/profile/profile.component';


// =========================================================
// ROUTES
// =========================================================

export const routes:
  Routes = [


  // =========================================================
  // HOME
  // =========================================================

  {
    path: '',
    component: HomeComponent
  },


  // =========================================================
  // AUTH
  // =========================================================

  {
    path: 'auth',
    component: AuthComponent
  },


  // =========================================================
  // STUDENT
  // =========================================================

  {
    path: 'student',

    component:
    StudentLayoutComponent,

    canActivate: [
      studentGuard
    ],

    canActivateChild: [
      studentChildGuard
    ],

    children: [


      {
        path: '',

        redirectTo:
          'dashboard',

        pathMatch:
          'full'
      },


      {
        path:
          'dashboard',

        component:
        StudentDashboardComponent
      },


      {
        path:
          'courses',

        component:
        MyCoursesComponent
      },


      {
        path:
          'marketplace',

        component:
        MarketplaceComponent
      },


      {
        path:
          'favorites',

        component:
        FavoritesComponent
      },


      {
        path:
          'progress',

        component:
        ProgressComponent
      },


      {
        path:
          'watch-history',

        component:
        WatchHistoryComponent
      },


      {
        path:
          'certificates',

        component:
        CertificatesComponent
      },


      {
        path:
          'certificate/:id',

        component:
        CertificateViewComponent
      },


      {
        path:
          'notifications',

        component:
        NotificationsComponent
      },


      {
        path:
          'payment-history',

        component:
        PaymentHistoryComponent
      },


      {
        path:
          'profile',

        component:
        ProfileComponent
      }

    ]
  },


  // =========================================================
  // TEACHER
  // =========================================================

  {
    path:
      'teacher',

    component:
    TeacherLayoutComponent,

    canActivate: [
      teacherGuard
    ],

    canActivateChild: [
      teacherChildGuard
    ],

    children: [


      {
        path: '',

        redirectTo:
          'dashboard',

        pathMatch:
          'full'
      },


      {
        path:
          'dashboard',

        component:
        TeacherDashboardComponent
      },


      {
        path:
          'courses',

        component:
        TeacherMyCoursesComponent
      },


      {
        path:
          'create-course',

        component:
        CreateCourseComponent
      },


      {
        path:
          'edit-course/:id',

        component:
        EditCourseComponent
      },


      {
        path:
          'students',

        component:
        StudentsComponent
      },


      {
        path:
          'analytics',

        component:
        AnalyticsComponent
      },


      {
        path:
          'reviews',

        component:
        ReviewsComponent
      },


      {
        path:
          'revenue',

        component:
        RevenueComponent
      },


      {
        path:
          'notifications',

        component:
        TeacherNotificationsComponent
      },


      {
        path:
          'profile',

        component:
        TeacherProfileComponent
      }

    ]
  },


  // =========================================================
  // OLD STUDENT URL
  // =========================================================

  {
    path:
      'student-dashboard',

    redirectTo:
      'student/dashboard',

    pathMatch:
      'full'
  },


  // =========================================================
  // OLD TEACHER URL
  // =========================================================

  {
    path:
      'teacher-dashboard',

    redirectTo:
      'teacher/dashboard',

    pathMatch:
      'full'
  }

];
