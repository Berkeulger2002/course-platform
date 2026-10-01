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
  AdminService,
  TeacherRegistrationRequest
} from '../../services/admin.service';

import {
  AuthService
} from '../../services/auth.service';


@Component({
  selector: 'app-admin-dashboard',

  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './admin-dashboard.component.html',

  styleUrl:
    './admin-dashboard.component.css'
})
export class AdminDashboardComponent
  implements OnInit {


  // =========================================================
  // ACTIVE TEACHER REQUESTS
  //
  // PENDING
  // CODE_SENT
  // =========================================================

  teacherRequests:
    TeacherRegistrationRequest[] = [];


  pendingCount =
    0;


  codeSentCount =
    0;


  isLoading =
    true;


  errorMessage =
    '';


  successMessage =
    '';


  processingRequestId:
    number | null =
    null;


  processingAction:
    'approve'
    | 'reject'
    | 'resend'
    | null =
    null;


  constructor(

    private adminService:
    AdminService,

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


    this.loadTeacherRequests();
  }


  // =========================================================
  // LOAD ACTIVE TEACHER REQUESTS
  //
  // Backend'den tüm kayıtları alıyoruz.
  //
  // Panelde yalnızca:
  //
  // PENDING
  // CODE_SENT
  //
  // gösteriyoruz.
  // =========================================================

  loadTeacherRequests(): void {


    this.isLoading =
      true;


    this.errorMessage =
      '';


    this.adminService
      .getTeacherRequests()
      .subscribe({


        next: (
          requests
        ) => {


          this.teacherRequests =

            requests.filter(

              request =>

                request.status === 'PENDING'
                ||
                request.status === 'CODE_SENT'

            );


          this.pendingCount =

            this.teacherRequests.filter(

              request =>
                request.status === 'PENDING'

            ).length;


          this.codeSentCount =

            this.teacherRequests.filter(

              request =>
                request.status === 'CODE_SENT'

            ).length;


          this.isLoading =
            false;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin teacher request hatası:',
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
            'Öğretmen başvuruları yüklenemedi.';
        }

      });
  }


  // =========================================================
  // APPROVE
  // =========================================================

  approveRequest(
    request: TeacherRegistrationRequest
  ): void {


    if (
      this.processingRequestId !== null
    ) {

      return;
    }


    if (
      request.status !== 'PENDING'
    ) {

      return;
    }


    const confirmed =
      window.confirm(

        `${request.name} adlı öğretmen adayının başvurusunu onaylayıp doğrulama kodu göndermek istiyor musunuz?`

      );


    if (
      !confirmed
    ) {

      return;
    }


    this.clearMessages();


    this.processingRequestId =
      request.id;


    this.processingAction =
      'approve';


    this.adminService
      .approveTeacherRequest(
        request.id
      )
      .subscribe({


        next: () => {


          this.clearProcessing();


          this.successMessage =
            'Başvuru onaylandı ve doğrulama kodu öğretmen adayının e-posta adresine gönderildi.';


          this.loadTeacherRequests();
        },


        error: (
          error
        ) => {


          console.error(
            'Teacher approve hatası:',
            error
          );


          this.clearProcessing();


          if (
            this.handleCommonError(
              error
            )
          ) {

            return;
          }


          if (
            error?.status === 502
          ) {


            this.errorMessage =
              'Başvuru onaylanamadı çünkü doğrulama e-postası gönderilemedi.';


            return;
          }


          if (
            error?.status === 409
          ) {


            this.errorMessage =
              'Bu başvuru artık onaylanabilir durumda değil.';


            this.loadTeacherRequests();


            return;
          }


          this.errorMessage =
            'Öğretmen başvurusu onaylanamadı.';
        }

      });
  }


  // =========================================================
  // RESEND VERIFICATION CODE
  // =========================================================

  resendCode(
    request: TeacherRegistrationRequest
  ): void {


    if (
      this.processingRequestId !== null
    ) {

      return;
    }


    if (
      request.status !== 'CODE_SENT'
    ) {

      return;
    }


    const confirmed =
      window.confirm(

        `${request.name} adlı öğretmen adayına yeni bir doğrulama kodu göndermek istiyor musunuz?\n\nEski doğrulama kodu geçersiz olacaktır.`

      );


    if (
      !confirmed
    ) {

      return;
    }


    this.clearMessages();


    this.processingRequestId =
      request.id;


    this.processingAction =
      'resend';


    this.adminService
      .resendTeacherVerificationCode(
        request.id
      )
      .subscribe({


        next: () => {


          this.clearProcessing();


          this.successMessage =
            'Yeni doğrulama kodu öğretmen adayının e-posta adresine gönderildi. Eski kod artık geçersizdir.';


          this.loadTeacherRequests();
        },


        error: (
          error
        ) => {


          console.error(
            'Teacher resend code hatası:',
            error
          );


          this.clearProcessing();


          if (
            this.handleCommonError(
              error
            )
          ) {

            return;
          }


          if (
            error?.status === 502
          ) {


            this.errorMessage =
              'Yeni doğrulama kodu e-posta ile gönderilemedi.';


            return;
          }


          if (
            error?.status === 409
          ) {


            this.errorMessage =
              'Bu başvuruya artık yeni doğrulama kodu gönderilemez.';


            this.loadTeacherRequests();


            return;
          }


          if (
            error?.status === 404
          ) {


            this.errorMessage =
              'Öğretmenlik başvurusu bulunamadı.';


            this.loadTeacherRequests();


            return;
          }


          this.errorMessage =
            'Yeni doğrulama kodu gönderilemedi.';
        }

      });
  }


  // =========================================================
  // REJECT
  // =========================================================

  rejectRequest(
    request: TeacherRegistrationRequest
  ): void {


    if (
      this.processingRequestId !== null
    ) {

      return;
    }


    if (
      request.status !== 'PENDING'
    ) {

      return;
    }


    const confirmed =
      window.confirm(

        `${request.name} adlı öğretmen adayının başvurusunu reddetmek istiyor musunuz?`

      );


    if (
      !confirmed
    ) {

      return;
    }


    this.clearMessages();


    this.processingRequestId =
      request.id;


    this.processingAction =
      'reject';


    this.adminService
      .rejectTeacherRequest(
        request.id
      )
      .subscribe({


        next: () => {


          this.clearProcessing();


          this.successMessage =
            'Öğretmen başvurusu reddedildi.';


          this.loadTeacherRequests();
        },


        error: (
          error
        ) => {


          console.error(
            'Teacher reject hatası:',
            error
          );


          this.clearProcessing();


          if (
            this.handleCommonError(
              error
            )
          ) {

            return;
          }


          if (
            error?.status === 409
          ) {


            this.errorMessage =
              'Bu başvuru artık reddedilebilir durumda değil.';


            this.loadTeacherRequests();


            return;
          }


          this.errorMessage =
            'Öğretmen başvurusu reddedilemedi.';
        }

      });
  }


  // =========================================================
  // STATUS LABEL
  // =========================================================

  getStatusLabel(
    status: string
  ): string {


    if (
      status === 'PENDING'
    ) {

      return 'Onay Bekliyor';
    }


    if (
      status === 'CODE_SENT'
    ) {

      return 'Kod Gönderildi';
    }


    return status;
  }


  // =========================================================
  // PROCESSING CHECK
  // =========================================================

  isProcessing(
    requestId: number
  ): boolean {


    return this.processingRequestId ===
      requestId;
  }


  // =========================================================
  // CLEAR PROCESSING
  // =========================================================

  private clearProcessing(): void {


    this.processingRequestId =
      null;


    this.processingAction =
      null;
  }


  // =========================================================
  // CLEAR MESSAGES
  // =========================================================

  private clearMessages(): void {


    this.errorMessage =
      '';


    this.successMessage =
      '';
  }


  // =========================================================
  // COMMON HTTP ERRORS
  // =========================================================

  private handleCommonError(
    error: any
  ): boolean {


    if (
      error?.status === 401
    ) {


      this.router.navigate([
        '/admin/login'
      ]);


      return true;
    }


    if (
      error?.status === 403
    ) {


      this.errorMessage =
        'Bu işlem için ADMIN yetkisi gerekiyor.';


      return true;
    }


    return false;
  }


  // =========================================================
  // LOGOUT
  // =========================================================

  logout(): void {


    this.authService
      .logout()
      .subscribe({


        next: () => {


          localStorage.removeItem(
            'currentUser'
          );


          this.router.navigate([
            '/admin/login'
          ]);
        },


        error: () => {


          localStorage.removeItem(
            'currentUser'
          );


          this.router.navigate([
            '/admin/login'
          ]);
        }

      });
  }
}
