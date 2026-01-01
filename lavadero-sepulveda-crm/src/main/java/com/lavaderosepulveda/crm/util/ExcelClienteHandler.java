package com.lavaderosepulveda.crm.util;

import com.lavaderosepulveda.crm.api.dto.ClienteDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para importar y exportar clientes desde/hacia Excel
 * Soporta formato de exportación de contabilidad
 */
public class ExcelClienteHandler {

    private static final Logger log = LoggerFactory.getLogger(ExcelClienteHandler.class);

    // ========================================
    // COLUMNAS DEL EXCEL DE CONTABILIDAD
    // ========================================
    private static final int COL_NIF = 0;              // A: NIF
    private static final int COL_NOMBRE = 1;           // B: NOMBRE O RAZÓN SOCIAL
    private static final int COL_EMAIL = 2;            // C: EMAIL
    private static final int COL_TELEFONO = 3;         // D: TELF.
    private static final int COL_MOVIL = 4;            // E: MÓVIL
    private static final int COL_FAX = 5;              // F: FAX
    private static final int COL_DIRECCION = 6;        // G: DIRECCIÓN
    private static final int COL_CODIGO_POSTAL = 7;    // H: COD. POSTAL
    private static final int COL_POBLACION = 8;        // I: POBLACIÓN
    private static final int COL_PROVINCIA = 9;        // J: PROVINCIA
    private static final int COL_PAIS = 10;            // K: PAÍS
    private static final int COL_URL = 11;             // L: URL
    private static final int COL_DESCUENTO = 12;       // M: % DESCUENTO
    private static final int COL_NOTAS = 17;           // R: NOTAS PRIVADAS

    // Fila donde empieza la cabecera (índice 0)
    private static final int FILA_CABECERA = 3;        // Fila 4 en Excel
    private static final int FILA_DATOS_INICIO = 4;    // Fila 5 en Excel

    /**
     * Importar clientes desde un archivo Excel (formato contabilidad)
     */
    public static List<ClienteDTO> importarDesdeExcel(File archivo) throws IOException {
        List<ClienteDTO> clientes = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Buscar hoja de Clientes
            Sheet sheet = workbook.getSheet("Clientes");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
                log.info("Hoja 'Clientes' no encontrada, usando primera hoja: {}", sheet.getSheetName());
            } else {
                log.info("Leyendo hoja: Clientes");
            }

            // Detectar si es formato de contabilidad o formato simple
            boolean esFormatoContabilidad = detectarFormatoContabilidad(sheet);
            log.info("Formato detectado: {}", esFormatoContabilidad ? "Contabilidad" : "Simple");

            if (esFormatoContabilidad) {
                clientes = importarFormatoContabilidad(sheet);
            } else {
                clientes = importarFormatoSimple(sheet);
            }

            log.info("Clientes importados: {}", clientes.size());
        }

        return clientes;
    }

    /**
     * Detectar si el Excel es formato de contabilidad
     */
    private static boolean detectarFormatoContabilidad(Sheet sheet) {
        // Buscar la fila de cabecera que contenga "NIF" y "NOMBRE O RAZÓN SOCIAL"
        for (int i = 0; i <= 5; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String primeraCelda = getCellValueAsString(row, 0);
                String segundaCelda = getCellValueAsString(row, 1);
                if (primeraCelda != null && segundaCelda != null) {
                    if (primeraCelda.toUpperCase().contains("NIF") && 
                        segundaCelda.toUpperCase().contains("NOMBRE")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Importar desde formato de contabilidad
     */
    private static List<ClienteDTO> importarFormatoContabilidad(Sheet sheet) {
        List<ClienteDTO> clientes = new ArrayList<>();

        // Encontrar fila de cabecera
        int filaCabecera = encontrarFilaCabecera(sheet);
        log.info("Fila de cabecera encontrada: {}", filaCabecera + 1);

        // Leer datos desde la fila siguiente a la cabecera
        for (int i = filaCabecera + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                ClienteDTO cliente = parsearFilaContabilidad(row);
                if (cliente != null) {
                    clientes.add(cliente);
                    log.debug("Cliente importado: {} - {}", cliente.getNif(), cliente.getNombre());
                }
            } catch (Exception e) {
                log.error("Error al parsear fila {}: {}", i + 1, e.getMessage());
            }
        }

        return clientes;
    }

    /**
     * Encontrar la fila que contiene la cabecera
     */
    private static int encontrarFilaCabecera(Sheet sheet) {
        for (int i = 0; i <= 10; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String celda = getCellValueAsString(row, 0);
                if (celda != null && celda.toUpperCase().contains("NIF")) {
                    return i;
                }
            }
        }
        return FILA_CABECERA; // Por defecto fila 4
    }

    /**
     * Parsear una fila del formato de contabilidad
     */
    private static ClienteDTO parsearFilaContabilidad(Row row) {
        String nif = getCellValueAsString(row, COL_NIF);
        String nombre = getCellValueAsString(row, COL_NOMBRE);

        // Ignorar filas vacías o cabeceras duplicadas
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }
        if (nombre.toUpperCase().contains("NOMBRE") || nombre.toUpperCase().contains("RAZÓN SOCIAL")) {
            return null;
        }

        ClienteDTO cliente = new ClienteDTO();
        
        // NIF
        cliente.setNif(nif != null ? nif.trim() : null);
        
        // Nombre completo (sin separar en apellidos para razones sociales)
        cliente.setNombre(nombre.trim());
        cliente.setApellidos(null); // Las empresas no tienen apellidos
        
        // Contacto
        String telefono = getCellValueAsString(row, COL_TELEFONO);
        String movil = getCellValueAsString(row, COL_MOVIL);
        // Usar el que tenga valor, priorizando móvil
        cliente.setTelefono(movil != null && !movil.isEmpty() ? movil.trim() : 
                          (telefono != null ? telefono.trim() : null));
        
        cliente.setEmail(getCellValueAsString(row, COL_EMAIL));
        
        // Dirección completa
        cliente.setDireccion(getCellValueAsString(row, COL_DIRECCION));
        cliente.setCodigoPostal(getCellValueAsString(row, COL_CODIGO_POSTAL));
        cliente.setCiudad(getCellValueAsString(row, COL_POBLACION));
        cliente.setProvincia(getCellValueAsString(row, COL_PROVINCIA));
        
        // Notas
        cliente.setNotas(getCellValueAsString(row, COL_NOTAS));
        
        // Valores por defecto
        cliente.setActivo(true);
        cliente.setTotalCitas(0);
        cliente.setTotalFacturado(0.0);

        return cliente;
    }

    /**
     * Importar desde formato simple (el original)
     */
    private static List<ClienteDTO> importarFormatoSimple(Sheet sheet) {
        List<ClienteDTO> clientes = new ArrayList<>();

        // Empezar desde la fila 1 (saltar cabecera)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                ClienteDTO cliente = parsearFilaSimple(row);
                if (cliente != null) {
                    clientes.add(cliente);
                }
            } catch (Exception e) {
                log.error("Error al parsear fila {}: {}", i, e.getMessage());
            }
        }

        return clientes;
    }

    /**
     * Parsear fila del formato simple
     */
    private static ClienteDTO parsearFilaSimple(Row row) {
        String nombre = getCellValueAsString(row, 0);
        String telefono = getCellValueAsString(row, 2);

        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }

        ClienteDTO cliente = new ClienteDTO();
        cliente.setNombre(nombre.trim());
        cliente.setApellidos(getCellValueAsString(row, 1));
        cliente.setTelefono(telefono);
        cliente.setEmail(getCellValueAsString(row, 3));
        cliente.setVehiculoHabitual(getCellValueAsString(row, 4));
        cliente.setActivo(true);
        cliente.setTotalCitas(0);
        cliente.setTotalFacturado(0.0);

        return cliente;
    }

    /**
     * Exportar clientes a un archivo Excel
     */
    public static void exportarAExcel(List<ClienteDTO> clientes, File archivo) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clientes");

            // Crear estilos
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle dataStyle = crearEstiloData(workbook);
            CellStyle moneyStyle = crearEstiloMoney(workbook);

            // Crear cabecera
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "NIF", "Nombre", "Apellidos", "Teléfono", "Email", 
                "Dirección", "Cód. Postal", "Población", "Provincia",
                "Vehículo", "Total Citas", "Facturado", "Activo"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agregar datos
            int rowNum = 1;
            for (ClienteDTO cliente : clientes) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                
                createCell(row, col++, cliente.getNif(), dataStyle);
                createCell(row, col++, cliente.getNombre(), dataStyle);
                createCell(row, col++, cliente.getApellidos(), dataStyle);
                createCell(row, col++, cliente.getTelefono(), dataStyle);
                createCell(row, col++, cliente.getEmail(), dataStyle);
                createCell(row, col++, cliente.getDireccion(), dataStyle);
                createCell(row, col++, cliente.getCodigoPostal(), dataStyle);
                createCell(row, col++, cliente.getCiudad(), dataStyle);
                createCell(row, col++, cliente.getProvincia(), dataStyle);
                createCell(row, col++, cliente.getVehiculoHabitual(), dataStyle);
                createCell(row, col++, cliente.getTotalCitas(), dataStyle);
                createCell(row, col++, cliente.getTotalFacturado(), moneyStyle);
                createCell(row, col++, cliente.getActivo() != null && cliente.getActivo() ? "Sí" : "No", dataStyle);
            }

            // Ajustar anchos de columna
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                workbook.write(fos);
            }

            log.info("Clientes exportados a: {}", archivo.getAbsolutePath());
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private static void createCell(Row row, int column, Integer value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : 0);
        cell.setCellStyle(style);
    }

    private static void createCell(Row row, int column, Double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : 0.0);
        cell.setCellStyle(style);
    }

    private static String getCellValueAsString(Row row, int column) {
        Cell cell = row.getCell(column);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                String value = cell.getStringCellValue();
                return value != null && !value.trim().isEmpty() ? value.trim() : null;
            case NUMERIC:
                // Para números, formatear sin decimales si es entero
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }

    private static CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle crearEstiloData(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloMoney(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00 €"));
        return style;
    }
}
