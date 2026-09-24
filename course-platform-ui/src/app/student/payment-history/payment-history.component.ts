import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import {
  PaymentHistoryRecord,
  PaymentHistoryService
} from '../../services/payment-history.service';


@Component({
  selector: 'app-payment-history',
  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl: './payment-history.component.html',
  styleUrl: './payment-history.component.css'
})
export class PaymentHistoryComponent implements OnInit {

  currentStudent: any;

  payments: PaymentHistoryRecord[] = [];

  isLoading = true;

  totalSpent = 0;


  constructor(
    private paymentHistoryService: PaymentHistoryService,
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


    this.loadPaymentHistory();
  }


  // =========================================================
  // ÖDEME GEÇMİŞİNİ GETİR
  // =========================================================
  loadPaymentHistory(): void {

    this.isLoading = true;


    this.paymentHistoryService
      .getStudentPaymentHistory(
        this.currentStudent.id
      )
      .subscribe({

        next: (payments) => {

          this.payments =
            payments;


          this.calculateTotalSpent();


          this.isLoading = false;
        },


        error: (err) => {

          console.error(
            'Ödeme geçmişi yüklenirken hata:',
            err
          );


          this.isLoading = false;
        }

      });
  }


  // =========================================================
  // TOPLAM HARCAMA
  // =========================================================
  calculateTotalSpent(): void {

    this.totalSpent =
      this.payments
        .filter(
          payment =>
            payment.status === 'COMPLETED'
        )
        .reduce(
          (total, payment) =>
            total + Number(payment.amount),
          0
        );
  }


  // =========================================================
  // BAŞARILI İŞLEM SAYISI
  // =========================================================
  get completedPaymentCount(): number {

    return this.payments
      .filter(
        payment =>
          payment.status === 'COMPLETED'
      )
      .length;
  }


  // =========================================================
  // PARA FORMATLA
  // =========================================================
  formatMoney(
    amount: number
  ): string {

    return new Intl.NumberFormat(
      'tr-TR',
      {
        style: 'currency',
        currency: 'TRY'
      }
    ).format(
      Number(amount)
    );
  }


  // =========================================================
  // DURUM YAZISI
  // =========================================================
  getStatusText(
    status: string
  ): string {

    switch (status) {

      case 'COMPLETED':
        return 'Tamamlandı';

      case 'PENDING':
        return 'Bekliyor';

      case 'FAILED':
        return 'Başarısız';

      case 'REFUNDED':
        return 'İade Edildi';

      default:
        return status;
    }
  }


  // =========================================================
  // ÖDEME YÖNTEMİ YAZISI
  // =========================================================
  getPaymentMethodText(
    paymentMethod: string
  ): string {

    switch (paymentMethod) {

      case 'PLATFORM':
        return 'Platform Ödemesi';

      case 'CARD':
        return 'Banka / Kredi Kartı';

      case 'FREE':
        return 'Ücretsiz';

      default:
        return paymentMethod;
    }
  }


  // =========================================================
  // MAĞAZAYA GİT
  // =========================================================
  goToMarketplace(): void {

    this.router.navigate([
      '/student/marketplace'
    ]);
  }


  // =========================================================
  // İŞLEM KODUNU KOPYALA
  // =========================================================
  copyTransactionCode(
    transactionCode: string
  ): void {

    navigator.clipboard
      .writeText(transactionCode)
      .then(() => {

        alert(
          'İşlem kodu kopyalandı.'
        );

      })
      .catch(err => {

        console.error(
          'İşlem kodu kopyalanamadı:',
          err
        );
      });
  }
}
