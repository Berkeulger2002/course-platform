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
  AppNotification,
  NotificationService
} from '../../services/notification.service';


@Component({
  selector: 'app-notifications',

  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl: './notifications.component.html',

  styleUrl: './notifications.component.css'
})
export class NotificationsComponent
  implements OnInit {

  currentTeacher: any;


  notifications:
    AppNotification[] = [];


  unreadCount = 0;


  isLoading = true;


  /*
   * ALL
   * UNREAD
   * READ
   */
  selectedFilter =
    'ALL';


  constructor(
    private notificationService:
    NotificationService,

    private router:
    Router
  ) {}


  ngOnInit(): void {

    const userStr =
      localStorage.getItem(
        'currentUser'
      );


    if (!userStr) {

      this.router.navigate([
        '/auth'
      ]);

      return;
    }


    this.currentTeacher =
      JSON.parse(userStr);


    this.loadNotifications();
  }


  // =========================================================
  // BİLDİRİMLERİ GETİR
  // =========================================================

  loadNotifications(): void {

    if (
      !this.currentTeacher?.id
    ) {

      return;
    }


    this.isLoading =
      true;


    this.notificationService
      .getTeacherNotifications(
        this.currentTeacher.id
      )
      .subscribe({

        next: notifications => {

          this.notifications =
            notifications || [];


          this.calculateUnreadCount();


          this.isLoading =
            false;
        },


        error: err => {

          console.error(
            'Öğretmen bildirimleri yüklenirken hata:',
            err
          );


          this.isLoading =
            false;
        }

      });
  }


  // =========================================================
  // FİLTRELENMİŞ BİLDİRİMLER
  // =========================================================

  get filteredNotifications():
    AppNotification[] {

    if (
      this.selectedFilter ===
      'UNREAD'
    ) {

      return this.notifications
        .filter(
          notification =>
            !notification.read
        );
    }


    if (
      this.selectedFilter ===
      'READ'
    ) {

      return this.notifications
        .filter(
          notification =>
            notification.read
        );
    }


    return this.notifications;
  }


  // =========================================================
  // OKUNMAMIŞ SAYISINI HESAPLA
  // =========================================================

  calculateUnreadCount(): void {

    this.unreadCount =
      this.notifications
        .filter(
          notification =>
            !notification.read
        )
        .length;


    this.notificationService
      .setTeacherUnreadCount(
        this.unreadCount
      );
  }


  // =========================================================
  // TEK BİLDİRİMİ OKUNDU YAP
  // =========================================================

  markAsRead(
    notification:
    AppNotification
  ): void {

    /*
     * Zaten okunmuşsa
     * tekrar API çağrısı yapma.
     */
    if (
      notification.read
    ) {

      return;
    }


    this.notificationService
      .markTeacherNotificationAsRead(

        this.currentTeacher.id,

        notification.id

      )
      .subscribe({

        next:
          updatedNotification => {

            notification.read =
              updatedNotification.read;


            this.calculateUnreadCount();
          },


        error: err => {

          console.error(
            'Bildirim okundu yapılırken hata:',
            err
          );
        }

      });
  }


  // =========================================================
  // TÜMÜNÜ OKUNDU YAP
  // =========================================================

  markAllAsRead(): void {

    if (
      this.unreadCount === 0
    ) {

      return;
    }


    const confirmed =
      confirm(
        'Tüm bildirimleri okundu olarak işaretlemek istiyor musunuz?'
      );


    if (!confirmed) {

      return;
    }


    this.notificationService
      .markAllTeacherNotificationsAsRead(
        this.currentTeacher.id
      )
      .subscribe({

        next: () => {

          this.notifications
            .forEach(
              notification => {

                notification.read =
                  true;
              }
            );


          this.calculateUnreadCount();
        },


        error: err => {

          console.error(
            'Bildirimler okundu yapılırken hata:',
            err
          );
        }

      });
  }


  // =========================================================
  // FILTER
  // =========================================================

  setFilter(
    filter:
      'ALL' |
      'UNREAD' |
      'READ'
  ): void {

    this.selectedFilter =
      filter;
  }


  // =========================================================
  // OKUNMUŞ SAYISI
  // =========================================================

  get readCount(): number {

    return this.notifications
      .filter(
        notification =>
          notification.read
      )
      .length;
  }


  // =========================================================
  // SATIŞ BİLDİRİM SAYISI
  // =========================================================

  get saleNotificationCount():
    number {

    return this.notifications
      .filter(
        notification =>
          notification.type ===
          'SALE'
      )
      .length;
  }


  // =========================================================
  // BİLDİRİM İKONU
  // =========================================================

  getNotificationIcon(
    type: string
  ): string {

    switch (type) {

      case 'SALE':
        return '🛒';


      case 'REVIEW':
        return '⭐';


      case 'STUDENT':
        return '👥';


      case 'COURSE_COMPLETED':
        return '🎓';


      case 'PAYMENT':
        return '💳';


      case 'COURSE':
        return '📚';


      case 'ANNOUNCEMENT':
        return '📢';


      case 'CERTIFICATE':
        return '🏆';


      default:
        return '🔔';
    }
  }


  // =========================================================
  // TÜR YAZISI
  // =========================================================

  getTypeText(
    type: string
  ): string {

    switch (type) {

      case 'SALE':
        return 'Satış';


      case 'REVIEW':
        return 'Değerlendirme';


      case 'STUDENT':
        return 'Yeni Öğrenci';


      case 'COURSE_COMPLETED':
        return 'Kurs Tamamlandı';


      case 'PAYMENT':
        return 'Ödeme';


      case 'COURSE':
        return 'Kurs';


      case 'ANNOUNCEMENT':
        return 'Duyuru';


      default:
        return 'Bildirim';
    }
  }
}
