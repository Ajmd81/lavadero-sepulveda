package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.service.ReminderSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controlador para gestionar el envío manual de recordatorios
 * Este controlador será útil para pruebas o para un panel de administración
 */
@Controller
@RequestMapping("/admin/recordatorios")
public class ReminderController {

    @Autowired
    private ReminderSchedulerService reminderService;

    /**
     * Envía un recordatorio manualmente para una cita específica
     *
     * @param citaId ID de la cita para la que se enviará el recordatorio
     * @return ResponseEntity con el resultado de la operación
     */
    @GetMapping("/enviar/{citaId}")
    @ResponseBody
    public ResponseEntity<String> enviarRecordatorioManual(@PathVariable Long citaId) {
        try {
            reminderService.enviarRecordatorioManual(citaId);
            return ResponseEntity.ok("Recordatorio enviado con éxito para la cita ID: " + citaId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al enviar recordatorio: " + e.getMessage());
        }
    }

    /**
     * Ejecuta manualmente el proceso de envío de recordatorios para todas las citas de mañana
     *
     * @return ResponseEntity con el resultado de la operación
     */
    @GetMapping("/proceso-diario")
    @ResponseBody
    public ResponseEntity<String> ejecutarProcesoRecordatorios() {
        try {
            reminderService.enviarRecordatoriosCitas();
            return ResponseEntity.ok("Proceso de envío de recordatorios ejecutado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al ejecutar el proceso de recordatorios: " + e.getMessage());
        }
    }
}