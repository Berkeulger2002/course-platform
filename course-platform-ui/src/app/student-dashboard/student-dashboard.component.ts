import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { CourseService } from '../services/course.service';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './student-dashboard.component.html',
  styleUrl: './student-dashboard.component.css'
})
export class StudentDashboardComponent implements OnInit {

  currentStudent: any;

  myCourses: any[] = [];
  availableCourses: any[] = [];

  totalMyCourses = 0;
  totalAvailableCourses = 0;

  isLoading = true;


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

    this.currentStudent =
      JSON.parse(userStr);

    this.loadDashboard();
  }


  // =========================================================
  // DASHBOARD VERİLERİ
  // =========================================================
  loadDashboard(): void {

    this.isLoading = true;

    forkJoin({

      enrolledCourses:
        this.courseService.getEnrolledCourses(
          this.currentStudent.id
        ),

      allCourses:
        this.courseService.getAllCourses()

    }).subscribe({

      next: (result: any) => {

        this.myCourses =
          result.enrolledCourses || [];


        const myCourseIds =
          this.myCourses.map(
            course => course.id
          );


        this.availableCourses =
          (result.allCourses || [])
            .filter(
              (course: any) =>
                !myCourseIds.includes(course.id)
            );


        this.totalMyCourses =
          this.myCourses.length;

        this.totalAvailableCourses =
          this.availableCourses.length;


        this.isLoading = false;
      },

      error: (err) => {

        this.isLoading = false;

        console.error(
          'Dashboard yüklenirken hata:',
          err
        );
      }

    });
  }


  // =========================================================
  // SAYFA YÖNLENDİRMELERİ
  // =========================================================
  goToMyCourses(): void {

    this.router.navigate([
      '/student/courses'
    ]);
  }


  goToMarketplace(): void {

    this.router.navigate([
      '/student/marketplace'
    ]);
  }


  goToProgress(): void {

    this.router.navigate([
      '/student/progress'
    ]);
  }


  goToCertificates(): void {

    this.router.navigate([
      '/student/certificates'
    ]);
  }


  goToNotifications(): void {

    this.router.navigate([
      '/student/notifications'
    ]);
  }


  goToProfile(): void {

    this.router.navigate([
      '/student/profile'
    ]);
  }
}
