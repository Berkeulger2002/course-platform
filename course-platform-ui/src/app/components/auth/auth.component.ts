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
  RegisterRequest,
  TeacherRegistrationCreateRequest,
  TeacherRegistrationCompleteRequest
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


  // =========================================================
  // ROLE
  // =========================================================

  role:
    'student' | 'teacher' =
    'student';


  // =========================================================
  // MODES
  // =========================================================

  isLoginMode:
    boolean = false;


  isTeacherApplicationMode:
    boolean = false;


  isTeacherCompletionMode:
    boolean = false;


  isSubmitting:
    boolean = false;


  // =========================================================
  // FORM
  // =========================================================

  name:
    string = '';


  email:
    string = '';


  password:
    string = '';


  confirmPassword:
    string = '';


  verificationCode:
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
        // =====================================================

        if (
          this.role === 'teacher'
        ) {


          this.isLoginMode =
            true;


          this.isTeacherApplicationMode =
            false;


          this.isTeacherCompletionMode =
            false;


          this.clearSensitiveFields();


          return;
        }


        // =====================================================
        // STUDENT
        // =====================================================

        this.isLoginMode =
          false;


        this.isTeacherApplicationMode =
          false;


        this.isTeacherCompletionMode =
          false;


        this.clearSensitiveFields();
      });
  }


  // =========================================================
  // STUDENT LOGIN / REGISTER SWITCH
  // =========================================================

  toggleMode(): void {


    if (
      this.role !== 'student'
    ) {

      return;
    }


    this.isLoginMode =
      !this.isLoginMode;


    this.clearSensitiveFields();
  }


  // =========================================================
  // OPEN TEACHER APPLICATION
  // =========================================================

  openTeacherApplication(): void {


    if (
      this.role !== 'teacher'
    ) {

      return;
    }


    this.isTeacherApplicationMode =
      true;


    this.isTeacherCompletionMode =
      false;


    this.isLoginMode =
      true;


    this.name =
      '';


    this.clearSensitiveFields();
  }


  // =========================================================
  // OPEN TEACHER COMPLETION
  // =========================================================

  openTeacherCompletion(): void {


    if (
      this.role !== 'teacher'
    ) {

      return;
    }


    this.isTeacherApplicationMode =
      false;


    this.isTeacherCompletionMode =
      true;


    this.isLoginMode =
      true;


    this.name =
      '';


    this.clearSensitiveFields();
  }


  // =========================================================
  // RETURN TO TEACHER LOGIN
  // =========================================================

  returnToTeacherLogin(): void {


    this.isTeacherApplicationMode =
      false;


    this.isTeacherCompletionMode =
      false;


    this.isLoginMode =
      true;


    this.name =
      '';


    this.clearSensitiveFields();
  }


  // =========================================================
  // SUBMIT
  // =========================================================

  onSubmit(): void {


    if (
      this.isSubmitting
    ) {

      return;
    }


    // =======================================================
    // TEACHER APPLICATION
    // =======================================================

    if (
      this.role === 'teacher'
      &&
      this.isTeacherApplicationMode
    ) {


      this.submitTeacherApplication();

      return;
    }


    // =======================================================
    // TEACHER REGISTRATION COMPLETE
    // =======================================================

    if (
      this.role === 'teacher'
      &&
      this.isTeacherCompletionMode
    ) {


      this.completeTeacherRegistration();

      return;
    }


    // =======================================================
    // LOGIN
    // =======================================================

    if (
      this.isLoginMode
    ) {


      this.login();

      return;
    }


    // =======================================================
    // STUDENT REGISTER
    // =======================================================

    if (
      this.role === 'student'
    ) {


      this.register();
    }
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
          // UI CACHE
          //
          // Gerçek authentication JWT HttpOnly cookie'dedir.
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


          // =================================================
          // ADMIN
          // =================================================

          if (
            user.role === 'ADMIN'
          ) {


            this.router.navigate([

              '/admin/dashboard'

            ]);


            return;
          }


          // =================================================
          // TEACHER
          // =================================================

          if (
            user.role === 'TEACHER'
          ) {


            this.router.navigate([

              '/teacher/dashboard'

            ]);


            return;
          }


          // =================================================
          // STUDENT
          // =================================================

          this.router.navigate([

            '/student/dashboard'

          ]);
        },


        error: (
          error
        ) => {


          this.isSubmitting =
            false;


          console.error(
            'Login hatası:',
            error
          );


          alert(
            this.getErrorMessage(
              error,
              'Giriş yapılamadı. E-posta veya şifrenizi kontrol edin.'
            )
          );
        }

      });
  }


  // =========================================================
  // STUDENT REGISTER
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


    this.isSubmitting =
      true;


    this.authService
      .register(
        userData
      )
      .subscribe({


        next: (
          response
        ) => {


          this.isSubmitting =
            false;


          alert(
            response
          );


          this.isLoginMode =
            true;


          this.clearSensitiveFields();
        },


        error: (
          error
        ) => {


          this.isSubmitting =
            false;


          console.error(
            'Kayıt hatası:',
            error
          );


          alert(
            this.getErrorMessage(
              error,
              'Kayıt başarısız oldu.'
            )
          );
        }

      });
  }


  // =========================================================
  // TEACHER APPLICATION
  // =========================================================

  private submitTeacherApplication(): void {


    const request:
      TeacherRegistrationCreateRequest = {

      name:
      this.name,

      email:
      this.email

    };


    this.isSubmitting =
      true;


    this.authService
      .requestTeacherRegistration(
        request
      )
      .subscribe({


        next: (
          response
        ) => {


          this.isSubmitting =
            false;


          alert(
            'Öğretmenlik başvurunuz başarıyla alındı.\n\n'
            +
            'Başvuru numaranız: '
            +
            response.id
            +
            '\n\n'
            +
            'Başvurunuz yönetici tarafından incelendikten sonra '
            +
            'e-posta adresinize doğrulama kodu gönderilecektir.'
          );


          this.isTeacherApplicationMode =
            false;


          this.isTeacherCompletionMode =
            false;


          this.isLoginMode =
            true;


          this.name =
            '';


          this.email =
            '';


          this.clearSensitiveFields();
        },


        error: (
          error
        ) => {


          this.isSubmitting =
            false;


          console.error(
            'Öğretmen başvurusu hatası:',
            error
          );


          alert(
            this.getErrorMessage(
              error,
              'Öğretmenlik başvurusu gönderilemedi.'
            )
          );
        }

      });
  }


  // =========================================================
  // COMPLETE TEACHER REGISTRATION
  // =========================================================

  private completeTeacherRegistration(): void {


    if (
      this.password !==
      this.confirmPassword
    ) {


      alert(
        'Şifre ve şifre tekrarı aynı olmalıdır.'
      );


      return;
    }


    const request:
      TeacherRegistrationCompleteRequest = {

      email:
      this.email,

      verificationCode:
      this.verificationCode,

      password:
      this.password,

      confirmPassword:
      this.confirmPassword

    };


    this.isSubmitting =
      true;


    this.authService
      .completeTeacherRegistration(
        request
      )
      .subscribe({


        next: (
          response
        ) => {


          this.isSubmitting =
            false;


          alert(
            'Öğretmen hesabınız başarıyla oluşturuldu.\n\n'
            +
            'Başvuru durumu: '
            +
            response.status
            +
            '\n\n'
            +
            'Artık e-posta adresiniz ve belirlediğiniz şifre ile '
            +
            'öğretmen hesabınıza giriş yapabilirsiniz.'
          );


          // =================================================
          // LOGIN MODE
          //
          // Email'i bırakıyoruz.
          // Kullanıcı sadece şifresini tekrar yazarak
          // giriş yapabilir.
          // =================================================

          this.isTeacherCompletionMode =
            false;


          this.isTeacherApplicationMode =
            false;


          this.isLoginMode =
            true;


          this.verificationCode =
            '';


          this.password =
            '';


          this.confirmPassword =
            '';
        },


        error: (
          error
        ) => {


          this.isSubmitting =
            false;


          console.error(
            'Öğretmen hesap tamamlama hatası:',
            error
          );


          alert(
            this.getErrorMessage(
              error,
              'Öğretmen hesabı oluşturulamadı.'
            )
          );
        }

      });
  }


  // =========================================================
  // CLEAR SENSITIVE FIELDS
  // =========================================================

  private clearSensitiveFields(): void {


    this.password =
      '';


    this.confirmPassword =
      '';


    this.verificationCode =
      '';
  }


  // =========================================================
  // ERROR MESSAGE
  // =========================================================

  private getErrorMessage(
    error: any,
    fallback: string
  ): string {


    if (
      typeof error?.error === 'string'
      &&
      error.error.trim()
    ) {


      return error.error;
    }


    if (
      typeof error?.error?.detail === 'string'
      &&
      error.error.detail.trim()
    ) {


      return error.error.detail;
    }


    if (
      typeof error?.error?.message === 'string'
      &&
      error.error.message.trim()
    ) {


      return error.error.message;
    }


    return fallback;
  }
}
