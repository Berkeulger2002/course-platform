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


function checkRole(

  expectedRole:
    'STUDENT' | 'TEACHER',

  loginRole:
    'student' | 'teacher'

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


        if (
          user.role ===
          'TEACHER'
        ) {


          return router.createUrlTree([

            '/teacher/dashboard'

          ]);
        }


        if (
          user.role ===
          'STUDENT'
        ) {


          return router.createUrlTree([

            '/student/dashboard'

          ]);
        }


        return router.createUrlTree(

          ['/auth'],

          {
            queryParams: {
              role:
              loginRole
            }
          }

        );
      }),


      catchError(() => {


        localStorage.removeItem(
          'currentUser'
        );


        return of(

          router.createUrlTree(

            ['/auth'],

            {
              queryParams: {
                role:
                loginRole
              }
            }

          )

        );
      })

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
