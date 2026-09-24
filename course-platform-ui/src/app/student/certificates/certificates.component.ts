import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import {
  Certificate,
  CertificateService
} from '../../services/certificate.service';

@Component({
  selector: 'app-certificates',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './certificates.component.html',
  styleUrl: './certificates.component.css'
})
export class CertificatesComponent implements OnInit {

  currentStudent: any;

  certificates: Certificate[] = [];

  isLoading = true;


  constructor(
    private certificateService: CertificateService,
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


    this.loadCertificates();
  }


  // =========================================================
  // SERTİFİKALARI GETİR
  // =========================================================
  loadCertificates(): void {

    this.isLoading = true;


    this.certificateService
      .getCertificates(
        this.currentStudent.id
      )
      .subscribe({

        next: (certificates) => {

          this.certificates =
            certificates;

          this.isLoading = false;
        },


        error: (err) => {

          this.isLoading = false;

          console.error(
            'Sertifikalar yüklenirken hata:',
            err
          );
        }

      });
  }


  // =========================================================
  // KURSLARIMA GİT
  // =========================================================
  goToCourses(): void {

    this.router.navigate([
      '/student/courses'
    ]);
  }


  // =========================================================
  // SERTİFİKAYI GÖRÜNTÜLE
  // =========================================================
  viewCertificate(
    certificateId: number
  ): void {

    this.router.navigate([
      '/student/certificate',
      certificateId
    ]);
  }


  // =========================================================
  // SERTİFİKA KODUNU KOPYALA
  // =========================================================
  copyCertificateCode(
    code: string
  ): void {

    navigator.clipboard
      .writeText(code)
      .then(() => {

        alert(
          'Sertifika kodu kopyalandı.'
        );

      })
      .catch(err => {

        console.error(
          'Sertifika kodu kopyalanamadı:',
          err
        );
      });
  }
}
