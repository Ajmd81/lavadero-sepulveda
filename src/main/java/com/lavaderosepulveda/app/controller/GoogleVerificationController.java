package com.lavaderosepulveda.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GoogleVerificationController {

    @GetMapping("/google33709fbb5cab4955.html")
    @ResponseBody
    public String googleVerification() {
        return "google-site-verification: google33709fbb5cab4955.html";
    }
}