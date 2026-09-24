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
  CourseService
} from '../../services/course.service';

import {
  TeacherRevenueRecord,
  TeacherRevenueService
} from '../../services/teacher-revenue.service';


@Component({
  selector: 'app-revenue',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl: './revenue.component.html',

  styleUrl: './revenue.component.css'
})
export class RevenueComponent implements OnInit {

  currentTeacher: any;


  payments:
    TeacherRevenueRecord[] = [];


  courses: any[] = [];


  isLoading = true;


  // =========================================================
  // FILTER
  // =========================================================

  selectedCourseId:
    number | null = null;


  selectedStatus =
    'ALL';


  constructor(
    private revenueService:
    TeacherRevenueService,

    private courseService:
    CourseService,

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


    this.loadData();
  }


  // =========================================================
  // VERİLERİ GETİR
  // =========================================================

  loadData(): void {

    this.isLoading = true;


    this.courseService
      .getAllCoursesForTeacher(
        this.currentTeacher.id
      )
      .subscribe({

        next: courses => {

          this.courses =
            courses || [];


          this.loadPayments();
        },


        error: err => {

          console.error(
            'Kurslar yüklenirken hata:',
            err
          );


          this.isLoading =
            false;
        }

      });
  }


  loadPayments(): void {

    this.revenueService
      .getTeacherPayments(
        this.currentTeacher.id
      )
      .subscribe({

        next: payments => {

          this.payments =
            payments || [];


          this.isLoading =
            false;
        },


        error: err => {

          console.error(
            'Satış kayıtları yüklenirken hata:',
            err
          );


          this.isLoading =
            false;
        }

      });
  }


  // =========================================================
  // TAMAMLANMIŞ SATIŞLAR
  // =========================================================

  get completedPayments():
    TeacherRevenueRecord[] {

    return this.payments
      .filter(
        payment =>
          payment.status ===
          'COMPLETED'
      );
  }


  // =========================================================
  // TOPLAM GELİR
  // =========================================================

  get totalRevenue(): number {

    return this.completedPayments
      .reduce(
        (
          total,
          payment
        ) =>
          total +
          Number(
            payment.amount
          ),

        0
      );
  }


  // =========================================================
  // SATIŞ SAYISI
  // =========================================================

  get totalSales(): number {

    return this.completedPayments
      .length;
  }


  // =========================================================
  // SATILAN FARKLI KURS SAYISI
  // =========================================================

  get soldCourseCount(): number {

    return new Set(
      this.completedPayments
        .map(
          payment =>
            payment.courseId
        )
    ).size;
  }


  // =========================================================
  // BENZERSİZ MÜŞTERİ
  // =========================================================

  get customerCount(): number {

    return new Set(
      this.completedPayments
        .map(
          payment =>
            payment.studentId
        )
    ).size;
  }


  // =========================================================
  // ORTALAMA SATIŞ
  // =========================================================

  get averageSale(): number {

    if (
      this.totalSales === 0
    ) {

      return 0;
    }


    return (
      this.totalRevenue /
      this.totalSales
    );
  }


  // =========================================================
  // FİLTRELENMİŞ KAYITLAR
  // =========================================================

  get filteredPayments():
    TeacherRevenueRecord[] {

    return this.payments
      .filter(payment => {

        const courseMatches =

          !this.selectedCourseId

          ||

          payment.courseId ===
          Number(
            this.selectedCourseId
          );


        const statusMatches =

          this.selectedStatus ===
          'ALL'

          ||

          payment.status ===
          this.selectedStatus;


        return (
          courseMatches &&
          statusMatches
        );
      });
  }


  // =========================================================
  // KURS BAZLI GELİR
  // =========================================================

  get courseRevenueSummary():
    any[] {

    const map =
      new Map<
        number,
        {
          courseId: number;
          courseName: string;
          revenue: number;
          sales: number;
        }
      >();


    this.completedPayments
      .forEach(payment => {

        const existing =
          map.get(
            payment.courseId
          );


        if (existing) {

          existing.revenue +=
            Number(
              payment.amount
            );


          existing.sales +=
            1;

        } else {

          map.set(
            payment.courseId,

            {
              courseId:
              payment.courseId,

              courseName:
              payment.courseName,

              revenue:
                Number(
                  payment.amount
                ),

              sales:
                1
            }
          );
        }

      });


    return Array.from(
      map.values()
    )
      .sort(
        (a, b) =>
          b.revenue -
          a.revenue
      );
  }


  // =========================================================
  // EN YÜKSEK GELİR PAYI
  // =========================================================

  getRevenuePercentage(
    revenue: number
  ): number {

    if (
      this.totalRevenue <= 0
    ) {

      return 0;
    }


    return Math.min(
      (
        revenue /
        this.totalRevenue
      ) * 100,

      100
    );
  }


  // =========================================================
  // PARA FORMAT
  // =========================================================

  formatMoney(
    amount: number
  ): string {

    return new Intl
      .NumberFormat(
        'tr-TR',
        {
          style: 'currency',
          currency: 'TRY'
        }
      )
      .format(
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
  // ÖDEME YÖNTEMİ
  // =========================================================

  getPaymentMethodText(
    method: string
  ): string {

    switch (method) {

      case 'PLATFORM':
        return 'Platform Ödemesi';

      case 'CARD':
        return 'Banka / Kredi Kartı';

      case 'FREE':
        return 'Ücretsiz';

      default:
        return method;
    }
  }


  // =========================================================
  // TRANSACTION CODE COPY
  // =========================================================

  copyTransactionCode(
    code: string
  ): void {

    navigator.clipboard
      .writeText(code)
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
