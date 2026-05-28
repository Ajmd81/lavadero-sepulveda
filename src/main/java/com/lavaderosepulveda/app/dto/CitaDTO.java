package com.lavaderosepulveda.app.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import jakarta.validation.constraints.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CitaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s'\\-\\.]+$",
             message = "El nombre solo puede contener letras, espacios, guiones y puntos")
    private String nombre;

    @Email(message = "Formato de email no válido")
    @Size(max = 150, message = "El email no puede superar 150 caracteres")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[6789]\\d{8}$",
             message = "El teléfono debe ser un número español válido (9 dígitos empezando por 6, 7, 8 o 9)")
    private String telefono;

    @NotBlank(message = "El modelo del vehículo es obligatorio")
    @Size(max = 100, message = "El modelo no puede superar 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s\\-\\.]+$",
             message = "El modelo del vehículo contiene caracteres no permitidos")
    private String modeloVehiculo;

    @NotNull(message = "El tipo de lavado es obligatorio")
    private TipoLavado tipoLavado;

    @NotNull(message = "La fecha es obligatoria")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime hora;

    /**
     * Duración en minutos. Por defecto 60.
     * Solo el CRM puede enviar 120 para reservar 2 horas consecutivas.
     * Los clientes públicos siempre usan 60 (el @PrePersist de Cita lo garantiza).
     */
    @Min(value = 60, message = "La duración mínima es 60 minutos")
    @Max(value = 180, message = "La duración máxima es 180 minutos")
    private Integer duracionEstimada = 60;

    private String estado = "PENDIENTE";
    private Boolean pagoAdelantado = false;

    @Size(max = 100)
    private String referenciaPago;

    @Pattern(regexp = "^[6789]\\d{8}$|^$",
             message = "El número de Bizum debe ser un teléfono español válido o estar vacío")
    private String numeroBizum;

    @Size(max = 500)
    private String observaciones;

    // ─── Deserializador flexible para LocalTime ───────────────────────────────

    public static class FlexibleLocalTimeDeserializer extends StdDeserializer<LocalTime> {
        public FlexibleLocalTimeDeserializer() { super(LocalTime.class); }

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String value = p.getText().trim();
            try {
                return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e1) {
                try {
                    return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm:ss"));
                } catch (DateTimeParseException e2) {
                    throw new IOException("No se puede parsear la hora '" + value +
                            "'. Formatos aceptados: HH:mm o HH:mm:ss");
                }
            }
        }
    }

    // ─── Constructores ────────────────────────────────────────────────────────

    public CitaDTO() {}

    public CitaDTO(String nombre, String email, String telefono, String modeloVehiculo,
                   TipoLavado tipoLavado, LocalDate fecha, LocalTime hora) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.modeloVehiculo = modeloVehiculo;
        this.tipoLavado = tipoLavado;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = "PENDIENTE";
        this.pagoAdelantado = false;
        this.duracionEstimada = 60;
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getModeloVehiculo() { return modeloVehiculo; }
    public void setModeloVehiculo(String modeloVehiculo) { this.modeloVehiculo = modeloVehiculo; }

    public TipoLavado getTipoLavado() { return tipoLavado; }
    public void setTipoLavado(TipoLavado tipoLavado) { this.tipoLavado = tipoLavado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public Integer getDuracionEstimada() { return duracionEstimada; }
    public void setDuracionEstimada(Integer duracionEstimada) { this.duracionEstimada = duracionEstimada; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Boolean getPagoAdelantado() { return pagoAdelantado; }
    public void setPagoAdelantado(Boolean pagoAdelantado) { this.pagoAdelantado = pagoAdelantado; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }

    public String getNumeroBizum() { return numeroBizum; }
    public void setNumeroBizum(String numeroBizum) { this.numeroBizum = numeroBizum; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return "CitaDTO{nombre='" + nombre + "', email='" + email + "', telefono='" + telefono +
               "', modeloVehiculo='" + modeloVehiculo + "', tipoLavado=" + tipoLavado +
               ", fecha=" + fecha + ", hora=" + hora + ", duracion=" + duracionEstimada + "}";
    }
}