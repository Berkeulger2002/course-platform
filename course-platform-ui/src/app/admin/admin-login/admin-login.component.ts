import {
  Component
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
  AuthService,
  AuthUser,
  LoginRequest
} from '../../services/auth.service';


@Component({
  selector: 'app-admin-login',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './admin-login.component.html',

  styleUrl:
    './admin-login.component.css'
})
export class AdminLoginComponent {


  email =
    '';


  password =
    '';


  isSubmitting =
    false;


  constructor(

    private authService:
    AuthService,

    private router:
    Router

  ) {
  }


  // =========================================================
  // LOGIN
  // =========================================================

  login(): void {


    if (
      this.isSubmitting
    ) {

      return;
    }


    const credentials:
      LoginRequest = {

      email:
      this.email,

      password:
      this.password

    };


    this.isSubmitting =
      true;


    this.authService
      .login(
        credentials
      )
      .subscribe({


        next: (
          user: AuthUser
        ) => {


          this.isSubmitting =
            false;


          // =================================================
          // ADMIN KONTROLÜ
          // =================================================

          if (
            user.role !== 'ADMIN'
          ) {


            // Admin olmayan kullanıcının oluşturulan
            // JWT cookie'sini de temizliyoruz.

            this.authService
              .logout()
              .subscribe({
                next: () => {},
                error: () => {}
              });


            localStorage.removeItem(
              'currentUser'
            );


            alert(
              'Bu giriş ekranı yalnızca yönetici hesabı içindir.'
            );


            return;
          }


          // =================================================
          // UI CACHE
          // =================================================

          localStorage.setItem(

            'currentUser',

            JSON.stringify(
              user
            )

          );


          this.router.navigate([

            '/admin/dashboard'

          ]);
        },


        error: (
          error
        ) => {


          this.isSubmitting =
            false;


          console.error(
            'Admin login hatası:',
            error
          );


          alert(
            'Admin girişi başarısız. E-posta veya şifrenizi kontrol edin.'
          );
        }

      });
  }
}
