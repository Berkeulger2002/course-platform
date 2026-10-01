import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';


// =========================================================
// SYSTEM COMPONENT
// =========================================================

export interface AdminSystemComponent {

  name: string;

  status:
    'UP'
    | 'DOWN'
    | string;

  responseTimeMs:
    number | null;

  detail: string;
}


// =========================================================
// SYSTEM RESPONSE
// =========================================================

export interface AdminSystemStatus {

  overallStatus:
    'UP'
    | 'DEGRADED'
    | string;

  serverTime: string;

  uptimeSeconds: number;

  javaVersion: string;

  usedMemoryMb: number;

  maxMemoryMb: number;

  flywayVersion: string;

  components:
    AdminSystemComponent[];
}


@Injectable({
  providedIn: 'root'
})
export class AdminSystemService {


  private readonly apiUrl =
    'http://localhost:8081/api/admin/system';


  constructor(
    private http: HttpClient
  ) {
  }


  // =========================================================
  // GET SYSTEM STATUS
  // =========================================================

  getSystemStatus():
    Observable<AdminSystemStatus> {


    return this.http.get<AdminSystemStatus>(

      this.apiUrl

    );
  }
}
