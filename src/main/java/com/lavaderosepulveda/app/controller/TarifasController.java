package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.TipoLavado;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TarifasController {

    @GetMapping("/tarifas")
    public String mostrarTarifas(Model model) {
        model.addAttribute("tiposLavado", TipoLavado.values());
        return "tarifas";
    }
}