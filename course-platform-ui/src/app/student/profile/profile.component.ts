import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  StudentProfile,
  StudentProfileService
} from '../../services/student-profile.service';


@Component({
  selector: 'app-profile',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {

  currentStudent: any;

  profile: StudentProfile | null = null;

  formName = '';

  formEmail = '';

  isLoading = true;

  isSaving = false;

  isEditing = false;

  successMessage = '';

  errorMessage = '';


  constructor(
    private studentProfileService: StudentProfileService,
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


    this.loadProfile();
  }


  // =========================================================
  // PROFİLİ GETİR
  // =========================================================
  loadProfile(): void {

    this.isLoading = true;


    this.studentProfileService
      .getProfile(
        this.currentStudent.id
      )
      .subscribe({

        next: (profile) => {

          this.profile =
            profile;


          this.formName =
            profile.name;


          this.formEmail =
            profile.email;


          this.isLoading = false;
        },


        error: (err) => {

          console.error(
            'Profil yüklenirken hata:',
            err
          );


          this.errorMessage =
            'Profil bilgileri yüklenemedi.';


          this.isLoading = false;
        }

      });
  }


  // =========================================================
  // DÜZENLEMEYİ AÇ
  // =========================================================
  startEditing(): void {

    if (!this.profile) {
      return;
    }


    this.formName =
      this.profile.name;


    this.formEmail =
      this.profile.email;


    this.successMessage = '';

    this.errorMessage = '';

    this.isEditing = true;
  }


  // =========================================================
  // İPTAL
  // =========================================================
  cancelEditing(): void {

    this.isEditing = false;

    this.successMessage = '';

    this.errorMessage = '';
  }


  // =========================================================
  // PROFİLİ KAYDET
  // =========================================================
  saveProfile(): void {

    if (
      !this.formName.trim() ||
      !this.formEmail.trim()
    ) {

      this.errorMessage =
        'Ad ve e-posta boş bırakılamaz.';

      return;
    }


    this.isSaving = true;

    this.successMessage = '';

    this.errorMessage = '';


    this.studentProfileService
      .updateProfile(

        this.currentStudent.id,

        this.formName,

        this.formEmail

      )
      .subscribe({

        next: (updatedProfile) => {

          this.profile =
            updatedProfile;


          /*
           * localStorage içindeki kullanıcı adını da güncelle.
           */
          const updatedCurrentUser = {

            ...this.currentStudent,

            name:
            updatedProfile.name,

            email:
            updatedProfile.email

          };


          localStorage.setItem(
            'currentUser',
            JSON.stringify(
              updatedCurrentUser
            )
          );


          this.currentStudent =
            updatedCurrentUser;


          this.isEditing = false;

          this.isSaving = false;


          this.successMessage =
            'Profil bilgileriniz başarıyla güncellendi.';
        },


        error: (err) => {

          console.error(
            'Profil güncellenirken hata:',
            err
          );


          this.errorMessage =
            err?.error?.message ||
            err?.error ||
            'Profil güncellenemedi.';


          this.isSaving = false;
        }

      });
  }


  // =========================================================
  // İSİM BAŞ HARFLERİ
  // =========================================================
  get initials(): string {

    if (!this.profile?.name) {

      return '👤';
    }


    return this.profile.name
      .split(' ')
      .filter(
        part =>
          part.length > 0
      )
      .slice(0, 2)
      .map(
        part =>
          part
            .charAt(0)
            .toUpperCase()
      )
      .join('');
  }
}
