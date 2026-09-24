import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { FavoriteService } from '../../services/favorite.service';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './favorites.component.html',
  styleUrl: './favorites.component.css'
})
export class FavoritesComponent implements OnInit {

  currentStudent: any;

  favoriteCourses: any[] = [];

  isLoading = true;


  constructor(
    private favoriteService: FavoriteService,
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


    this.loadFavorites();
  }


  // =========================================================
  // FAVORİLERİ GETİR
  // =========================================================
  loadFavorites(): void {

    this.isLoading = true;


    this.favoriteService
      .getFavorites(
        this.currentStudent.id
      )
      .subscribe({

        next: (courses) => {

          this.favoriteCourses = courses;

          this.isLoading = false;
        },

        error: (err) => {

          this.isLoading = false;

          console.error(
            'Favoriler yüklenirken hata:',
            err
          );
        }

      });
  }


  // =========================================================
  // FAVORİDEN ÇIKAR
  // =========================================================
  removeFavorite(course: any): void {

    this.favoriteService
      .removeFavorite(
        this.currentStudent.id,
        course.id
      )
      .subscribe({

        next: () => {

          this.favoriteCourses =
            this.favoriteCourses.filter(
              favorite =>
                favorite.id !== course.id
            );
        },

        error: (err) => {

          console.error(
            'Favoriden çıkarılırken hata:',
            err
          );
        }

      });
  }


  // =========================================================
  // MAĞAZAYA GİT
  // =========================================================
  goToMarketplace(): void {

    this.router.navigate([
      '/student/marketplace'
    ]);
  }
}
