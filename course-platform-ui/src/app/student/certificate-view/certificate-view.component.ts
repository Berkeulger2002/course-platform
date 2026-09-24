import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import {
  Certificate,
  CertificateService
} from '../../services/certificate.service';

@Component({
  selector: 'app-certificate-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './certificate-view.component.html',
  styleUrl: './certificate-view.component.css'
})
export class CertificateViewComponent implements OnInit {

  currentStudent: any;

  certificate: Certificate | null = null;

  certificateId!: number;

  isLoading = true;

  errorMessage = '';


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private certificateService: CertificateService
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


    this.certificateId =
      Number(
        this.route.snapshot.paramMap.get('id')
      );


    if (!this.certificateId) {

      this.errorMessage =
        'Geçersiz sertifika.';

      this.isLoading = false;

      return;
    }


    this.loadCertificate();
  }


  loadCertificate(): void {

    this.isLoading = true;


    this.certificateService
      .getCertificates(
        this.currentStudent.id
      )
      .subscribe({

        next: (certificates) => {

          const foundCertificate =
            certificates.find(
              item =>
                item.certificateId ===
                this.certificateId
            );


          if (!foundCertificate) {

            this.errorMessage =
              'Sertifika bulunamadı.';

            this.certificate = null;

          } else {

            this.certificate =
              foundCertificate;
          }


          this.isLoading = false;
        },


        error: (err) => {

          console.error(
            'Sertifika yüklenirken hata:',
            err
          );


          this.errorMessage =
            'Sertifika yüklenemedi.';

          this.isLoading = false;
        }

      });
  }


  printCertificate(): void {

    window.print();
  }


  goBack(): void {

    this.router.navigate([
      '/student/certificates'
    ]);
  }
}
