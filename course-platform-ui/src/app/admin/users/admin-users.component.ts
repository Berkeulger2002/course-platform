import {
  Component,
  OnInit
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
  AdminService,
  AdminUser,
  AdminUserSummary
} from '../../services/admin.service';

import {
  AdminUserMailService
} from './admin-user-mail.service';


@Component({
  selector: 'app-admin-users',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './admin-users.component.html',

  styleUrl:
    './admin-users.component.css'
})
export class AdminUsersComponent
  implements OnInit {


  // =========================================================
  // DATA
  // =========================================================

  users:
    AdminUser[] = [];


  filteredUsers:
    AdminUser[] = [];


  summary:
    AdminUserSummary = {

    totalUsers: 0,

    students: 0,

    teachers: 0,

    admins: 0
  };


  // =========================================================
  // FILTERS
  // =========================================================

  searchTerm =
    '';


  selectedRole:
    'ALL'
    | 'STUDENT'
    | 'TEACHER'
    | 'ADMIN' =
    'ALL';


  // =========================================================
  // PAGE STATE
  // =========================================================

  isLoading =
    true;


  errorMessage =
    '';


  // =========================================================
  // MAIL STATE
  // =========================================================

  selectedMailUser:
    AdminUser | null =
    null;


  mailSubject =
    '';


  mailMessage =
    '';


  isSendingMail =
    false;


  mailErrorMessage =
    '';


  mailSuccessMessage =
    '';


  constructor(

    private adminService:
    AdminService,

    private adminUserMailService:
    AdminUserMailService,

    private router:
    Router

  ) {
  }


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {


    this.loadUsers();


    this.loadSummary();
  }


  // =========================================================
  // LOAD USERS
  // =========================================================

  loadUsers(): void {


    this.isLoading =
      true;


    this.errorMessage =
      '';


    this.adminService
      .getUsers()
      .subscribe({


        next: (
          users
        ) => {


          this.users =
            users;


          this.applyFilters();


          this.isLoading =
            false;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin users hatası:',
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
            'Kullanıcılar yüklenemedi.';
        }

      });
  }


  // =========================================================
  // LOAD SUMMARY
  // =========================================================

  loadSummary(): void {


    this.adminService
      .getUserSummary()
      .subscribe({


        next: (
          summary
        ) => {


          this.summary =
            summary;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin user summary hatası:',
            error
          );
        }

      });
  }


  // =========================================================
  // APPLY FILTERS
  // =========================================================

  applyFilters(): void {


    const normalizedSearch =
      this.searchTerm
        .trim()
        .toLowerCase();


    this.filteredUsers =

      this.users.filter(

        user => {


          const roleMatches =

            this.selectedRole === 'ALL'
            ||
            user.role === this.selectedRole;


          const searchMatches =

            !normalizedSearch

            ||

            user.name
              .toLowerCase()
              .includes(
                normalizedSearch
              )

            ||

            user.email
              .toLowerCase()
              .includes(
                normalizedSearch
              )

            ||

            String(
              user.id
            )
              .includes(
                normalizedSearch
              );


          return roleMatches
            &&
            searchMatches;
        }

      );
  }


  // =========================================================
  // ROLE LABEL
  // =========================================================

  getRoleLabel(
    role: string
  ): string {


    switch (
      role
      ) {


      case 'STUDENT':

        return 'Öğrenci';


      case 'TEACHER':

        return 'Öğretmen';


      case 'ADMIN':

        return 'Admin';


      default:

        return role;
    }
  }


  // =========================================================
  // CAN SEND EMAIL
  // =========================================================

  canSendEmail(
    user: AdminUser
  ): boolean {


    return user.role === 'STUDENT'
      ||
      user.role === 'TEACHER';
  }


  // =========================================================
  // OPEN MAIL MODAL
  // =========================================================

  openMailModal(
    user: AdminUser
  ): void {


    if (
      !this.canSendEmail(
        user
      )
    ) {


      return;
    }


    this.selectedMailUser =
      user;


    this.mailSubject =
      '';


    this.mailMessage =
      '';


    this.mailErrorMessage =
      '';


    this.mailSuccessMessage =
      '';
  }


  // =========================================================
  // CLOSE MAIL MODAL
  // =========================================================

  closeMailModal(): void {


    if (
      this.isSendingMail
    ) {


      return;
    }


    this.selectedMailUser =
      null;


    this.mailSubject =
      '';


    this.mailMessage =
      '';


    this.mailErrorMessage =
      '';
  }


  // =========================================================
  // SEND EMAIL
  // =========================================================

  sendMail(): void {


    if (
      !this.selectedMailUser
    ) {


      return;
    }


    if (
      !this.canSendEmail(
        this.selectedMailUser
      )
    ) {


      return;
    }


    const subject =
      this.mailSubject.trim();


    const message =
      this.mailMessage.trim();


    if (
      !subject
    ) {


      this.mailErrorMessage =
        'Mail konusu zorunludur.';


      return;
    }


    if (
      !message
    ) {


      this.mailErrorMessage =
        'Mail mesajı zorunludur.';


      return;
    }


    if (
      subject.length > 150
    ) {


      this.mailErrorMessage =
        'Mail konusu en fazla 150 karakter olabilir.';


      return;
    }


    if (
      message.length > 5000
    ) {


      this.mailErrorMessage =
        'Mail mesajı en fazla 5000 karakter olabilir.';


      return;
    }


    const userId =
      this.selectedMailUser.id;


    const userName =
      this.selectedMailUser.name;


    this.isSendingMail =
      true;


    this.mailErrorMessage =
      '';


    this.adminUserMailService
      .sendEmail(

        userId,

        {
          subject,
          message
        }

      )
      .subscribe({


        next: () => {


          this.isSendingMail =
            false;


          this.selectedMailUser =
            null;


          this.mailSubject =
            '';


          this.mailMessage =
            '';


          this.mailSuccessMessage =

            `${userName} adlı kullanıcıya e-posta başarıyla gönderildi.`;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin mail gönderme hatası:',
            error
          );


          this.isSendingMail =
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


            this.mailErrorMessage =
              'Bu işlem için ADMIN yetkisi gerekiyor.';


            return;
          }


          if (
            error?.status === 404
          ) {


            this.mailErrorMessage =
              'Kullanıcı bulunamadı.';


            return;
          }


          if (
            error?.status === 400
          ) {


            this.mailErrorMessage =
              'Mail bilgileri geçersiz.';


            return;
          }


          if (
            error?.status === 502
          ) {


            this.mailErrorMessage =
              'E-posta servisine ulaşılamadı veya mail gönderilemedi.';


            return;
          }


          this.mailErrorMessage =
            'E-posta gönderilemedi.';
        }

      });
  }


  // =========================================================
  // REFRESH
  // =========================================================

  refresh(): void {


    this.loadUsers();


    this.loadSummary();
  }
}
