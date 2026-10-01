package com.example.course_platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/auth",

            "/admin",
            "/admin/**",

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