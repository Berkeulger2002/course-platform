import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import {
  forkJoin
} from 'rxjs';

import { CourseService } from '../../services/course.service';
import { FavoriteService } from '../../services/favorite.service';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';


@Component({
  selector: 'app-marketplace',
  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl: './marketplace.component.html',
  styleUrl: './marketplace.component.css'
})
export class MarketplaceComponent implements OnInit {

  currentStudent: any;

  allCourses: any[] = [];


  // =========================================================
  // GERÇEK BACKEND SEPETİ
  //
  // Artık geçici frontend sepeti değildir.
  //
  // Backend'deki:
  //
  // Cart
  //   └── CartItem
  //
  // kayıtlarının frontend için sadeleştirilmiş halidir.
  // =========================================================

  cart: any[] = [];


  // =========================================================
  // BACKEND'DEN GELEN GERÇEK SEPET TOPLAMI
  // =========================================================

  cartTotal = 0;


  isLoading = true;

  isCheckingOut = false;


  constructor(
    private courseService: CourseService,
    private favoriteService: FavoriteService,
    private cartService: CartService,
    private orderService: OrderService,
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


    this.loadMarketplace();
  }


  // =========================================================
  // MAĞAZAYI YÜKLE
  //
  // Artık mağaza açılırken:
  //
  // - kurslar
  // - öğrencinin aldığı kurslar
  // - favoriler
  // - GERÇEK BACKEND SEPETİ
  //
  // birlikte yüklenir.
  // =========================================================

  loadMarketplace(): void {

    this.isLoading = true;


    forkJoin({

      allCourses:
        this.courseService
          .getAllCourses(),

      enrolledCourses:
        this.courseService
          .getEnrolledCourses(
            this.currentStudent.id
          ),

      favoriteCourses:
        this.favoriteService
          .getFavorites(
            this.currentStudent.id
          ),

      cart:
        this.cartService
          .getCart(
            this.currentStudent.id
          )

    }).subscribe({

      next: (result: any) => {


        // =====================================================
        // BACKEND SEPETİNİ FRONTEND'E AKTAR
        // =====================================================

        this.syncCartFromBackend(
          result.cart
        );


        // =====================================================
        // SATIN ALINMIŞ KURS ID'LERİ
        // =====================================================

        const enrolledIds =
          result.enrolledCourses
            .map(
              (course: any) =>
                course.id
            );


        // =====================================================
        // FAVORİ KURS ID'LERİ
        // =====================================================

        const favoriteIds =
          result.favoriteCourses
            .map(
              (course: any) =>
                course.id
            );


        // =====================================================
        // MAĞAZADA GÖSTERİLECEK KURSLAR
        // =====================================================

        this.allCourses =
          result.allCourses

            .filter(
              (course: any) =>
                !enrolledIds
                  .includes(course.id)
            )

            .map(
              (course: any) => ({

                ...course,

                showReviews: false,

                reviews: [],

                isFavorite:
                  favoriteIds
                    .includes(course.id)

              })
            );


        this.isLoading = false;
      },


      error: (err) => {

        this.isLoading = false;


        console.error(
          'Kurs mağazası yüklenirken hata:',
          err
        );
      }

    });
  }


  // =========================================================
  // BACKEND CART -> FRONTEND CART
  //
  // Backend:
  //
  // Cart
  //   items:
  //     CartItem
  //       id
  //       priceAtAddition
  //       course
  //
  // HTML ise:
  //
  // item.id
  // item.name
  // item.price
  //
  // bekliyor.
  //
  // Burada iki yapıyı birbirine dönüştürüyoruz.
  // =========================================================

  private syncCartFromBackend(
    backendCart: any
  ): void {


    const backendItems =
      Array.isArray(
        backendCart?.items
      )
        ? backendCart.items
        : [];


    this.cart =
      backendItems.map(
        (item: any) => ({

          // CartItem ID
          //
          // Silme işleminde bunu kullanacağız.
          id:
          item.id,

          // Course ID
          //
          // isInCart kontrolünde bunu kullanacağız.
          courseId:
          item.course?.id,

          name:
            item.course?.name
            ?? 'Kurs',

          price:
            Number(
              item.priceAtAddition
              ??
              item.course?.price
              ??
              0
            )

        })
      );


    // Backend Cart.totalPrice gerçek kaynak.
    this.cartTotal =
      Number(
        backendCart?.totalPrice
        ??
        0
      );
  }


  // =========================================================
  // FAVORİ AÇ / KAPAT
  // =========================================================

  toggleFavorite(
    course: any
  ): void {

    if (course.isFavorite) {

      this.favoriteService
        .removeFavorite(
          this.currentStudent.id,
          course.id
        )
        .subscribe({

          next: () => {

            course.isFavorite = false;
          },


          error: (err) => {

            console.error(
              'Favoriden çıkarma hatası:',
              err
            );
          }

        });

    } else {

      this.favoriteService
        .addFavorite(
          this.currentStudent.id,
          course.id
        )
        .subscribe({

          next: () => {

            course.isFavorite = true;
          },


          error: (err) => {

            console.error(
              'Favoriye ekleme hatası:',
              err
            );
          }

        });
    }
  }


  // =========================================================
  // KURSU GERÇEK BACKEND SEPETİNE EKLE
  //
  // ESKİ:
  //
  // this.cart.push(course)
  //
  // YENİ:
  //
  // POST -> Spring Boot -> CartService -> CartItem -> PostgreSQL
  // =========================================================

  addToCart(
    course: any
  ): void {


    if (
      this.isInCart(
        course.id
      )
    ) {

      return;
    }


    this.cartService
      .addCourseToCart(
        this.currentStudent.id,
        course.id
      )
      .subscribe({

        next: (backendCart) => {


          // Backend ne döndürdüyse
          // frontend sepetini onunla güncelle.
          this.syncCartFromBackend(
            backendCart
          );
        },


        error: (err) => {

          console.error(
            'Sepete ekleme hatası:',
            err
          );


          const backendMessage =
            err?.error?.message;


          alert(
            backendMessage
            ||
            'Kurs sepete eklenirken bir sorun oluştu.'
          );
        }

      });
  }


  // =========================================================
  // GERÇEK BACKEND SEPETİNDEN CART ITEM SİL
  //
  // Buraya Course ID değil,
  // CartItem ID gelir.
  // =========================================================

  removeFromCart(
    cartItemId: number
  ): void {


    this.cartService
      .removeCartItem(
        this.currentStudent.id,
        cartItemId
      )
      .subscribe({

        next: (backendCart) => {


          this.syncCartFromBackend(
            backendCart
          );
        },


        error: (err) => {

          console.error(
            'Sepetten çıkarma hatası:',
            err
          );


          const backendMessage =
            err?.error?.message;


          alert(
            backendMessage
            ||
            'Kurs sepetten çıkarılırken bir sorun oluştu.'
          );
        }

      });
  }


  // =========================================================
  // KURS GERÇEK BACKEND SEPETİNDE Mİ?
  // =========================================================

  isInCart(
    courseId: number
  ): boolean {

    return this.cart.some(
      item =>
        item.courseId === courseId
    );
  }


  // =========================================================
  // SEPET TOPLAMI
  //
  // Artık frontend kendi kendine hesaplamıyor.
  //
  // Backend'deki Cart.totalPrice esas alınır.
  // =========================================================

  getCartTotal(): number {

    return this.cartTotal;
  }


  // =========================================================
  // SATIN AL
  //
  // ESKİ AKIŞ:
  //
  // frontend sepeti
  // ↓
  // backend sepetini temizle
  // ↓
  // tekrar CartItem oluştur
  // ↓
  // Order
  //
  //
  // YENİ AKIŞ:
  //
  // CartItem'lar ZATEN backend'de.
  //
  // Satın Al:
  //
  // mevcut backend Cart
  // ↓
  // OrderService
  // ↓
  // satın alma
  // ↓
  // Cart temizlenir
  //
  // =========================================================

  checkout(): void {

    if (
      this.cart.length === 0
    ) {

      return;
    }


    if (
      this.isCheckingOut
    ) {

      return;
    }


    this.isCheckingOut = true;


    this.orderService
      .placeOrder(
        this.currentStudent.id
      )
      .subscribe({

        next: () => {

          alert(
            '🎉 Sepetteki kurslar başarıyla satın alındı!'
          );


          // Backend checkout sonrasında sepeti
          // temizlediği için frontend'i de
          // anında temizliyoruz.
          this.cart = [];

          this.cartTotal = 0;


          this.isCheckingOut = false;


          // Satın alınan kursları mağazadan
          // kaldırmak ve backend sepetini tekrar
          // doğrulamak için mağazayı yeniden yükle.
          this.loadMarketplace();
        },


        error: (err) => {

          this.isCheckingOut = false;


          console.error(
            'Satın alma sırasında hata:',
            err
          );


          const backendMessage =
            err?.error?.message;


          alert(
            backendMessage
            ||
            'Satın alma işlemi sırasında bir sorun oluştu.'
          );
        }

      });
  }


  // =========================================================
  // YORUMLAR
  // =========================================================

  toggleReviews(
    course: any
  ): void {

    if (
      course.showReviews
    ) {

      course.showReviews = false;

      return;
    }


    this.courseService
      .getCourseReviews(
        course.id
      )
      .subscribe({

        next: (reviews) => {

          course.reviews =
            reviews;


          course.showReviews =
            true;
        },


        error: (err) => {

          console.error(
            'Yorumlar yüklenirken hata:',
            err
          );
        }

      });
  }


  getStars(
    rating: number
  ): string {

    return '⭐'.repeat(
      rating
    );
  }
}
