package com.lavaderosepulveda.app.fiscal.service;

import com.lavaderosepulveda.app.dto.DeclaracionFiscalDTO;
import com.lavaderosepulveda.app.model.DeclaracionFiscal;
import com.lavaderosepulveda.app.model.enums.EstadoDeclaracion;
import com.lavaderosepulveda.app.repository.DeclaracionFiscalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeclaracionFiscalService {

    @Autowired
    private DeclaracionFiscalRepository repo;

    // ─────────────────────────────────────────────────────────────────────────
    // Consultas
    // ─────────────────────────────────────────────────────────────────────────

    /** Devuelve todo el historial (más reciente primero). */
    public List<DeclaracionFiscalDTO> listarTodas() {
        return repo.findAllOrdenadas()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Devuelve el historial filtrado por modelo ("303" o "130"). */
    public List<DeclaracionFiscalDTO> listarPorModelo(String modelo) {
        return repo.findByModelo(modelo.toUpperCase())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Devuelve solo las declaraciones pendientes de presentar. */
    public List<DeclaracionFiscalDTO> listarGeneradas() {
        return repo.findByEstado(EstadoDeclaracion.GENERADO)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Registro de generación
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registra (o actualiza si ya existe) la generación de un fichero BOE 303.
     * Se llama desde el controlador en el momento de la descarga.
     */
    @Transactional
    public DeclaracionFiscalDTO registrarGeneracion303(
            int ejercicio, String periodo,
            BigDecimal baseRepercutida, BigDecimal cuotaRepercutida,
            BigDecimal baseSoportada, BigDecimal cuotaSoportada,
            String nombreFichero) {

        DeclaracionFiscal d = repo
                .findByClaveNatural("303", ejercicio, periodo.toUpperCase())
                .orElse(new DeclaracionFiscal());

        d.setModelo("303");
        d.setEjercicio(ejercicio);
        d.setPeriodo(periodo.toUpperCase());
        d.setBaseRepercutida(orCero(baseRepercutida));
        d.setCuotaRepercutida(orCero(cuotaRepercutida));
        d.setBaseSoportada(orCero(baseSoportada));
        d.setCuotaSoportada(orCero(cuotaSoportada));
        d.setResultadoIva(orCero(cuotaRepercutida).subtract(orCero(cuotaSoportada)));
        d.setNombreFichero(nombreFichero);
        // Si ya estaba PRESENTADO, no retrocedemos el estado — solo actualizamos
        // importes
        if (d.getEstado() == null)
            d.setEstado(EstadoDeclaracion.GENERADO);

        return toDTO(repo.save(d));
    }

    /**
     * Registra (o actualiza si ya existe) la generación de un fichero BOE 130.
     */
    @Transactional
    public DeclaracionFiscalDTO registrarGeneracion130(
            int ejercicio, String periodo,
            BigDecimal ingresosTrimestre, BigDecimal gastosTrimestre,
            BigDecimal rendimientoNeto, BigDecimal pagoFraccionado,
            String nombreFichero) {

        DeclaracionFiscal d = repo
                .findByClaveNatural("130", ejercicio, periodo.toUpperCase())
                .orElse(new DeclaracionFiscal());

        d.setModelo("130");
        d.setEjercicio(ejercicio);
        d.setPeriodo(periodo.toUpperCase());
        d.setIngresosTrimestre(orCero(ingresosTrimestre));
        d.setGastosTrimestre(orCero(gastosTrimestre));
        d.setRendimientoNeto(orCero(rendimientoNeto));
        d.setPagoFraccionado(orCero(pagoFraccionado));
        d.setNombreFichero(nombreFichero);
        if (d.getEstado() == null)
            d.setEstado(EstadoDeclaracion.GENERADO);

        return toDTO(repo.save(d));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Marcar como presentado
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Marca una declaración como PRESENTADO ante la AEAT.
     *
     * @param id                ID de la declaración
     * @param fechaPresentacion Fecha en que se presentó (puede ser null → hoy)
     */
    @Transactional
    public DeclaracionFiscalDTO marcarComoPresentado(Long id, LocalDate fechaPresentacion) {
        DeclaracionFiscal d = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Declaración no encontrada con id: " + id));

        d.setEstado(EstadoDeclaracion.PRESENTADO);
        d.setFechaPresentacion(fechaPresentacion != null ? fechaPresentacion : LocalDate.now());

        return toDTO(repo.save(d));
    }

    /**
     * Revierte el estado a GENERADO (por si se marcó por error como presentado).
     */
    @Transactional
    public DeclaracionFiscalDTO revertirPresentacion(Long id) {
        DeclaracionFiscal d = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Declaración no encontrada con id: " + id));

        d.setEstado(EstadoDeclaracion.GENERADO);
        d.setFechaPresentacion(null);

        return toDTO(repo.save(d));
    }

    /**
     * Elimina una declaración del historial.
     */
    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Declaración no encontrada con id: " + id);
        }
        repo.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapper entidad → DTO
    // ─────────────────────────────────────────────────────────────────────────

    private DeclaracionFiscalDTO toDTO(DeclaracionFiscal d) {
        DeclaracionFiscalDTO dto = new DeclaracionFiscalDTO();
        dto.setId(d.getId());
        dto.setModelo(d.getModelo());
        dto.setEjercicio(d.getEjercicio());
        dto.setPeriodo(d.getPeriodo());
        dto.setEstado(d.getEstado().name());
        dto.setFechaGeneracion(d.getFechaGeneracion());
        dto.setFechaPresentacion(d.getFechaPresentacion());
        dto.setBaseRepercutida(d.getBaseRepercutida());
        dto.setCuotaRepercutida(d.getCuotaRepercutida());
        dto.setBaseSoportada(d.getBaseSoportada());
        dto.setCuotaSoportada(d.getCuotaSoportada());
        dto.setResultadoIva(d.getResultadoIva());
        dto.setIngresosTrimestre(d.getIngresosTrimestre());
        dto.setGastosTrimestre(d.getGastosTrimestre());
        dto.setRendimientoNeto(d.getRendimientoNeto());
        dto.setPagoFraccionado(d.getPagoFraccionado());
        dto.setNombreFichero(d.getNombreFichero());
        return dto;
    }

    private BigDecimal orCero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}