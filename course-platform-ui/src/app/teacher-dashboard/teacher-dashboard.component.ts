import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { CourseService } from '../services/course.service';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './teacher-dashboard.component.html',
  styleUrl: './teacher-dashboard.component.css'
})
export class TeacherDashboardComponent implements OnInit {

  currentTeacher: any;

  courses: any[] = [];

  isLoading = true;

  totalCourses = 0;
  totalStudents = 0;
  purchasableCourses = 0;
  totalCapacity = 0;


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


    this.loadDashboard();
  }


  // =========================================================
  // DASHBOARD VERİLERİNİ YÜKLE
  // =========================================================
  loadDashboard(): void {

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


          this.isLoading = false;
        },

        error: (err) => {

          console.error(
            'Öğretmen kursları yüklenirken hata:',
            err
          );

          this.isLoading = false;
        }

      });
  }


  // =========================================================
  // İSTATİSTİKLER
  // =========================================================
  calculateStatistics(): void {

    this.totalCourses =
      this.courses.length;


    this.totalStudents =
      this.courses.reduce(
        (total, course) =>
          total + (course.currentEnrolled || 0),
        0
      );


    this.purchasableCourses =
      this.courses.filter(
        course =>
          course.purchasable === true ||
          course.isPurchasable === true
      ).length;


    this.totalCapacity =
      this.courses.reduce(
        (total, course) =>
          total + (course.maxCapacity || 0),
        0
      );
  }


  // =========================================================
  // SON KURSLAR
  // =========================================================
  get recentCourses(): any[] {

    return this.courses
      .slice()
      .reverse()
      .slice(0, 4);
  }


  // =========================================================
  // SAYFA GEÇİŞLERİ
  // =========================================================
  goToCourses(): void {

    this.router.navigate([
      '/teacher/courses'
    ]);
  }


  goToCreateCourse(): void {

    this.router.navigate([
      '/teacher/create-course'
    ]);
  }


  goToStudents(): void {

    this.router.navigate([
      '/teacher/students'
    ]);
  }


  goToAnalytics(): void {

    this.router.navigate([
      '/teacher/analytics'
    ]);
  }
}
