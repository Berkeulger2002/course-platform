// =========================================================
// API CONFIGURATION
//
// LOCAL:
// Angular      -> http://localhost:4200
// Spring Boot  -> http://localhost:8081
//
// PRODUCTION:
// Browser yalnızca frontend domainini görür.
//
// Örnek:
// https://course-platform.example.com/api/...
//
// Hosting tarafında /api isteklerini Spring Boot backend'e
// yönlendireceğiz.
// =========================================================


export const LOCAL_BACKEND_ORIGIN =
  'http://localhost:8081';


// =========================================================
// LOCAL ÇALIŞIYOR MU?
// =========================================================

const isLocalDevelopment =
  window.location.hostname === 'localhost'
  ||
  window.location.hostname === '127.0.0.1';


// =========================================================
// GERÇEK API ORIGIN
//
// Local:
// http://localhost:8081
//
// Production:
// frontend'in mevcut origin'i
//
// Örnek:
// https://course-platform.example.com
// =========================================================

export const API_ORIGIN =
  isLocalDevelopment
    ?
    LOCAL_BACKEND_ORIGIN
    :
    window.location.origin;


// =========================================================
// CSRF ENDPOINT
// =========================================================

export const CSRF_ENDPOINT =
  `${API_ORIGIN}/api/auth/csrf`;


// =========================================================
// URL DÖNÜŞTÜR
//
// Mevcut service dosyalarımız:
//
// http://localhost:8081/api/...
//
// kullanmaya devam edebilir.
//
// Production'da interceptor bunları:
//
// https://site-adresi/api/...
//
// haline dönüştürür.
// =========================================================

export function resolveApiUrl(
  url: string
): string {


  if (
    url.startsWith(
      LOCAL_BACKEND_ORIGIN
    )
  ) {


    return url.replace(

      LOCAL_BACKEND_ORIGIN,

      API_ORIGIN
    );
  }


  return url;
}


// =========================================================
// BACKEND REQUEST Mİ?
// =========================================================

export function isBackendUrl(
  url: string
): boolean {


  const resolvedUrl =
    resolveApiUrl(
      url
    );


  return (

    resolvedUrl.startsWith(
      `${API_ORIGIN}/api/`
    )

    ||

    resolvedUrl ===
    `${API_ORIGIN}/api`

  );
}
