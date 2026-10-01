import {
  inject
} from '@angular/core';

import {
  CanActivateChildFn,
  CanActivateFn,
  Router
} from '@angular/router';

import {
  catchError,
  map,
  of
} from 'rxjs';

import {
  AuthService
} from '../services/auth.service';


// =========================================================
// ROLE TYPE
// =========================================================

type UserRole =
  'STUDENT'
  | 'TEACHER'
  | 'ADMIN';


type LoginRole =
  'student'
  | 'teacher'
  | 'admin';


// =========================================================
// COMMON ROLE CHECK
// =========================================================

function checkRole(

  expectedRole:
  UserRole,

  loginRole:
  LoginRole

) {


  const authService =
    inject(
      AuthService
    );


  const router =
    inject(
      Router
    );


  return authService
    .me()
    .pipe(


      map(user => {


        // =====================================================
        // DOĞRU ROLE
        // =====================================================

        if (
          user.role ===
          expectedRole
        ) {


          localStorage.setItem(

            'currentUser',

            JSON.stringify(
              user
            )

          );


          return true;
        }


        // =====================================================
        // ADMIN
        // =====================================================

        if (
          user.role ===
          'ADMIN'
        ) {


          return router.createUrlTree([

            '/admin/dashboard'

          ]);
        }


        // =====================================================
        // TEACHER
        // =====================================================

        if (
          user.role ===
          'TEACHER'
        ) {


          return router.createUrlTree([

            '/teacher/dashboard'

          ]);
        }


        // =====================================================
        // STUDENT
        // =====================================================

        if (
          user.role ===
          'STUDENT'
        ) {


          return router.createUrlTree([

            '/student/dashboard'

          ]);
        }


        // =====================================================
        // BİLİNMEYEN ROLE
        // =====================================================

        return createLoginRedirect(
          router,
          loginRole
        );
      }),


      // =======================================================
      // AUTH YOK / COOKIE GEÇERSİZ
      // =======================================================

      catchError(() => {


        localStorage.removeItem(
          'currentUser'
        );


        return of(

          createLoginRedirect(
            router,
            loginRole
          )

        );
      })

    );
}


// =========================================================
// LOGIN REDIRECT
// =========================================================

function createLoginRedirect(

  router:
  Router,

  loginRole:
  LoginRole

) {


  // =========================================================
  // ADMIN LOGIN
  // =========================================================

  if (
    loginRole ===
    'admin'
  ) {


    return router.createUrlTree([

      '/admin/login'

    ]);
  }


  // =========================================================
  // STUDENT / TEACHER LOGIN
  // =========================================================

  return router.createUrlTree(

    ['/auth'],

    {
      queryParams: {

        role:
        loginRole

      }
    }

  );
}


// =========================================================
// STUDENT
// =========================================================

export const studentGuard:
  CanActivateFn = () => {


  return checkRole(
    'STUDENT',
    'student'
  );
};


export const studentChildGuard:
  CanActivateChildFn = () => {


  return checkRole(
    'STUDENT',
    'student'
  );
};


// =========================================================
// TEACHER
// =========================================================

export const teacherGuard:
  CanActivateFn = () => {


  return checkRole(
    'TEACHER',
    'teacher'
  );
};


export const teacherChildGuard:
  CanActivateChildFn = () => {


  return checkRole(
    'TEACHER',
    'teacher'
  );
};

// =========================================================
// ADMIN CHILD
// =========================================================

export const adminChildGuard:
  CanActivateChildFn = () => {


  return checkRole(
    'ADMIN',
    'admin'
  );
};
// =========================================================
// ADMIN
// =========================================================

export const adminGuard:
  CanActivateFn = () => {


  return checkRole(
    'ADMIN',
    'admin'
  );

};
