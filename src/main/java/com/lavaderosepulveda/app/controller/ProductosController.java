package com.lavaderosepulveda.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductosController {

    @GetMapping("/productos")
    public String mostrarProductos() {
        return "productos";
    }
}