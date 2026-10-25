package com.lavaderosepulveda.app.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para servir la vista del chatbot
 */
@Controller
public class ChatbotViewController {

    /**
     * Mostrar página del chatbot
     */
    @GetMapping("/chatbot")
    public String mostrarChatbot() {
        return "chatbot/chatbot";
    }
}