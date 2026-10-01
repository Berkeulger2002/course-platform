import {
  Component
} from '@angular/core';

import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

import {
  AuthService
} from '../../services/auth.service';


@Component({
  selector: 'app-admin-layout',

  standalone: true,

  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],

  templateUrl:
    './admin-layout.component.html',

  styleUrl:
    './admin-layout.component.css'
})
export class AdminLayoutComponent {


  constructor(

    private authService:
    AuthService,

    private router:
    Router

  ) {
  }


  logout(): void {


    this.authService
      .logout()
      .subscribe({


        next: () => {


          localStorage.removeItem(
            'currentUser'
          );


          this.router.navigate([
            '/admin/login'
          ]);
        },


        error: () => {


          localStorage.removeItem(
            'currentUser'
          );


          this.router.navigate([
            '/admin/login'
          ]);
        }

      });
  }
}
