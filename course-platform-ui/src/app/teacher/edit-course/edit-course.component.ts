import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  forkJoin,
  of
} from 'rxjs';

import { CourseService } from '../../services/course.service';
import { ImageUploadService } from '../../services/image-upload.service';


@Component({
  selector: 'app-edit-course',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl: './edit-course.component.html',
  styleUrl: './edit-course.component.css'
})
export class EditCourseComponent implements OnInit {

  currentTeacher: any;

  courseId!: number;

  isLoading = true;

  isSaving = false;


  course: any = {
    name: '',
    description: '',
    price: null,
    maxCapacity: 0,
    imageUrl: '',
    videoPath: ''
  };


  // =========================================================
  // IMAGE
  // =========================================================

  selectedImageFile:
    File | null = null;

  imagePreview:
    string | null = null;


  // =========================================================
  // VIDEO
  // =========================================================

  selectedVideoFile:
    File | null = null;

  selectedVideoName:
    string | null = null;


  constructor(
    private courseService:
    CourseService,

    private imageUploadService:
    ImageUploadService,

    private route:
    ActivatedRoute,

    private router:
    Router
  ) {}


  ngOnInit(): void {

    const userStr =
      localStorage.getItem(
        'currentUser'
      );


    if (!userStr) {

      this.router.navigate([
        '/auth'
      ]);

      return;
    }


    this.currentTeacher =
      JSON.parse(userStr);


    const id =
      this.route.snapshot
        .paramMap
        .get('id');


    if (!id) {

      this.router.navigate([
        '/teacher/courses'
      ]);

      return;
    }


    this.courseId =
      Number(id);


    this.loadCourse();
  }


  // =========================================================
  // KURSU GETİR
  // =========================================================

  loadCourse(): void {

    this.isLoading = true;


    this.courseService
      .getCourseForTeacher(
        this.currentTeacher.id,
        this.courseId
      )
      .subscribe({

        next: course => {

          this.course = {
            ...course
          };


          this.imagePreview =
            course.imageUrl || null;


          this.isLoading =
            false;
        },


        error: err => {

          console.error(
            'Kurs yüklenemedi:',
            err
          );


          alert(
            err?.error?.message ||
            'Kurs bilgileri alınamadı.'
          );


          this.router.navigate([
            '/teacher/courses'
          ]);
        }

      });
  }


  // =========================================================
  // IMAGE SELECT
  // =========================================================

  onImageSelected(
    event: Event
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


    if (
      !file.type.startsWith(
        'image/'
      )
    ) {

      alert(
        'Lütfen geçerli bir görsel seçin.'
      );

      input.value = '';

      return;
    }


    this.selectedImageFile =
      file;


    const reader =
      new FileReader();


    reader.onload = () => {

      this.imagePreview =
        reader.result as string;
    };


    reader.readAsDataURL(
      file
    );
  }


  // =========================================================
  // VIDEO SELECT
  // =========================================================

  onVideoSelected(
    event: Event
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


    if (
      !file.type.startsWith(
        'video/'
      )
    ) {

      alert(
        'Lütfen geçerli bir video seçin.'
      );

      input.value = '';

      return;
    }


    this.selectedVideoFile =
      file;


    this.selectedVideoName =
      file.name;
  }


  // =========================================================
  // KAYDET
  // =========================================================

  saveCourse(): void {

    if (
      !this.course.name?.trim()
    ) {

      alert(
        'Kurs adı zorunludur.'
      );

      return;
    }


    if (
      Number(this.course.price) < 0
    ) {

      alert(
        'Fiyat negatif olamaz.'
      );

      return;
    }


    if (
      Number(this.course.maxCapacity) < 0
    ) {

      alert(
        'Kapasite negatif olamaz.'
      );

      return;
    }


    this.isSaving =
      true;


    /*
     * Yeni resim seçildiyse yükle.
     * Seçilmediyse mevcut resmi kullan.
     */
    const imageUpload$ =
      this.selectedImageFile

        ? this.imageUploadService
          .uploadImage(
            this.selectedImageFile
          )

        : of({
          url:
            this.course.imageUrl || ''
        });


    /*
     * Yeni video seçildiyse private upload.
     * Seçilmediyse mevcut videoPath korunur.
     */
    const videoUpload$ =
      this.selectedVideoFile

        ? this.imageUploadService
          .uploadVideo(
            this.selectedVideoFile
          )

        : of({
          videoPath:
            this.course.videoPath || ''
        });


    forkJoin({

      image:
      imageUpload$,

      video:
      videoUpload$

    }).subscribe({

      next: result => {

        const updatedCourse = {

          name:
          this.course.name,

          description:
          this.course.description,

          price:
            Number(
              this.course.price
            ),

          maxCapacity:
            Number(
              this.course.maxCapacity
            ),

          imageUrl:
          result.image.url,

          videoPath:
          result.video.videoPath

        };


        this.courseService
          .updateCourseForTeacher(

            this.currentTeacher.id,

            this.courseId,

            updatedCourse

          )
          .subscribe({

            next: () => {

              this.isSaving =
                false;


              alert(
                'Kurs başarıyla güncellendi. ✅'
              );


              this.router.navigate([
                '/teacher/courses'
              ]);
            },


            error: err => {

              this.isSaving =
                false;


              console.error(
                'Kurs güncelleme hatası:',
                err
              );


              alert(
                err?.error?.message ||
                'Kurs güncellenemedi.'
              );
            }

          });
      },


      error: err => {

        this.isSaving =
          false;


        console.error(
          'Dosya yükleme hatası:',
          err
        );


        alert(
          'Görsel veya video yüklenirken hata oluştu.'
        );
      }

    });
  }


  // =========================================================
  // GERİ
  // =========================================================

  cancel(): void {

    this.router.navigate([
      '/teacher/courses'
    ]);
  }
}
