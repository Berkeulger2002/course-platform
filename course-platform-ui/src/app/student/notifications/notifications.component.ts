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
  selector:
    'app-notifications',

  standalone:
    true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './notifications.component.html',

  styleUrl:
    './notifications.component.css'
})
export class NotificationsComponent
  implements OnInit {


  currentStudent:
    any;


  notifications:
    AppNotification[] = [];


  unreadCount =
    0;


  isLoading =
    true;


  constructor(

    private notificationService:
    NotificationService,

    private router:
    Router

  ) {}


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {


    const userStr =
      localStorage.getItem(
        'currentUser'
      );


    if (
      !userStr
    ) {


      this.router.navigate([
        '/auth'
      ]);


      return;
    }


    this.currentStudent =
      JSON.parse(
        userStr
      );


    this.loadNotifications();
  }


  // =========================================================
  // BİLDİRİMLERİ GETİR
  // =========================================================

  loadNotifications(): void {


    this.isLoading =
      true;


    this.notificationService
      .getNotifications(
        this.currentStudent.id
      )
      .subscribe({


        next: (
          notifications
        ) => {


          this.notifications =
            notifications;


          /*
           * Hem bu sayfadaki sayıyı
           * hem sidebar'daki BehaviorSubject'i
           * aynı anda günceller.
           */
          this.calculateUnreadCount();


          this.isLoading =
            false;
        },


        error: (
          err
        ) => {


          console.error(
            'Bildirimler yüklenirken hata:',
            err
          );


          this.isLoading =
            false;
        }

      });
  }


  // =========================================================
  // OKUNMAMIŞ SAYISINI HESAPLA
  //
  // ÖNEMLİ:
  // Sidebar da aynı değeri kullanabilsin diye
  // NotificationService içerisindeki
  // BehaviorSubject da burada güncellenir.
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
      .setUnreadCount(
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
     * Zaten okunmuş bildirime
     * tekrar API isteği gönderme.
     */
    if (
      notification.read
    ) {

      return;
    }


    this.notificationService
      .markAsRead(

        this.currentStudent.id,

        notification.id

      )
      .subscribe({


        next: (
          updatedNotification
        ) => {


          /*
           * Backend'den dönen gerçek
           * read durumunu kullanıyoruz.
           */
          notification.read =
            updatedNotification.read;


          /*
           * Sayfa içindeki sayı:
           * 3 → 2
           *
           * Sidebar badge:
           * 3 → 2
           *
           * anında güncellenir.
           */
          this.calculateUnreadCount();
        },


        error: (
          err
        ) => {


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


    this.notificationService
      .markAllAsRead(
        this.currentStudent.id
      )
      .subscribe({


        next: () => {


          /*
           * Local listedeki bütün
           * bildirimleri de okundu yap.
           */
          this.notifications
            .forEach(
              notification => {

                notification.read =
                  true;

              }
            );


          /*
           * Hem sayfadaki count'u
           * hem sidebar badge'i
           * anında sıfırlar.
           */
          this.calculateUnreadCount();
        },


        error: (
          err
        ) => {


          console.error(
            'Bildirimler okundu yapılırken hata:',
            err
          );
        }

      });
  }


  // =========================================================
  // BİLDİRİM İKONU
  // =========================================================

  getNotificationIcon(
    type: string
  ): string {


    switch (
      type
      ) {


      case 'CERTIFICATE':

        return '🏆';


      case 'COURSE':

        return '🎓';


      case 'PAYMENT':

        return '💳';


      case 'ANNOUNCEMENT':

        return '📢';


      default:

        return '🔔';
    }
  }
}
