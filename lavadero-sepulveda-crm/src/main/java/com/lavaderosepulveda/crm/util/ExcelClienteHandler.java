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
 */
public class ExcelClienteHandler {

    private static final Logger log = LoggerFactory.getLogger(ExcelClienteHandler.class);

    // Columnas del Excel
    private static final int COL_NOMBRE = 0;
    private static final int COL_APELLIDOS = 1;
    private static final int COL_TELEFONO = 2;
    private static final int COL_EMAIL = 3;
    private static final int COL_VEHICULO = 4;
    private static final int COL_TOTAL_CITAS = 5;
    private static final int COL_FACTURADO = 6;
    private static final int COL_ACTIVO = 7;

    /**
     * Importar clientes desde un archivo Excel
     */
    public static List<ClienteDTO> importarDesdeExcel(File archivo) throws IOException {
        List<ClienteDTO> clientes = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("Leyendo hoja: {}", sheet.getSheetName());

            // Empezar desde la fila 1 (saltar cabecera)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    ClienteDTO cliente = parsearFilaCliente(row);
                    if (cliente != null) {
                        clientes.add(cliente);
                    }
                } catch (Exception e) {
                    log.error("Error al parsear fila {}: {}", i, e.getMessage());
                }
            }

            log.info("Clientes importados: {}", clientes.size());
        }

        return clientes;
    }

    /**
     * Parsear una fila de Excel a ClienteDTO
     */
    private static ClienteDTO parsearFilaCliente(Row row) {
        String nombre = getCellValueAsString(row, COL_NOMBRE);
        String telefono = getCellValueAsString(row, COL_TELEFONO);

        // Validar campos obligatorios
        if (nombre == null || nombre.trim().isEmpty() || 
            telefono == null || telefono.trim().isEmpty()) {
            log.warn("Fila {} ignorada: falta nombre o teléfono", row.getRowNum());
            return null;
        }

        ClienteDTO cliente = new ClienteDTO();
        cliente.setNombre(nombre.trim());
        cliente.setApellidos(getCellValueAsString(row, COL_APELLIDOS));
        cliente.setTelefono(telefono.trim());
        cliente.setEmail(getCellValueAsString(row, COL_EMAIL));
        cliente.setVehiculoHabitual(getCellValueAsString(row, COL_VEHICULO));
        
        // Campos numéricos
        cliente.setTotalCitas(getCellValueAsInteger(row, COL_TOTAL_CITAS));
        cliente.setTotalFacturado(getCellValueAsDouble(row, COL_FACTURADO));
        
        // Estado
        String activoStr = getCellValueAsString(row, COL_ACTIVO);
        cliente.setActivo(activoStr != null && 
                         (activoStr.equalsIgnoreCase("sí") || 
                          activoStr.equalsIgnoreCase("si") ||
                          activoStr.equalsIgnoreCase("true") ||
                          activoStr.equalsIgnoreCase("activo")));

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
            crearCabeceraExcel(sheet, headerStyle);

            // Agregar datos
            int rowNum = 1;
            for (ClienteDTO cliente : clientes) {
                Row row = sheet.createRow(rowNum++);
                escribirFilaCliente(row, cliente, dataStyle, moneyStyle);
            }

            // Ajustar anchos de columna
            for (int i = 0; i <= COL_ACTIVO; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                workbook.write(fos);
            }

            log.info("Clientes exportados a: {}", archivo.getAbsolutePath());
        }
    }

    /**
     * Crear cabecera del Excel
     */
    private static void crearCabeceraExcel(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        
        String[] headers = {
            "Nombre", "Apellidos", "Teléfono", "Email", 
            "Vehículo Habitual", "Total Citas", "Total Facturado", "Activo"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Escribir una fila de cliente en Excel
     */
    private static void escribirFilaCliente(Row row, ClienteDTO cliente, 
                                           CellStyle dataStyle, CellStyle moneyStyle) {
        createCell(row, COL_NOMBRE, cliente.getNombre(), dataStyle);
        createCell(row, COL_APELLIDOS, cliente.getApellidos(), dataStyle);
        createCell(row, COL_TELEFONO, cliente.getTelefono(), dataStyle);
        createCell(row, COL_EMAIL, cliente.getEmail(), dataStyle);
        createCell(row, COL_VEHICULO, cliente.getVehiculoHabitual(), dataStyle);
        createCell(row, COL_TOTAL_CITAS, cliente.getTotalCitas(), dataStyle);
        createCell(row, COL_FACTURADO, cliente.getTotalFacturado(), moneyStyle);
        createCell(row, COL_ACTIVO, cliente.getActivo() ? "Sí" : "No", dataStyle);
    }

    /**
     * Crear celda con valor String
     */
    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * Crear celda con valor Integer
     */
    private static void createCell(Row row, int column, Integer value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue(0);
        }
        cell.setCellStyle(style);
    }

    /**
     * Crear celda con valor Double
     */
    private static void createCell(Row row, int column, Double value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue(0.0);
        }
        cell.setCellStyle(style);
    }

    /**
     * Obtener valor de celda como String
     */
    private static String getCellValueAsString(Row row, int column) {
        Cell cell = row.getCell(column);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    /**
     * Obtener valor de celda como Integer
     */
    private static Integer getCellValueAsInteger(Row row, int column) {
        Cell cell = row.getCell(column);
        if (cell == null) return 0;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue();
                return value.isEmpty() ? 0 : Integer.parseInt(value);
            }
        } catch (Exception e) {
            log.warn("Error al parsear integer en celda {}: {}", column, e.getMessage());
        }
        return 0;
    }

    /**
     * Obtener valor de celda como Double
     */
    private static Double getCellValueAsDouble(Row row, int column) {
        Cell cell = row.getCell(column);
        if (cell == null) return 0.0;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue();
                return value.isEmpty() ? 0.0 : Double.parseDouble(value);
            }
        } catch (Exception e) {
            log.warn("Error al parsear double en celda {}: {}", column, e.getMessage());
        }
        return 0.0;
    }

    /**
     * Crear estilo para cabecera
     */
    private static CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Crear estilo para datos
     */
    private static CellStyle crearEstiloData(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    /**
     * Crear estilo para moneda
     */
    private static CellStyle crearEstiloMoney(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00 €"));
        return style;
    }
}