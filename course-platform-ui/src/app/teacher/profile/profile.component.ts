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
  TeacherProfile,
  TeacherService
} from '../../services/teacher.service';


@Component({
  selector: 'app-profile',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './profile.component.html',

  styleUrl:
    './profile.component.css'
})
export class ProfileComponent
  implements OnInit {

  currentTeacher: any;


  profile:
    TeacherProfile = {

    id: 0,

    name: '',

    email: '',

    role: 'TEACHER'
  };


  originalProfile:
    TeacherProfile | null = null;


  isLoading = true;

  isSaving = false;


  errorMessage = '';

  successMessage = '';


  constructor(
    private teacherService:
    TeacherService,

    private router:
    Router
  ) {}


  // =========================================================
  // INIT
  // =========================================================

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
      JSON.parse(
        userStr
      );


    if (
      !this.currentTeacher?.id
    ) {

      this.router.navigate([
        '/auth'
      ]);

      return;
    }


    this.loadProfile();
  }


  // =========================================================
  // PROFİLİ GETİR
  // =========================================================

  loadProfile(): void {

    this.isLoading = true;

    this.errorMessage = '';

    this.successMessage = '';


    this.teacherService
      .getProfile(
        this.currentTeacher.id
      )
      .subscribe({

        next: profile => {

          this.profile = {
            ...profile
          };


          this.originalProfile = {
            ...profile
          };


          this.isLoading =
            false;
        },


        error: err => {

          console.error(
            'Öğretmen profili alınamadı:',
            err
          );


          this.errorMessage =
            err?.error?.message
            ||
            'Profil bilgileri yüklenirken bir hata oluştu.';


          this.isLoading =
            false;
        }

      });
  }


  // =========================================================
  // PROFİL KAYDET
  // =========================================================

  saveProfile(): void {

    this.errorMessage = '';

    this.successMessage = '';


    const name =
      this.profile.name
        ?.trim();


    const email =
      this.profile.email
        ?.trim()
        .toLowerCase();


    // =====================================================
    // AD KONTROLÜ
    // =====================================================

    if (!name) {

      this.errorMessage =
        'Ad soyad alanı boş bırakılamaz.';

      return;
    }


    if (
      name.length > 100
    ) {

      this.errorMessage =
        'Ad soyad en fazla 100 karakter olabilir.';

      return;
    }


    // =====================================================
    // EMAIL KONTROLÜ
    // =====================================================

    if (!email) {

      this.errorMessage =
        'E-posta alanı boş bırakılamaz.';

      return;
    }


    const emailRegex =
      /^[^\s@]+@[^\s@]+\.[^\s@]+$/;


    if (
      !emailRegex.test(email)
    ) {

      this.errorMessage =
        'Geçerli bir e-posta adresi giriniz.';

      return;
    }


    // =====================================================
    // API
    // =====================================================

    this.isSaving =
      true;


    this.teacherService
      .updateProfile(
        this.currentTeacher.id,
        {
          name,
          email
        }
      )
      .subscribe({

        next:
          updatedProfile => {

            this.profile = {
              ...updatedProfile
            };


            this.originalProfile = {
              ...updatedProfile
            };


            // =============================================
            // LOCAL STORAGE DA GÜNCELLE
            // =============================================

            const currentUserStr =
              localStorage.getItem(
                'currentUser'
              );


            if (currentUserStr) {

              const currentUser =
                JSON.parse(
                  currentUserStr
                );


              const updatedUser = {

                ...currentUser,

                name:
                updatedProfile.name,

                email:
                updatedProfile.email

              };


              localStorage.setItem(
                'currentUser',
                JSON.stringify(
                  updatedUser
                )
              );


              this.currentTeacher =
                updatedUser;
            }


            this.successMessage =
              'Profil bilgileriniz başarıyla güncellendi.';


            this.isSaving =
              false;
          },


        error: err => {

          console.error(
            'Profil güncellenirken hata:',
            err
          );


          this.errorMessage =
            err?.error?.message
            ||
            'Profil güncellenirken bir hata oluştu.';


          this.isSaving =
            false;
        }

      });
  }


  // =========================================================
  // DEĞİŞİKLİKLERİ GERİ AL
  // =========================================================

  resetForm(): void {

    if (
      !this.originalProfile
    ) {

      return;
    }


    this.profile = {
      ...this.originalProfile
    };


    this.errorMessage = '';

    this.successMessage = '';
  }


  // =========================================================
  // FORM DEĞİŞTİ Mİ?
  // =========================================================

  get hasChanges(): boolean {

    if (
      !this.originalProfile
    ) {

      return false;
    }


    return (

      this.profile.name.trim()
      !==
      this.originalProfile.name.trim()

      ||

      this.profile.email.trim()
        .toLowerCase()
      !==
      this.originalProfile.email.trim()
        .toLowerCase()

    );
  }


  // =========================================================
  // İSİM BAŞ HARFLERİ
  // =========================================================

  get initials(): string {

    const name =
      this.profile.name
        ?.trim();


    if (!name) {

      return 'Ö';
    }


    const parts =
      name
        .split(/\s+/)
        .filter(Boolean);


    if (
      parts.length === 1
    ) {

      return parts[0]
        .substring(
          0,
          2
        )
        .toUpperCase();
    }


    return (
      parts[0][0]
      +
      parts[
      parts.length - 1
        ][0]
    )
      .toUpperCase();
  }


  // =========================================================
  // ROLE TEXT
  // =========================================================

  get roleText(): string {

    if (
      this.profile.role ===
      'TEACHER'
    ) {

      return 'Öğretmen';
    }


    return this.profile.role
      ||
      'Öğretmen';
  }
}
