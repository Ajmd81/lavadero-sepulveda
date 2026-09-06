package com.lavaderosepulveda.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para Thymeleaf en /admin/login
 * 
 * ⚠️ IMPORTANTE:
 * - SOLO esta clase maneja Thymeleaf para /admin/**
 * - TODOS los demás endpoints (/citas, /facturas, /modelos-fiscales, etc.)
 *   son manejados por React Router en el frontend
 * - NO agregar más métodos que retornen String (vistas Thymeleaf)
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    /**
     * Página de login - LA ÚNICA VISTA THYMELEAF DE /admin
     * 
     * React Router maneja:
     * - /admin/citas
     * - /admin/facturas
     * - /admin/modelos-fiscales
     * - /admin/configuracion
     * - /admin/clientes
     * - /admin/calendario
     * - /admin/contabilidad
     * - /admin/resumen-financiero
     * - /admin/proveedores
     * - /admin/libros-contables
     * - /admin/mi-perfil
     * - /admin (dashboard)
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        
        logger.info("GET /admin/login");
        
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña inválidos");
            logger.warn("Error de autenticación");
        }
        
        if (logout != null) {
            model.addAttribute("logout", "Sesión cerrada correctamente");
            logger.info("Usuario desconectado");
        }
        
        return "login";
    }

    // ❌ NO AGREGAR MÁS MÉTODOS QUE RETORNEN STRING
    // ❌ NO AGREGAR @GetMapping("/**") o similares
    // ✅ React Router maneja todas las demás rutas
}