import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';

import { CourseService } from '../../services/course.service';
import {
  CourseProgress,
  ProgressService
} from '../../services/progress.service';

@Component({
  selector: 'app-progress',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './progress.component.html',
  styleUrl: './progress.component.css'
})
export class ProgressComponent implements OnInit {

  currentStudent: any;

  progressItems: any[] = [];

  isLoading = true;

  totalCourses = 0;

  completedCourses = 0;

  averageProgress = 0;


  constructor(
    private courseService: CourseService,
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


    this.loadProgress();
  }


  // =========================================================
  // TÜM KURS İLERLEMELERİ
  // =========================================================
  loadProgress(): void {

    this.isLoading = true;


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
                'İlerleme verileri alınamadı:',
                err
              );

              return of([]);
            })
          )

    }).subscribe({

      next: (result: any) => {

        const courses =
          result.courses || [];

        const progresses:
          CourseProgress[] =
          result.progresses || [];


        /*
         * Satın alınan bütün kursları gösteriyoruz.
         *
         * Hiç izlenmemiş kurslar da %0 olarak
         * listede bulunacak.
         */
        this.progressItems =
          courses.map((course: any) => {

            const progress =
              progresses.find(
                item =>
                  item.courseId === course.id
              );


            return {

              courseId:
              course.id,

              courseName:
              course.name,

              description:
              course.description,

              imageUrl:
              course.imageUrl,

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


        this.calculateStatistics();

        this.isLoading = false;
      },

      error: (err) => {

        this.isLoading = false;

        console.error(
          'İlerleme sayfası yüklenirken hata:',
          err
        );
      }

    });
  }


  // =========================================================
  // İSTATİSTİKLER
  // =========================================================
  private calculateStatistics(): void {

    this.totalCourses =
      this.progressItems.length;


    this.completedCourses =
      this.progressItems.filter(
        item => item.completed
      ).length;


    if (this.totalCourses === 0) {

      this.averageProgress = 0;

      return;
    }


    const totalProgress =
      this.progressItems.reduce(
        (total, item) =>
          total +
          Number(
            item.progressPercentage || 0
          ),
        0
      );


    this.averageProgress =
      totalProgress /
      this.totalCourses;
  }


  // =========================================================
  // KURSA GİT
  // =========================================================
  goToCourses(): void {

    this.router.navigate([
      '/student/courses'
    ]);
  }


  // =========================================================
  // SANİYE -> SAAT / DAKİKA / SANİYE
  // =========================================================
  formatTime(seconds: number): string {

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
