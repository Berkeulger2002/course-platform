import {
  Component,
  OnInit
} from '@angular/core';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  AuthService,
  AuthUser,
  LoginRequest,
  RegisterRequest
} from '../../services/auth.service';


@Component({
  selector: 'app-auth',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './auth.component.html',

  styleUrl:
    './auth.component.css'
})
export class AuthComponent
  implements OnInit {


  role:
    'student' | 'teacher' =
    'student';


  isLoginMode:
    boolean = false;


  name:
    string = '';


  email:
    string = '';


  password:
    string = '';


  constructor(

    private route:
    ActivatedRoute,

    private authService:
    AuthService,

    private router:
    Router

  ) {
  }


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {


    this.route
      .queryParams
      .subscribe(params => {


        const requestedRole =
          params['role'];


        this.role =
          requestedRole === 'teacher'
            ?
            'teacher'
            :
            'student';


        // =====================================================
        // TEACHER
        //
        // Öğretmen için public registration yoktur.
        //
        // Bu nedenle öğretmen ekranı her zaman login
        // modunda açılır.
        // =====================================================

        if (
          this.role === 'teacher'
        ) {


          this.isLoginMode =
            true;
        }

      });
  }


  // =========================================================
  // LOGIN / REGISTER MODE
  // =========================================================

  toggleMode(): void {


    // =======================================================
    // TEACHER
    //
    // Öğretmen public kayıt olamaz.
    // =======================================================

    if (
      this.role === 'teacher'
    ) {


      this.isLoginMode =
        true;


      return;
    }


    this.isLoginMode =
      !this.isLoginMode;
  }


  // =========================================================
  // SUBMIT
  // =========================================================

  onSubmit(): void {


    if (
      this.isLoginMode
    ) {


      this.login();

      return;
    }


    // =======================================================
    // EXTRA FRONTEND GUARD
    //
    // Backend zaten öğretmen kaydını kabul etmez.
    //
    // Frontend tarafında da yanlışlıkla register
    // çalıştırılmasını engelliyoruz.
    // =======================================================

    if (
      this.role === 'teacher'
    ) {


      alert(
        'Öğretmen hesapları public kayıt ile oluşturulamaz.'
      );


      this.isLoginMode =
        true;


      return;
    }


    this.register();
  }


  // =========================================================
  // LOGIN
  // =========================================================

  private login(): void {


    const credentials:
      LoginRequest = {

      email:
      this.email,

      password:
      this.password

    };


    this.authService
      .login(
        credentials
      )
      .subscribe({


        next: (
          user: AuthUser
        ) => {


          // =================================================
          // UI CACHE
          // =================================================
          //
          // Authentication kaynağı değildir.
          //
          // Gerçek authentication JWT HttpOnly Cookie ile
          // yapılmaktadır.
          // =================================================

          localStorage.setItem(

            'currentUser',

            JSON.stringify(
              user
            )

          );


          alert(
            'Giriş başarılı! Hoş geldin '
            + user.name
          );


          if (
            user.role ===
            'TEACHER'
          ) {


            this.router.navigate([

              '/teacher/dashboard'

            ]);


            return;
          }


          this.router.navigate([

            '/student/dashboard'

          ]);
        },


        error: (
          error
        ) => {


          console.error(
            'Login hatası:',
            error
          );


          if (
            typeof error?.error
            ===
            'string'
            &&
            error.error
          ) {


            alert(
              error.error
            );


            return;
          }


          alert(
            'Giriş yapılamadı.'
          );
        }

      });
  }


  // =========================================================
  // REGISTER
  //
  // Public registration yalnızca STUDENT.
  // =========================================================

  private register(): void {


    const userData:
      RegisterRequest = {

      name:
      this.name,

      email:
      this.email,

      password:
      this.password

    };


    this.authService
      .register(
        userData
      )
      .subscribe({


        next: (
          response
        ) => {


          alert(
            response
          );


          this.isLoginMode =
            true;
        },


        error: (
          error
        ) => {


          console.error(
            'Kayıt hatası:',
            error
          );


          if (
            typeof error?.error
            ===
            'string'
            &&
            error.error
          ) {


            alert(
              error.error
            );


            return;
          }


          alert(
            'Kayıt başarısız oldu.'
          );
        }

      });
  }
}
