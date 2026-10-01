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
  AdminSessionSummary,
  AdminSessionsService,
  AdminUserSession
} from './admin-sessions.service';


@Component({
  selector: 'app-admin-sessions',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './admin-sessions.component.html',

  styleUrl:
    './admin-sessions.component.css'
})
export class AdminSessionsComponent
  implements OnInit {


  sessions:
    AdminUserSession[] = [];


  filteredSessions:
    AdminUserSession[] = [];


  summary:
    AdminSessionSummary = {

    totalSessions: 0,

    onlineNow: 0,

    onlineStudents: 0,

    onlineTeachers: 0,

    todayLogins: 0,

    averageActiveSeconds: 0
  };


  searchTerm =
    '';


  roleFilter =
    'ALL';


  statusFilter =
    'ALL';


  isLoading =
    true;


  errorMessage =
    '';


  constructor(

    private sessionsService:
    AdminSessionsService,

    private router:
    Router

  ) {
  }


  ngOnInit(): void {


    this.loadData();
  }


  loadData(): void {


    this.loadSessions();

    this.loadSummary();
  }


  loadSessions(): void {


    this.isLoading =
      true;


    this.errorMessage =
      '';


    this.sessionsService
      .getSessions()
      .subscribe({


        next: (
          sessions
        ) => {


          this.sessions =
            sessions;


          this.applyFilter();


          this.isLoading =
            false;
        },


        error: (
          error
        ) => {


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
            'Oturum kayıtları yüklenemedi.';
        }

      });
  }


  loadSummary(): void {


    this.sessionsService
      .getSummary()
      .subscribe({


        next: (
          summary
        ) => {


          this.summary =
            summary;
        }

      });
  }


  applyFilter(): void {


    const search =

      this.searchTerm
        .trim()
        .toLowerCase();


    this.filteredSessions =

      this.sessions.filter(

        session => {


          const matchesSearch =

            !search

            ||

            session.userName
              ?.toLowerCase()
              .includes(
                search
              )

            ||

            session.userEmail
              ?.toLowerCase()
              .includes(
                search
              )

            ||

            String(
              session.userId
            ).includes(
              search
            );


          const matchesRole =

            this.roleFilter ===
            'ALL'

            ||

            session.role ===
            this.roleFilter;


          const matchesStatus =

            this.statusFilter ===
            'ALL'

            ||

            session.status ===
            this.statusFilter;


          return (

            matchesSearch

            &&

            matchesRole

            &&

            matchesStatus
          );
        }

      );
  }


  formatDuration(
    seconds:
      number
      | null
      | undefined
  ): string {


    const safe =
      Math.max(
        0,
        seconds ?? 0
      );


    const hours =
      Math.floor(
        safe / 3600
      );


    const minutes =
      Math.floor(
        (
          safe % 3600
        )
        /
        60
      );


    const secs =
      Math.floor(
        safe % 60
      );


    if (
      hours > 0
    ) {


      return `${hours} sa ${minutes} dk`;
    }


    if (
      minutes > 0
    ) {


      return `${minutes} dk ${secs} sn`;
    }


    return `${secs} sn`;
  }


  formatDate(
    value:
      string
      | null
      | undefined
  ): string {


    if (
      !value
    ) {


      return '—';
    }


    const date =
      new Date(
        value
      );


    if (
      Number.isNaN(
        date.getTime()
      )
    ) {


      return value;
    }


    return date.toLocaleString(
      'tr-TR'
    );
  }


  getRoleLabel(
    role: string
  ): string {


    if (
      role === 'STUDENT'
    ) {


      return 'Öğrenci';
    }


    if (
      role === 'TEACHER'
    ) {


      return 'Öğretmen';
    }


    return role;
  }


  getStatusLabel(
    status: string
  ): string {


    switch (
      status
      ) {


      case 'ONLINE':
        return 'Online';


      case 'LOGGED_OUT':
        return 'Çıkış Yaptı';


      case 'EXPIRED':
        return 'Süresi Doldu';


      case 'OFFLINE':
        return 'Offline';


      default:
        return status;
    }
  }


  refresh(): void {


    this.loadData();
  }
}
