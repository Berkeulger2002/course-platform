import {
  Component,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  AdminCourse,
  AdminCourseSummary,
  AdminService
} from '../../services/admin.service';


@Component({
  selector: 'app-admin-courses',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './admin-courses.component.html',

  styleUrl:
    './admin-courses.component.css'
})
export class AdminCoursesComponent
  implements OnInit {


  courses:
    AdminCourse[] = [];


  filteredCourses:
    AdminCourse[] = [];


  summary:
    AdminCourseSummary = {

    totalCourses: 0,

    purchasableCourses: 0,

    closedCourses: 0,

    totalEnrollments: 0
  };


  searchTerm =
    '';


  selectedStatus:
    'ALL'
    | 'OPEN'
    | 'CLOSED' =
    'ALL';


  isLoading =
    true;


  errorMessage =
    '';


  constructor(

    private adminService:
    AdminService,

    private router:
    Router

  ) {
  }


  ngOnInit(): void {


    this.loadCourses();


    this.loadSummary();
  }


  // =========================================================
  // COURSES
  // =========================================================

  loadCourses(): void {


    this.isLoading =
      true;


    this.errorMessage =
      '';


    this.adminService
      .getCourses()
      .subscribe({


        next: (
          courses
        ) => {


          this.courses =
            courses;


          this.applyFilters();


          this.isLoading =
            false;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin courses hatası:',
            error
          );


          this.isLoading =
            false;


          if (
            error?.status === 401
          ) {


            this.router.navigate([
              '/admin/login'
            ]);


            return;
          }


          if (
            error?.status === 403
          ) {


            this.errorMessage =
              'Bu sayfaya erişim yetkiniz bulunmuyor.';


            return;
          }


          this.errorMessage =
            'Kurslar yüklenemedi.';
        }

      });
  }


  // =========================================================
  // SUMMARY
  // =========================================================

  loadSummary(): void {


    this.adminService
      .getCourseSummary()
      .subscribe({


        next: (
          summary
        ) => {


          this.summary =
            summary;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin course summary hatası:',
            error
          );
        }

      });
  }


  // =========================================================
  // FILTER
  // =========================================================

  applyFilters(): void {


    const search =
      this.searchTerm
        .trim()
        .toLowerCase();


    this.filteredCourses =

      this.courses.filter(

        course => {


          const statusMatches =

            this.selectedStatus === 'ALL'
            ||
            (
              this.selectedStatus === 'OPEN'
              &&
              course.purchasable
            )
            ||
            (
              this.selectedStatus === 'CLOSED'
              &&
              !course.purchasable
            );


          const searchMatches =

            !search
            ||
            course.name
              .toLowerCase()
              .includes(
                search
              )
            ||
            course.teacherName
              .toLowerCase()
              .includes(
                search
              )
            ||
            String(
              course.id
            )
              .includes(
                search
              );


          return statusMatches
            &&
            searchMatches;
        }

      );
  }


  // =========================================================
  // CAPACITY
  // =========================================================

  getCapacityText(
    course: AdminCourse
  ): string {


    if (
      course.maxCapacity <= 0
    ) {


      return `${course.currentEnrolled} / Sınırsız`;
    }


    return `${course.currentEnrolled} / ${course.maxCapacity}`;
  }


  // =========================================================
  // PRICE
  // =========================================================

  formatPrice(
    price: number
  ): string {


    if (
      price === null
      ||
      price === undefined
    ) {


      return '-';
    }


    return new Intl.NumberFormat(

      'tr-TR',

      {
        style: 'currency',
        currency: 'TRY'
      }

    ).format(
      price
    );
  }


  refresh(): void {


    this.loadCourses();


    this.loadSummary();
  }
}
