import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { forkJoin, of } from 'rxjs';

import { CourseService } from '../../services/course.service';
import { ImageUploadService } from '../../services/image-upload.service';

@Component({
  selector: 'app-create-course',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './create-course.component.html',
  styleUrl: './create-course.component.css'
})
export class CreateCourseComponent implements OnInit {

  currentTeacher: any;


  // =========================================================
  // KAPAK GÖRSELİ
  // =========================================================
  selectedImageFile: File | null = null;

  imagePreview: string | null = null;


  // =========================================================
  // KURS VİDEOSU
  // =========================================================
  selectedVideoFile: File | null = null;

  selectedVideoName: string | null = null;


  isUploading = false;


  newCourse = {

    name: '',

    description: '',

    price: null,

    imageUrl: '',

    videoPath: ''

  };


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
  }


  // =========================================================
  // GÖRSEL SEÇ
  // =========================================================
  onImageSelected(event: Event): void {

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
      !file.type.startsWith('image/')
    ) {

      alert(
        'Lütfen geçerli bir görsel dosyası seçin.'
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
  // VIDEO SEÇ
  // =========================================================
  onVideoSelected(event: Event): void {

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
      !file.type.startsWith('video/')
    ) {

      alert(
        'Lütfen geçerli bir video dosyası seçin.'
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
  // KURSU YAYINLA
  // =========================================================
  addCourse(): void {

    if (
      !this.currentTeacher ||
      !this.currentTeacher.id
    ) {

      alert(
        'Öğretmen kimliği bulunamadı. Lütfen tekrar giriş yapın.'
      );

      return;
    }


    this.isUploading = true;


    const imageUpload$ =
      this.selectedImageFile

        ? this.imageUploadService
          .uploadImage(
            this.selectedImageFile
          )

        : of({
          url: ''
        });


    const videoUpload$ =
      this.selectedVideoFile

        ? this.imageUploadService
          .uploadVideo(
            this.selectedVideoFile
          )

        : of({
          videoPath: ''
        });


    forkJoin({

      image: imageUpload$,

      video: videoUpload$

    }).subscribe({

      next: (result) => {

        this.newCourse.imageUrl =
          result.image.url;


        this.newCourse.videoPath =
          result.video.videoPath;


        this.createCourse();
      },


      error: (err) => {

        this.isUploading =
          false;


        console.error(
          'ImageKit dosya yükleme hatası:',
          err
        );


        alert(
          'Kurs görseli veya videosu yüklenirken bir hata oluştu.'
        );
      }

    });
  }


  // =========================================================
  // BACKEND'E KAYDET
  // =========================================================
  private createCourse(): void {

    this.courseService
      .createCourseForTeacher(
        this.currentTeacher.id,
        this.newCourse
      )
      .subscribe({

        next: () => {

          this.isUploading =
            false;


          alert(
            'Tebrikler! Kurs başarıyla yayınlandı. 🎉'
          );


          this.resetForm();


          /*
           * Kurs oluşturulduktan sonra
           * öğretmeni Kurslarım sayfasına gönderiyoruz.
           */
          this.router.navigate([
            '/teacher/courses'
          ]);
        },


        error: (err) => {

          this.isUploading =
            false;


          console.error(
            'Kurs ekleme hatası:',
            err
          );


          alert(
            'Kurs eklenirken bir hata oluştu.'
          );
        }

      });
  }


  // =========================================================
  // FORMU TEMİZLE
  // =========================================================
  private resetForm(): void {

    this.newCourse = {

      name: '',

      description: '',

      price: null,

      imageUrl: '',

      videoPath: ''

    };


    this.selectedImageFile =
      null;


    this.imagePreview =
      null;


    this.selectedVideoFile =
      null;


    this.selectedVideoName =
      null;
  }


  // =========================================================
  // İPTAL
  // =========================================================
  cancel(): void {

    this.router.navigate([
      '/teacher/dashboard'
    ]);
  }
}
