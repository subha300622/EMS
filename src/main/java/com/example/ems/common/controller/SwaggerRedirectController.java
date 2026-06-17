package com.example.ems.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/")
    public String redirectToSwaggerFromRoot() {
        return "redirect:/swagger-ui/index.html";
    }

    @GetMapping("/swagger-ui")
    public String redirectToSwaggerFromUiPath() {
        return "redirect:/swagger-ui/index.html";
    }
}
