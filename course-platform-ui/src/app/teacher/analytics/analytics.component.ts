import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { CourseService } from '../../services/course.service';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.css'
})
export class AnalyticsComponent implements OnInit {

  currentTeacher: any;

  courses: any[] = [];

  isLoading = true;


  // =========================================================
  // GENEL İSTATİSTİKLER
  // =========================================================

  totalCourses = 0;

  totalEnrollments = 0;

  totalCapacity = 0;

  activeCourses = 0;

  coursesWithVideo = 0;

  averagePrice = 0;

  occupancyRate = 0;


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


    this.loadAnalytics();
  }


  // =========================================================
  // VERİLERİ GETİR
  // =========================================================

  loadAnalytics(): void {

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


          this.calculateStatistics();


          this.isLoading =
            false;
        },


        error: (err) => {

          console.error(
            'İstatistikler yüklenirken hata:',
            err
          );


          this.isLoading =
            false;
        }

      });
  }


  // =========================================================
  // HESAPLAMALAR
  // =========================================================

  calculateStatistics(): void {

    this.totalCourses =
      this.courses.length;


    this.totalEnrollments =
      this.courses.reduce(
        (total, course) =>
          total +
          (course.currentEnrolled || 0),

        0
      );


    this.totalCapacity =
      this.courses.reduce(
        (total, course) =>
          total +
          (course.maxCapacity || 0),

        0
      );


    this.activeCourses =
      this.courses.filter(
        course =>
          this.isPurchasable(course)
      ).length;


    this.coursesWithVideo =
      this.courses.filter(
        course =>
          course.videoPath
      ).length;


    const totalPrice =
      this.courses.reduce(
        (total, course) =>
          total +
          Number(course.price || 0),

        0
      );


    this.averagePrice =
      this.totalCourses > 0

        ? totalPrice /
        this.totalCourses

        : 0;


    /*
     * maxCapacity = 0 olan kursları
     * sınırsız kapasite kabul ediyoruz.
     *
     * Doluluk hesabında yalnızca kapasitesi
     * belirlenmiş kursları kullanıyoruz.
     */
    const limitedCourses =
      this.courses.filter(
        course =>
          (course.maxCapacity || 0) > 0
      );


    const limitedCapacity =
      limitedCourses.reduce(
        (total, course) =>
          total +
          course.maxCapacity,

        0
      );


    const limitedEnrollments =
      limitedCourses.reduce(
        (total, course) =>
          total +
          (course.currentEnrolled || 0),

        0
      );


    this.occupancyRate =
      limitedCapacity > 0

        ? Math.min(
          (
            limitedEnrollments /
            limitedCapacity
          ) * 100,
          100
        )

        : 0;
  }


  // =========================================================
  // SATIŞ DURUMU
  // =========================================================

  isPurchasable(
    course: any
  ): boolean {

    return (
      course?.purchasable === true ||
      course?.isPurchasable === true
    );
  }


  // =========================================================
  // KURS DOLULUK ORANI
  // =========================================================

  getCourseOccupancy(
    course: any
  ): number {

    if (
      !course.maxCapacity ||
      course.maxCapacity <= 0
    ) {

      return 0;
    }


    return Math.min(
      (
        (course.currentEnrolled || 0)
        /
        course.maxCapacity
      ) * 100,
      100
    );
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
