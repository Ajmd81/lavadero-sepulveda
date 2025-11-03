package com.lavaderosepulveda.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PolicyController {

    @GetMapping("/policy")
    public String mostrarPolicy() {
        return "policy";
    }
}