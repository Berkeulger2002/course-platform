import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class UserActivityService {


  // =========================================================
  // HEARTBEAT ENDPOINT
  // =========================================================

  private readonly heartbeatUrl =
    'http://localhost:8081/api/auth/heartbeat';


  // =========================================================
  // INTERVAL
  //
  // Kullanıcı siteyi görünür sekmede kullanırken
  // yaklaşık 60 saniyede bir heartbeat gönderilir.
  // =========================================================

  private readonly heartbeatIntervalMs =
    60_000;


  private heartbeatIntervalId:
    ReturnType<typeof setInterval>
    | null = null;


  private running =
    false;


  constructor(
    private http: HttpClient
  ) {
  }


  // =========================================================
  // VISIBILITY EVENT
  // =========================================================

  private readonly visibilityHandler =
    (): void => {


      if (
        document.visibilityState ===
        'visible'
      ) {


        this.sendHeartbeat();
      }
    };


  // =========================================================
  // START
  // =========================================================

  start(): void {


    if (
      this.running
    ) {


      return;
    }


    this.running =
      true;


    // =====================================================
    // TAB TEKRAR AÇILDIĞINDA
    // =====================================================

    document.addEventListener(

      'visibilitychange',

      this.visibilityHandler

    );


    // =====================================================
    // İLK HEARTBEAT
    // =====================================================

    if (
      document.visibilityState ===
      'visible'
    ) {


      this.sendHeartbeat();
    }


    // =====================================================
    // 60 SANİYE POLLING
    // =====================================================

    this.heartbeatIntervalId =

      setInterval(

        () => {


          if (
            document.visibilityState ===
            'visible'
          ) {


            this.sendHeartbeat();
          }

        },

        this.heartbeatIntervalMs

      );
  }


  // =========================================================
  // STOP
  // =========================================================

  stop(): void {


    if (
      !this.running
    ) {


      return;
    }


    this.running =
      false;


    if (
      this.heartbeatIntervalId !==
      null
    ) {


      clearInterval(
        this.heartbeatIntervalId
      );


      this.heartbeatIntervalId =
        null;
    }


    document.removeEventListener(

      'visibilitychange',

      this.visibilityHandler

    );
  }


  // =========================================================
  // SEND HEARTBEAT
  // =========================================================

  private sendHeartbeat(): void {


    this.http
      .post<void>(

        this.heartbeatUrl,

        {}

      )
      .subscribe({


        next: () => {

          // Heartbeat başarılı.
        },


        error: (
          error
        ) => {


          // =================================================
          // SESSION ARTIK GEÇERLİ DEĞİL
          // =================================================

          if (
            error?.status === 401
            ||
            error?.status === 403
          ) {


            this.stop();


            return;
          }


          // =================================================
          // GEÇİCİ NETWORK HATASI
          //
          // Tracking'i kapatmıyoruz.
          // Sonraki heartbeat tekrar deneyecek.
          // =================================================

          console.warn(
            'Heartbeat gönderilemedi:',
            error
          );
        }

      });
  }
}
