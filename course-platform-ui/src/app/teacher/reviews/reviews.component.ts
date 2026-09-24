import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  catchError,
  forkJoin,
  map,
  of
} from 'rxjs';

import { CourseService } from '../../services/course.service';


@Component({
  selector: 'app-reviews',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './reviews.component.html',
  styleUrl: './reviews.component.css'
})
export class ReviewsComponent implements OnInit {

  currentTeacher: any;

  courses: any[] = [];

  reviews: any[] = [];

  isLoading = true;


  // =========================================================
  // FILTER
  // =========================================================

  selectedCourseId: number | null = null;


  constructor(
    private courseService: CourseService,
    private router: Router
  ) {}


  ngOnInit(): void {

    const userStr =
      localStorage.getItem('currentUser');


    if (!userStr) {

      this.router.navigate(['/auth']);

      return;
    }


    this.currentTeacher =
      JSON.parse(userStr);


    this.loadReviews();
  }


  // =========================================================
  // TÜM KURSLAR + YORUMLAR
  // =========================================================

  loadReviews(): void {

    if (!this.currentTeacher?.id) {

      return;
    }


    this.isLoading = true;


    this.courseService
      .getAllCoursesForTeacher(
        this.currentTeacher.id
      )
      .subscribe({

        next: (courses) => {

          this.courses =
            courses || [];


          /*
           * Öğretmenin hiç kursu yoksa
           * review isteği atmaya gerek yok.
           */
          if (this.courses.length === 0) {

            this.reviews = [];

            this.isLoading = false;

            return;
          }


          /*
           * Her kursun yorumlarını paralel çekiyoruz.
           */
          const requests =
            this.courses.map(course =>

              this.courseService
                .getCourseReviews(course.id)
                .pipe(

                  catchError(err => {

                    console.error(
                      `${course.name} yorumları alınamadı:`,
                      err
                    );

                    return of([]);
                  }),


                  map((reviews: any) => {

                    const reviewList =
                      Array.isArray(reviews)
                        ? reviews
                        : [];


                    /*
                     * Review objesine kurs bilgisi ekliyoruz.
                     */
                    return reviewList.map(
                      review => ({

                        ...review,

                        courseId:
                        course.id,

                        courseName:
                        course.name,

                        courseImageUrl:
                        course.imageUrl

                      })
                    );
                  })

                )

            );


          forkJoin(requests)
            .subscribe({

              next: (reviewGroups) => {

                /*
                 * [[...], [...]]
                 * yapısını tek listeye indir.
                 */
                this.reviews =
                  reviewGroups.flat();


                /*
                 * En yeni yorum üstte.
                 */
                this.reviews.sort(
                  (a, b) => {

                    const aDate =
                      this.getReviewDateValue(a);

                    const bDate =
                      this.getReviewDateValue(b);

                    return bDate - aDate;
                  }
                );


                this.isLoading =
                  false;
              },


              error: (err) => {

                console.error(
                  'Yorumlar yüklenirken hata:',
                  err
                );


                this.isLoading =
                  false;
              }

            });
        },


        error: (err) => {

          console.error(
            'Kurslar yüklenirken hata:',
            err
          );


          this.isLoading =
            false;
        }

      });
  }


  // =========================================================
  // FİLTRELENMİŞ YORUMLAR
  // =========================================================

  get filteredReviews(): any[] {

    if (!this.selectedCourseId) {

      return this.reviews;
    }


    return this.reviews.filter(
      review =>
        Number(review.courseId)
        ===
        Number(this.selectedCourseId)
    );
  }


  // =========================================================
  // TOPLAM YORUM
  // =========================================================

  get totalReviews(): number {

    return this.reviews.length;
  }


  // =========================================================
  // ORTALAMA PUAN
  // =========================================================

  get averageRating(): number {

    if (this.reviews.length === 0) {

      return 0;
    }


    const total =
      this.reviews.reduce(
        (sum, review) =>
          sum +
          this.getRating(review),

        0
      );


    return total /
      this.reviews.length;
  }


  // =========================================================
  // 5 YILDIZ SAYISI
  // =========================================================

  get fiveStarReviews(): number {

    return this.reviews.filter(
      review =>
        Math.round(
          this.getRating(review)
        ) === 5
    ).length;
  }


  // =========================================================
  // YORUM GELEN KURS SAYISI
  // =========================================================

  get reviewedCourseCount(): number {

    return new Set(
      this.reviews.map(
        review =>
          review.courseId
      )
    ).size;
  }


  // =========================================================
  // BACKEND FIELD UYUMLULUĞU
  // =========================================================

  getRating(review: any): number {

    return Number(
      review?.rating
      ??
      review?.score
      ??
      review?.stars
      ??
      0
    );
  }


  getComment(review: any): string {

    return (
      review?.comment
      ??
      review?.review
      ??
      review?.content
      ??
      review?.text
      ??
      'Yorum metni bulunmuyor.'
    );
  }


  getStudentName(review: any): string {

    return (
      review?.studentName
      ??
      review?.student?.name
      ??
      review?.userName
      ??
      review?.user?.name
      ??
      'Öğrenci'
    );
  }


  getReviewDate(review: any): any {

    return (
      review?.createdAt
      ??
      review?.reviewDate
      ??
      review?.date
      ??
      null
    );
  }


  private getReviewDateValue(
    review: any
  ): number {

    const value =
      this.getReviewDate(review);


    if (!value) {

      return 0;
    }


    return new Date(value)
      .getTime();
  }


  // =========================================================
  // YILDIZ
  // =========================================================

  getStars(review: any): string {

    const rating =
      Math.max(
        0,
        Math.min(
          Math.round(
            this.getRating(review)
          ),
          5
        )
      );


    return '★'.repeat(rating)
      +
      '☆'.repeat(5 - rating);
  }


  // =========================================================
  // KURSLARA GİT
  // =========================================================

  goToCourses(): void {

    this.router.navigate([
      '/teacher/courses'
    ]);
  }
}
