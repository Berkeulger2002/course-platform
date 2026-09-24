package com.example.course_platform.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class SpaForwardController {


    // =========================================================
    // ANGULAR SPA ROUTE FORWARD
    // =========================================================
    //
    // Angular client-side routing kullanıyor.
    //
    // Örneğin kullanıcı:
    //
    // /student/dashboard
    //
    // sayfasına Angular içerisinde giderse problem yoktur.
    //
    // Fakat production'da:
    //
    // https://site.com/student/dashboard
    //
    // adresinde F5 yapılırsa istek önce Spring Boot'a gelir.
    //
    // Spring Boot'ta /student/dashboard isimli REST endpoint
    // olmadığı için normal şartlarda 404 alınır.
    //
    // Bu controller API olmayan Angular route'larını tekrar
    // index.html'e gönderir.
    //
    // Daha sonra Angular Router doğru componenti açar.
    //
    //
    // DİKKAT:
    //
    // /api/**
    //
    // burada bulunmuyor.
    //
    // Dolayısıyla REST API istekleri Angular'a yönlendirilmez.
    //
    // =========================================================


    @RequestMapping({

            "/auth",

            "/student",
            "/student/**",

            "/teacher",
            "/teacher/**",

            "/student-dashboard",
            "/teacher-dashboard"

    })
    public String forwardAngularRoutes() {

        return "forward:/index.html";
    }
}