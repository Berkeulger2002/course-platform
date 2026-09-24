import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';

import { CourseService } from '../../services/course.service';
import {
  CourseProgress,
  ProgressService
} from '../../services/progress.service';

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './my-courses.component.html',
  styleUrl: './my-courses.component.css'
})
export class MyCoursesComponent implements OnInit {

  currentStudent: any;

  myCourses: any[] = [];


  // =========================================================
  // VIDEO PLAYER
  // =========================================================

  selectedVideoUrl: string | null = null;

  selectedVideoCourse: any = null;

  isVideoLoading = false;


  // =========================================================
  // VIDEO İLERLEME TAKİBİ
  // =========================================================

  /*
   * Öğrencinin videoda daha önce kaldığı yer.
   */
  savedPositionSeconds = 0;


  /*
   * Player'ın şu an bulunduğu saniye.
   */
  currentPositionSeconds = 0;


  /*
   * Videonun toplam süresi.
   */
  currentDurationSeconds = 0;


  /*
   * Son otomatik kayıt zamanı.
   *
   * Her timeupdate eventinde backend'e istek
   * atmak istemediğimiz için kullanıyoruz.
   */
  private lastProgressSaveMs = 0;


  /*
   * Yaklaşık kaç saniyede bir progress kaydedilecek?
   */
  private readonly progressSaveIntervalMs = 10000;


  constructor(
    private courseService: CourseService,
    private progressService: ProgressService,
    private router: Router
  ) {}


  // =========================================================
  // SAYFA AÇILIŞI
  // =========================================================
  ngOnInit(): void {

    const userStr =
      localStorage.getItem('currentUser');


    if (!userStr) {

      this.router.navigate(['/auth']);

      return;
    }


    this.currentStudent =
      JSON.parse(userStr);


    this.loadCourses();
  }


  // =========================================================
  // KURSLARIM
  // =========================================================
  loadCourses(): void {

    /*
     * Hem satın alınan kursları,
     * hem de öğrencinin progress kayıtlarını alıyoruz.
     */
    forkJoin({

      courses:
        this.courseService
          .getEnrolledCourses(
            this.currentStudent.id
          ),

      progresses:
        this.progressService
          .getStudentProgress(
            this.currentStudent.id
          )
          .pipe(
            catchError(err => {

              console.error(
                'İlerleme bilgileri alınamadı:',
                err
              );

              /*
               * Progress tarafında sorun olsa bile
               * kursların görünmesini engellemiyoruz.
               */
              return of([]);
            })
          )

    }).subscribe({

      next: (result: any) => {

        const progresses:
          CourseProgress[] =
          result.progresses || [];


        this.myCourses =
          (result.courses || [])
            .map((course: any) => {

              const progress =
                progresses.find(
                  item =>
                    item.courseId === course.id
                );


              return {

                ...course,

                tempRating: 5,

                tempComment: '',

                showReviews: false,

                reviews: [],


                /*
                 * Artık kurs kartı da ilerleme
                 * bilgisini gösterebilir.
                 */
                progressPercentage:
                  progress?.progressPercentage || 0,

                lastPositionSeconds:
                  progress?.lastPositionSeconds || 0,

                durationSeconds:
                  progress?.durationSeconds || 0,

                completed:
                  progress?.completed || false,

                lastWatchedAt:
                  progress?.lastWatchedAt || null

              };

            });
      },

      error: (err) => {

        console.error(
          'Kurslar yüklenirken hata:',
          err
        );
      }

    });
  }


  // =========================================================
  // PRIVATE VIDEONUN SIGNED URL'SİNİ AL
  // =========================================================
  openCourseVideo(course: any): void {

    if (!course?.id) {
      return;
    }


    /*
     * Yeni video açılmadan önce eski player verilerini
     * temizliyoruz.
     */
    this.selectedVideoCourse = course;

    this.selectedVideoUrl = null;

    this.savedPositionSeconds = 0;

    this.currentPositionSeconds = 0;

    this.currentDurationSeconds = 0;

    this.lastProgressSaveMs = Date.now();

    this.isVideoLoading = true;


    /*
     * Aynı anda:
     *
     * 1) Private video signed URL
     * 2) Öğrencinin eski progress kaydı
     *
     * isteniyor.
     */
    forkJoin({

      video:
        this.courseService
          .getCourseVideo(
            course.id,
            this.currentStudent.id
          ),

      progress:
        this.progressService
          .getCourseProgress(
            this.currentStudent.id,
            course.id
          )
          .pipe(
            catchError(err => {

              console.error(
                'Kurs ilerlemesi alınamadı:',
                err
              );

              /*
               * Progress kaydı alınamazsa video yine açılsın.
               */
              return of(null);
            })
          )

    }).subscribe({

      next: (result) => {

        this.selectedVideoUrl =
          result.video.videoUrl;


        /*
         * Kurs daha önce tamamlandıysa yeniden
         * oynatıldığında baştan başlasın.
         *
         * Tamamlanmadıysa kaldığı saniyeden devam etsin.
         */
        if (
          result.progress &&
          !result.progress.completed
        ) {

          this.savedPositionSeconds =
            result.progress.lastPositionSeconds || 0;

        } else {

          this.savedPositionSeconds = 0;
        }


        /*
         * Kurs kartındaki güncel bilgileri de
         * yeniliyoruz.
         */
        if (result.progress) {

          course.progressPercentage =
            result.progress.progressPercentage;

          course.completed =
            result.progress.completed;

          course.lastPositionSeconds =
            result.progress.lastPositionSeconds;

          course.durationSeconds =
            result.progress.durationSeconds;

          course.lastWatchedAt =
            result.progress.lastWatchedAt;
        }


        this.isVideoLoading = false;


        /*
         * Player sayfanın üst kısmında olduğu için
         * videoyu açınca yukarı kaydırıyoruz.
         */
        window.scrollTo({
          top: 0,
          behavior: 'smooth'
        });
      },

      error: (err) => {

        this.isVideoLoading = false;

        this.selectedVideoUrl = null;

        this.selectedVideoCourse = null;


        console.error(
          'Video açılırken hata:',
          err
        );


        if (err.status === 403) {

          alert(
            'Bu kursun videosuna erişim yetkiniz bulunmuyor.'
          );

          return;
        }


        if (err.status === 404) {

          alert(
            err.error?.message ||
            'Bu kurs için henüz video yüklenmemiş.'
          );

          return;
        }


        alert(
          'Video açılırken bir sorun oluştu.'
        );
      }

    });
  }


  // =========================================================
  // VIDEO METADATA YÜKLENDİ
  // =========================================================
  //
  // Video süresi artık belli olduğu için
  // öğrenciyi kaldığı noktaya götürüyoruz.
  //
  // =========================================================
  onVideoLoadedMetadata(
    video: HTMLVideoElement
  ): void {

    if (!video) {
      return;
    }


    if (
      Number.isFinite(video.duration)
    ) {

      this.currentDurationSeconds =
        video.duration;
    }


    /*
     * Daha önce kaldığı yer varsa:
     *
     * Örn:
     * 04:35
     *
     * video doğrudan oradan devam eder.
     */
    if (
      this.savedPositionSeconds > 0 &&
      this.savedPositionSeconds <
      video.duration - 3
    ) {

      video.currentTime =
        this.savedPositionSeconds;


      this.currentPositionSeconds =
        this.savedPositionSeconds;
    }
  }


  // =========================================================
  // VIDEO OYNATILIRKEN
  // =========================================================
  //
  // timeupdate tarayıcı tarafından sık sık tetiklenir.
  // Biz backend'e yaklaşık 10 saniyede bir kayıt atıyoruz.
  //
  // =========================================================
  onVideoTimeUpdate(
    video: HTMLVideoElement
  ): void {

    if (!video) {
      return;
    }


    this.currentPositionSeconds =
      video.currentTime || 0;


    if (
      Number.isFinite(video.duration)
    ) {

      this.currentDurationSeconds =
        video.duration;
    }


    const now = Date.now();


    /*
     * Yaklaşık 10 saniye geçtiyse
     * progress'i kaydet.
     */
    if (
      now - this.lastProgressSaveMs >=
      this.progressSaveIntervalMs
    ) {

      this.lastProgressSaveMs = now;

      this.saveCurrentProgress();
    }
  }


  // =========================================================
  // VIDEO DURDURULDU
  // =========================================================
  onVideoPause(
    video: HTMLVideoElement
  ): void {

    if (!video) {
      return;
    }


    this.currentPositionSeconds =
      video.currentTime || 0;


    if (
      Number.isFinite(video.duration)
    ) {

      this.currentDurationSeconds =
        video.duration;
    }


    this.saveCurrentProgress();
  }


  // =========================================================
  // VIDEO BİTTİ
  // =========================================================
  onVideoEnded(
    video: HTMLVideoElement
  ): void {

    if (!video) {
      return;
    }


    this.currentDurationSeconds =
      video.duration || 0;


    this.currentPositionSeconds =
      this.currentDurationSeconds;


    /*
     * Videonun sonuna gelindiği için backend
     * %100'e yakın progress hesaplayacak ve
     * completed = true yapacak.
     */
    this.saveCurrentProgress();
  }


  // =========================================================
  // PROGRESS BACKEND'E KAYDET
  // =========================================================
  private saveCurrentProgress(): void {

    /*
     * Video veya öğrenci seçili değilse kayıt yapma.
     */
    if (
      !this.currentStudent?.id ||
      !this.selectedVideoCourse?.id
    ) {

      return;
    }


    /*
     * Video süresi henüz belli değilse kayıt yapma.
     */
    if (
      !this.currentDurationSeconds ||
      this.currentDurationSeconds <= 0
    ) {

      return;
    }


    /*
     * Değerleri değişkenlere alıyoruz.
     *
     * Player kapatılsa bile HTTP isteği
     * doğru courseId ile devam edebilsin.
     */
    const studentId =
      this.currentStudent.id;

    const courseId =
      this.selectedVideoCourse.id;

    const currentTime =
      this.currentPositionSeconds;

    const duration =
      this.currentDurationSeconds;


    this.progressService
      .updateProgress(
        studentId,
        courseId,
        currentTime,
        duration
      )
      .subscribe({

        next: (progress) => {

          /*
           * Kurs kartındaki bilgiyi de
           * anlık olarak güncelliyoruz.
           */
          const course =
            this.myCourses.find(
              item =>
                item.id === courseId
            );


          if (course) {

            course.progressPercentage =
              progress.progressPercentage;

            course.lastPositionSeconds =
              progress.lastPositionSeconds;

            course.durationSeconds =
              progress.durationSeconds;

            course.completed =
              progress.completed;

            course.lastWatchedAt =
              progress.lastWatchedAt;
          }
        },

        error: (err) => {

          console.error(
            'İlerleme kaydedilirken hata:',
            err
          );
        }

      });
  }


  // =========================================================
  // VIDEO PLAYER KAPAT
  // =========================================================
  closeCourseVideo(): void {

    /*
     * Kapatmadan hemen önce son konumu kaydet.
     */
    this.saveCurrentProgress();


    this.selectedVideoUrl = null;

    this.selectedVideoCourse = null;

    this.savedPositionSeconds = 0;

    this.currentPositionSeconds = 0;

    this.currentDurationSeconds = 0;

    this.isVideoLoading = false;
  }


  // =========================================================
  // YORUM EKLE
  // =========================================================
  submitReview(course: any): void {

    if (
      !course.tempComment ||
      course.tempComment.trim() === ''
    ) {

      alert(
        'Lütfen bir yorum yazın.'
      );

      return;
    }


    const reviewPayload = {

      courseId: course.id,

      studentId:
      this.currentStudent.id,

      studentName:
      this.currentStudent.name,

      rating:
      course.tempRating,

      comment:
      course.tempComment

    };


    this.courseService
      .addReview(reviewPayload)
      .subscribe({

        next: () => {

          alert(
            `"${course.name}" için yorumunuz eklendi.`
          );


          course.tempComment = '';

          course.tempRating = 5;


          if (course.showReviews) {

            this.loadReviews(course);
          }
        },

        error: (err) => {

          console.error(
            'Yorum gönderilirken hata:',
            err
          );


          alert(
            'Yorum gönderilirken bir sorun oluştu.'
          );
        }

      });
  }


  // =========================================================
  // YORUMLARI AÇ / KAPAT
  // =========================================================
  toggleReviews(course: any): void {

    if (course.showReviews) {

      course.showReviews = false;

      return;
    }


    this.loadReviews(course);
  }


  // =========================================================
  // YORUMLARI GETİR
  // =========================================================
  private loadReviews(
    course: any
  ): void {

    this.courseService
      .getCourseReviews(
        course.id
      )
      .subscribe({

        next: (reviews) => {

          course.reviews =
            reviews;

          course.showReviews =
            true;
        },

        error: (err) => {

          console.error(
            'Yorumlar yüklenirken hata:',
            err
          );
        }

      });
  }


  // =========================================================
  // YILDIZLAR
  // =========================================================
  getStars(
    rating: number
  ): string {

    return '⭐'.repeat(rating);
  }


  // =========================================================
  // SÜREYİ GÖSTERMEK İÇİN
  // =========================================================
  //
  // 325 saniye -> 05:25
  //
  // HTML'de daha sonra kullanacağız.
  //
  // =========================================================
  formatTime(
    seconds: number
  ): string {

    if (
      !seconds ||
      !Number.isFinite(seconds)
    ) {

      return '00:00';
    }


    const minutes =
      Math.floor(seconds / 60);


    const remainingSeconds =
      Math.floor(seconds % 60);


    return (
      String(minutes)
        .padStart(2, '0')
      +
      ':'
      +
      String(remainingSeconds)
        .padStart(2, '0')
    );
  }
}
