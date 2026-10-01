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
  AdminOrder,
  AdminOrderSummary,
  AdminOrdersService
} from './admin-orders.service';


@Component({
  selector: 'app-admin-orders',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './admin-orders.component.html',

  styleUrl:
    './admin-orders.component.css'
})
export class AdminOrdersComponent
  implements OnInit {


  // =========================================================
  // DATA
  // =========================================================

  orders:
    AdminOrder[] = [];


  filteredOrders:
    AdminOrder[] = [];


  summary:
    AdminOrderSummary = {

    totalOrders: 0,

    totalRevenue: 0,

    totalItemsSold: 0,

    averageOrderValue: 0
  };


  // =========================================================
  // SEARCH
  // =========================================================

  searchTerm =
    '';


  // =========================================================
  // DETAIL
  // =========================================================

  expandedOrderId:
    number | null =
    null;


  // =========================================================
  // STATE
  // =========================================================

  isLoading =
    true;


  errorMessage =
    '';


  constructor(

    private adminOrdersService:
    AdminOrdersService,

    private router:
    Router

  ) {
  }


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {


    this.loadOrders();


    this.loadSummary();
  }


  // =========================================================
  // LOAD ORDERS
  // =========================================================

  loadOrders(): void {


    this.isLoading =
      true;


    this.errorMessage =
      '';


    this.adminOrdersService
      .getOrders()
      .subscribe({


        next: (
          orders
        ) => {


          this.orders =
            orders;


          this.applyFilter();


          this.isLoading =
            false;
        },


        error: (
          error
        ) => {


          console.error(
            'Admin orders hatası:',
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
            'Siparişler yüklenemedi.';
        }

      });
  }


  // =========================================================
  // SUMMARY
  // =========================================================

  loadSummary(): void {


    this.adminOrdersService
      .getSummary()
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
            'Admin order summary hatası:',
            error
          );
        }

      });
  }


  // =========================================================
  // SEARCH FILTER
  // =========================================================

  applyFilter(): void {


    const search =
      this.searchTerm
        .trim()
        .toLowerCase();


    this.filteredOrders =

      this.orders.filter(

        order => {


          if (
            !search
          ) {

            return true;
          }


          const orderCode =
            order.orderCode
              ?.toLowerCase()
            ??
            '';


          const studentName =
            order.studentName
              ?.toLowerCase()
            ??
            '';


          const studentEmail =
            order.studentEmail
              ?.toLowerCase()
            ??
            '';


          const id =
            String(
              order.id
            );


          const courseMatch =

            order.items
              ?.some(

                item =>

                  item.courseName
                    ?.toLowerCase()
                    .includes(
                      search
                    )

              )
            ??
            false;


          return (

            orderCode.includes(
              search
            )

            ||

            studentName.includes(
              search
            )

            ||

            studentEmail.includes(
              search
            )

            ||

            id.includes(
              search
            )

            ||

            courseMatch

          );
        }

      );
  }


  // =========================================================
  // DETAIL TOGGLE
  // =========================================================

  toggleOrderDetail(
    orderId: number
  ): void {


    if (
      this.expandedOrderId ===
      orderId
    ) {


      this.expandedOrderId =
        null;


      return;
    }


    this.expandedOrderId =
      orderId;
  }


  // =========================================================
  // CHECK EXPANDED
  // =========================================================

  isExpanded(
    orderId: number
  ): boolean {


    return this.expandedOrderId ===
      orderId;
  }


  // =========================================================
  // MONEY
  // =========================================================

  formatMoney(
    amount: number | null | undefined
  ): string {


    const safeAmount =
      amount ?? 0;


    return new Intl.NumberFormat(

      'tr-TR',

      {
        style: 'currency',
        currency: 'TRY'
      }

    ).format(
      safeAmount
    );
  }


  // =========================================================
  // REFRESH
  // =========================================================

  refresh(): void {


    this.expandedOrderId =
      null;


    this.loadOrders();


    this.loadSummary();
  }
}
