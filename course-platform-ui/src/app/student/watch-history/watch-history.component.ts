import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import {
  CourseProgress,
  ProgressService
} from '../../services/progress.service';

@Component({
  selector: 'app-watch-history',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './watch-history.component.html',
  styleUrl: './watch-history.component.css'
})
export class WatchHistoryComponent implements OnInit {

  currentStudent: any;

  watchHistory: CourseProgress[] = [];

  isLoading = true;


  constructor(
    private progressService: ProgressService,
    private router: Router
  ) {}


  ngOnInit(): void {

    const userStr =
      localStorage.getItem('currentUser');


    if (!userStr) {

      this.router.navigate(['/auth']);

      return;
    }


    this.currentStudent =
      JSON.parse(userStr);


    this.loadWatchHistory();
  }


  // =========================================================
  // İZLEME GEÇMİŞİNİ YÜKLE
  // =========================================================
  loadWatchHistory(): void {

    this.isLoading = true;


    this.progressService
      .getStudentProgress(
        this.currentStudent.id
      )
      .subscribe({

        next: (progressList) => {

          /*
           * Sadece gerçekten izlenmiş kursları gösteriyoruz.
           *
           * lastWatchedAt null ise öğrenci kursu hiç açmamış.
           */
          this.watchHistory =
            progressList
              .filter(
                item =>
                  item.lastWatchedAt !== null
              )
              .sort(
                (a, b) => {

                  const dateA =
                    new Date(
                      a.lastWatchedAt!
                    ).getTime();

                  const dateB =
                    new Date(
                      b.lastWatchedAt!
                    ).getTime();


                  return dateB - dateA;
                }
              );


          this.isLoading = false;
        },

        error: (err) => {

          this.isLoading = false;

          console.error(
            'İzleme geçmişi yüklenirken hata:',
            err
          );
        }

      });
  }


  // =========================================================
  // KURSLARIMA GİT
  // =========================================================
  goToCourses(): void {

    this.router.navigate([
      '/student/courses'
    ]);
  }


  // =========================================================
  // SANİYEYİ FORMATLA
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


    const totalSeconds =
      Math.floor(seconds);


    const hours =
      Math.floor(
        totalSeconds / 3600
      );


    const minutes =
      Math.floor(
        (totalSeconds % 3600) / 60
      );


    const secs =
      totalSeconds % 60;


    if (hours > 0) {

      return (
        String(hours)
          .padStart(2, '0')
        +
        ':'
        +
        String(minutes)
          .padStart(2, '0')
        +
        ':'
        +
        String(secs)
          .padStart(2, '0')
      );
    }


    return (
      String(minutes)
        .padStart(2, '0')
      +
      ':'
      +
      String(secs)
        .padStart(2, '0')
    );
  }
}
