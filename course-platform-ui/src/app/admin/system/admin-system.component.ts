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
  AdminSystemComponent,
  AdminSystemService,
  AdminSystemStatus
} from './admin-system.service';


@Component({
  selector: 'app-admin-system',

  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './admin-system.component.html',

  styleUrl:
    './admin-system.component.css'
})
export class AdminSystemComponentPage
  implements OnInit {


  // =========================================================
  // SYSTEM DATA
  // =========================================================

  system:
    AdminSystemStatus = {

    overallStatus: 'UP',

    serverTime: '',

    uptimeSeconds: 0,

    javaVersion: '',

    usedMemoryMb: 0,

    maxMemoryMb: 0,

    flywayVersion: '',

    components: []
  };


  // =========================================================
  // STATE
  // =========================================================

  isLoading =
    true;


  errorMessage =
    '';


  constructor(

    private adminSystemService:
    AdminSystemService,

    private router:
    Router

  ) {
  }


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {


    this.loadSystemStatus();
  }


  // =========================================================
  // LOAD SYSTEM
  // =========================================================

  loadSystemStatus(): void {


    this.isLoading =
      true;


    this.errorMessage =
      '';


    this.adminSystemService
      .getSystemStatus()
      .subscribe({


        next: (
          system
        ) => {


          this.system =
            system;


          this.isLoading =
            false;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin system hatası:',
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
            'Sistem durumu alınamadı.';
        }

      });
  }


  // =========================================================
  // UPTIME
  // =========================================================

  formatUptime(
    totalSeconds:
      number
      | null
      | undefined
  ): string {


    const safeSeconds =

      Math.max(
        0,
        totalSeconds ?? 0
      );


    const days =

      Math.floor(
        safeSeconds
        /
        86400
      );


    const hours =

      Math.floor(
        (
          safeSeconds % 86400
        )
        /
        3600
      );


    const minutes =

      Math.floor(
        (
          safeSeconds % 3600
        )
        /
        60
      );


    const seconds =

      Math.floor(
        safeSeconds % 60
      );


    const parts:
      string[] = [];


    if (
      days > 0
    ) {


      parts.push(
        `${days}g`
      );
    }


    if (
      hours > 0
      ||
      days > 0
    ) {


      parts.push(
        `${hours}sa`
      );
    }


    if (
      minutes > 0
      ||
      hours > 0
      ||
      days > 0
    ) {


      parts.push(
        `${minutes}dk`
      );
    }


    parts.push(
      `${seconds}sn`
    );


    return parts.join(
      ' '
    );
  }


  // =========================================================
  // MEMORY PERCENT
  // =========================================================

  getMemoryPercentage(): number {


    if (
      !this.system.maxMemoryMb
      ||
      this.system.maxMemoryMb <= 0
    ) {


      return 0;
    }


    const percentage =

      (
        this.system.usedMemoryMb
        /
        this.system.maxMemoryMb
      )
      *
      100;


    return Math.min(

      100,

      Math.max(
        0,
        percentage
      )
    );
  }


  // =========================================================
  // MEMORY PERCENT LABEL
  // =========================================================

  getMemoryPercentageLabel(): string {


    return `${this.getMemoryPercentage().toFixed(1)}%`;
  }


  // =========================================================
  // COMPONENT STATUS
  // =========================================================

  isComponentUp(
    component:
    AdminSystemComponent
  ): boolean {


    return component.status === 'UP';
  }


  // =========================================================
  // OVERALL STATUS LABEL
  // =========================================================

  getOverallStatusLabel(): string {


    if (
      this.system.overallStatus === 'UP'
    ) {


      return 'Tüm Sistemler Çalışıyor';
    }


    if (
      this.system.overallStatus === 'DEGRADED'
    ) {


      return 'Sistem Kısmen Çalışıyor';
    }


    return this.system.overallStatus;
  }


  // =========================================================
  // SERVER TIME
  // =========================================================

  getServerTime(): string {


    if (
      !this.system.serverTime
    ) {


      return '-';
    }


    const date =
      new Date(
        this.system.serverTime
      );


    if (
      Number.isNaN(
        date.getTime()
      )
    ) {


      return this.system.serverTime;
    }


    return date.toLocaleString(
      'tr-TR'
    );
  }


  // =========================================================
  // REFRESH
  // =========================================================

  refresh(): void {


    this.loadSystemStatus();
  }
}
