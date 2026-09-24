import {
  HttpInterceptorFn,
  HttpRequest
} from '@angular/common/http';

import {
  from
} from 'rxjs';

import {
  switchMap
} from 'rxjs/operators';


import {
  CSRF_ENDPOINT,
  isBackendUrl,
  resolveApiUrl
} from '../config/api.config';


// =========================================================
// CSRF
// =========================================================

const CSRF_COOKIE_NAME =
  'XSRF-TOKEN';


const CSRF_HEADER_NAME =
  'X-XSRF-TOKEN';


// =========================================================
// COOKIE OKU
// =========================================================

function getCookie(
  name: string
): string | null {


  const prefix =
    `${name}=`;


  const cookies =
    document.cookie

      .split(';')

      .map(
        cookie =>
          cookie.trim()
      );


  const cookie =
    cookies.find(
      item =>
        item.startsWith(
          prefix
        )
    );


  if (!cookie) {

    return null;
  }


  return decodeURIComponent(

    cookie.substring(
      prefix.length
    )
  );
}


// =========================================================
// SAFE HTTP METHOD
// =========================================================

function isSafeMethod(
  method: string
): boolean {


  const normalizedMethod =
    method.toUpperCase();


  return (

    normalizedMethod === 'GET'

    ||

    normalizedMethod === 'HEAD'

    ||

    normalizedMethod === 'OPTIONS'

  );
}


// =========================================================
// BACKEND REQUEST HAZIRLA
//
// Burada iki iş yapıyoruz:
//
// 1) Localhost URL'sini gerekiyorsa production URL'sine çevir.
//
// 2) Cookie ve CSRF header ekle.
// =========================================================

function prepareBackendRequest(
  request: HttpRequest<unknown>
): HttpRequest<unknown> {


  const resolvedUrl =
    resolveApiUrl(
      request.url
    );


  let securedRequest =
    request.clone({

      url:
      resolvedUrl,

      withCredentials:
        true

    });


  // =======================================================
  // POST / PUT / PATCH / DELETE
  // =======================================================

  if (
    !isSafeMethod(
      request.method
    )
  ) {


    const csrfToken =
      getCookie(
        CSRF_COOKIE_NAME
      );


    if (csrfToken) {


      securedRequest =
        securedRequest.clone({

          setHeaders: {

            [CSRF_HEADER_NAME]:
            csrfToken

          }

        });
    }
  }


  return securedRequest;
}


// =========================================================
// INTERCEPTOR
// =========================================================

export const credentialsInterceptor:
  HttpInterceptorFn = (

  request,
  next

) => {


  // =======================================================
  // BACKEND REQUEST DEĞİLSE DOKUNMA
  // =======================================================

  if (
    !isBackendUrl(
      request.url
    )
  ) {


    return next(
      request
    );
  }


  // =======================================================
  // GET / HEAD / OPTIONS
  // =======================================================

  if (
    isSafeMethod(
      request.method
    )
  ) {


    return next(

      prepareBackendRequest(
        request
      )
    );
  }


  // =======================================================
  // CSRF TOKEN ZATEN VAR
  // =======================================================

  const existingCsrfToken =
    getCookie(
      CSRF_COOKIE_NAME
    );


  if (existingCsrfToken) {


    return next(

      prepareBackendRequest(
        request
      )
    );
  }


  // =======================================================
  // CSRF COOKIE YOK
  //
  // Önce Spring'den CSRF token oluşturmasını isteriz.
  //
  // native fetch kullanıldığı için interceptor tekrar
  // kendisini çağırmaz.
  // =======================================================

  return from(

    fetch(

      CSRF_ENDPOINT,

      {

        method:
          'GET',

        credentials:
          'include'

      }

    )

  ).pipe(

    switchMap(

      response => {


        if (
          !response.ok
        ) {


          throw new Error(
            'CSRF token alınamadı.'
          );
        }


        return next(

          prepareBackendRequest(
            request
          )
        );
      }
    )
  );
};
