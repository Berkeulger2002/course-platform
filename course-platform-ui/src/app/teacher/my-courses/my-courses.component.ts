import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { finalize, switchMap } from 'rxjs';

import { CourseService } from '../../services/course.service';
import { ImageUploadService } from '../../services/image-upload.service';

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-courses.component.html',
  styleUrl: './my-courses.component.css'
})
export class MyCoursesComponent implements OnInit {

  currentTeacher: any;

  courses: any[] = [];

  isLoading = true;

  togglingCourseId: number | null = null;

  uploadingVideoCourseId: number | null = null;


  constructor(
    private courseService: CourseService,
    private imageUploadService: ImageUploadService,
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


    this.loadCourses();
  }


  // =========================================================
  // KURSLARI GETİR
  // =========================================================
  loadCourses(): void {

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

          this.isLoading =
            false;
        },

        error: (err) => {

          console.error(
            'Öğretmenin kursları yüklenirken hata:',
            err
          );

          this.isLoading =
            false;
        }

      });
  }


  // =========================================================
  // YENİ KURS
  // =========================================================
  createCourse(): void {

    this.router.navigate([
      '/teacher/create-course'
    ]);
  }


  // =========================================================
  // KURS DÜZENLE
  // =========================================================
  editCourse(course: any): void {

    if (!course?.id) {

      return;
    }


    this.router.navigate([
      '/teacher/edit-course',
      course.id
    ]);
  }


  // =========================================================
  // ÖĞRENCİLER
  // =========================================================
  viewStudents(course: any): void {

    if (!course?.id) {

      return;
    }


    this.router.navigate(
      ['/teacher/students'],
      {
        queryParams: {
          courseId: course.id
        }
      }
    );
  }


  // =========================================================
  // SATIŞ DURUMU
  // =========================================================
  isPurchasable(course: any): boolean {

    return (
      course?.purchasable === true ||
      course?.isPurchasable === true
    );
  }


  // =========================================================
  // SATIŞA AÇ / KAPAT
  // =========================================================
  togglePurchasable(course: any): void {

    if (
      !this.currentTeacher?.id ||
      !course?.id
    ) {

      return;
    }


    const newStatus =
      !this.isPurchasable(course);


    const message =
      newStatus
        ? `"${course.name}" kursunu satışa açmak istiyor musunuz?`
        : `"${course.name}" kursunu satışa kapatmak istiyor musunuz?`;


    if (!confirm(message)) {

      return;
    }


    this.togglingCourseId =
      course.id;


    this.courseService
      .setCoursePurchasable(
        this.currentTeacher.id,
        course.id,
        newStatus
      )
      .pipe(

        finalize(() => {

          this.togglingCourseId =
            null;
        })

      )
      .subscribe({

        next: (updatedCourse) => {

          Object.assign(
            course,
            updatedCourse
          );
        },

        error: (err) => {

          console.error(
            'Satış durumu değiştirilemedi:',
            err
          );


          alert(
            err?.error?.message ||
            'Satış durumu değiştirilemedi.'
          );
        }

      });
  }


  // =========================================================
  // VIDEO SEÇ / YÜKLE
  // =========================================================
  onVideoSelected(
    event: Event,
    course: any
  ): void {

    const input =
      event.target as HTMLInputElement;


    if (
      !input.files ||
      input.files.length === 0
    ) {

      return;
    }


    const file =
      input.files[0];


    if (!file.type.startsWith('video/')) {

      alert(
        'Lütfen geçerli bir video dosyası seçin.'
      );

      input.value = '';

      return;
    }


    if (
      !this.currentTeacher?.id ||
      !course?.id
    ) {

      input.value = '';

      return;
    }


    const confirmed =
      confirm(
        `"${course.name}" kursuna "${file.name}" videosunu eklemek istiyor musunuz?`
      );


    if (!confirmed) {

      input.value = '';

      return;
    }


    this.uploadingVideoCourseId =
      course.id;


    /*
     * 1) ImageKit private upload
     * 2) videoPath backend'e kaydedilir
     */
    this.imageUploadService
      .uploadVideo(file)
      .pipe(

        switchMap(
          result =>

            this.courseService
              .updateCourseVideo(
                this.currentTeacher.id,
                course.id,
                result.videoPath
              )
        ),

        finalize(() => {

          this.uploadingVideoCourseId =
            null;

          input.value = '';
        })

      )
      .subscribe({

        next: (updatedCourse) => {

          Object.assign(
            course,
            updatedCourse
          );


          alert(
            'Kurs videosu başarıyla eklendi. 🎉'
          );
        },

        error: (err) => {

          console.error(
            'Video yükleme hatası:',
            err
          );


          alert(
            err?.error?.message ||
            'Video yüklenirken hata oluştu.'
          );
        }

      });
  }


  // =========================================================
  // KAPASİTE YÜZDESİ
  // =========================================================
  getCapacityPercentage(
    course: any
  ): number {

    if (
      !course?.maxCapacity ||
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
}
