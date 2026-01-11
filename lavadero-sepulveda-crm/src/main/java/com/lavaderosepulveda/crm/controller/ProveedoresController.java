package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.ProveedorDTO;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProveedoresController {

    @FXML
    private TextField txtBuscar;
    @FXML
    private CheckBox chkMostrarInactivos;
    @FXML
    private Label lblTotal;
    @FXML
    private TableView<ProveedorDTO> tablaProveedores;
    @FXML
    private TableColumn<ProveedorDTO, String> colNombre;
    @FXML
    private TableColumn<ProveedorDTO, String> colNif;
    @FXML
    private TableColumn<ProveedorDTO, String> colTelefono;
    @FXML
    private TableColumn<ProveedorDTO, String> colEmail;
    @FXML
    private TableColumn<ProveedorDTO, String> colContacto;
    @FXML
    private TableColumn<ProveedorDTO, String> colActivo;
    @FXML
    private TableColumn<ProveedorDTO, Void> colAcciones;

    private FacturacionApiService apiService;
    private ObservableList<ProveedorDTO> listaProveedores = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        apiService = FacturacionApiService.getInstance();
        configurarTabla();
        cargarProveedores();
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colNif.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNif()));
        colTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colContacto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getContacto()));
        colActivo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getActivo() != null && c.getValue().getActivo() ? "✅ Activo" : "❌ Inactivo"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox contenedor = new HBox(5, btnEditar, btnEliminar);

            {
                btnEditar.setOnAction(e -> editarProveedor(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarProveedor(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tablaProveedores.setItems(listaProveedores);
    }

    @FXML
    public void cargarProveedores() {
        try {
            List<ProveedorDTO> proveedores;
            if (chkMostrarInactivos.isSelected()) {
                proveedores = apiService.obtenerProveedores();
            } else {
                proveedores = apiService.obtenerProveedoresActivos();
            }
            listaProveedores.setAll(proveedores);
            lblTotal.setText("Total: " + proveedores.size() + " proveedores");
        } catch (Exception e) {
            mostrarError("Error", "No se pudieron cargar los proveedores: " + e.getMessage());
        }
    }

    @FXML
    private void buscar() {
        String termino = txtBuscar.getText().trim();
        if (termino.isEmpty()) {
            cargarProveedores();
            return;
        }

        try {
            List<ProveedorDTO> resultado = apiService.buscarProveedores(termino);
            listaProveedores.setAll(resultado);
            lblTotal.setText("Encontrados: " + resultado.size() + " proveedores");
        } catch (Exception e) {
            mostrarError("Error", "Error en la búsqueda: " + e.getMessage());
        }
    }

    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        cargarProveedores();
    }

    @FXML
    private void nuevoProveedor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario-proveedor.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Nuevo Proveedor");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tablaProveedores.getScene().getWindow());
            stage.showAndWait();
            cargarProveedores();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    private void editarProveedor(ProveedorDTO proveedor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario-proveedor.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Editar Proveedor");
            stage.setScene(new Scene(loader.load()));

            FormularioProveedorController controller = loader.getController();
            controller.setProveedor(proveedor);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tablaProveedores.getScene().getWindow());
            stage.showAndWait();
            cargarProveedores();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    private void eliminarProveedor(ProveedorDTO proveedor) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar proveedor " + proveedor.getNombre() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        confirmacion.initOwner(tablaProveedores.getScene().getWindow());

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    apiService.eliminarProveedor(proveedor.getId());
                    cargarProveedores();
                    mostrarInfo("Éxito", "Proveedor eliminado correctamente");
                } catch (Exception e) {
                    mostrarError("Error", "No se pudo eliminar el proveedor: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void importarProveedores() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Proveedores");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));

        File archivo = fileChooser.showOpenDialog(tablaProveedores.getScene().getWindow());
        if (archivo == null) return;

        try {
            List<ProveedorDTO> proveedoresImportados;
            
            if (archivo.getName().toLowerCase().endsWith(".xlsx") || 
                archivo.getName().toLowerCase().endsWith(".xls")) {
                proveedoresImportados = leerExcel(archivo);
            } else {
                proveedoresImportados = leerCSV(archivo);
            }
            
            if (proveedoresImportados.isEmpty()) {
                mostrarError("Error", "No se encontraron proveedores válidos en el archivo");
                return;
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar importación");
            confirmacion.setHeaderText("Se encontraron " + proveedoresImportados.size() + " proveedores");
            confirmacion.setContentText("¿Desea importarlos?");
            confirmacion.initOwner(tablaProveedores.getScene().getWindow());

            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    int importados = 0;
                    int errores = 0;
                    StringBuilder mensajesError = new StringBuilder();
                    
                    for (ProveedorDTO proveedor : proveedoresImportados) {
                        try {
                            apiService.crearProveedor(proveedor);
                            importados++;
                        } catch (Exception e) {
                            errores++;
                            if (errores <= 5) {
                                mensajesError.append("- ").append(proveedor.getNombre())
                                        .append(": ").append(e.getMessage()).append("\n");
                            }
                        }
                    }
                    
                    cargarProveedores();
                    
                    String mensaje = "Proveedores importados: " + importados;
                    if (errores > 0) {
                        mensaje += "\nErrores: " + errores;
                        if (mensajesError.length() > 0) {
                            mensaje += "\n\nDetalles:\n" + mensajesError;
                        }
                    }
                    mostrarInfo("Importación completada", mensaje);
                }
            });

        } catch (Exception e) {
            mostrarError("Error de importación", "No se pudo leer el archivo: " + e.getMessage());
        }
    }

    private List<ProveedorDTO> leerExcel(File archivo) throws IOException {
        List<ProveedorDTO> proveedores = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet("Proveedores");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }
            
            int filaInicio = 0;
            for (int i = 0; i <= 10; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String primeraCelda = obtenerValorCelda(row.getCell(0));
                    if (primeraCelda != null && primeraCelda.toUpperCase().contains("NIF")) {
                        filaInicio = i + 1;
                        break;
                    }
                }
            }
            
            for (int i = filaInicio; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String nif = obtenerValorCelda(row.getCell(0));
                String nombre = obtenerValorCelda(row.getCell(1));
                
                if (nombre == null || nombre.trim().isEmpty()) continue;
                
                ProveedorDTO proveedor = new ProveedorDTO();
                proveedor.setNif(nif);
                proveedor.setNombre(nombre.trim());
                proveedor.setEmail(obtenerValorCelda(row.getCell(2)));
                
                String telefono = obtenerValorCelda(row.getCell(3));
                String movil = obtenerValorCelda(row.getCell(4));
                if (telefono != null && !telefono.isEmpty()) {
                    proveedor.setTelefono(telefono);
                } else if (movil != null && !movil.isEmpty()) {
                    proveedor.setTelefono(movil);
                }
                
                String direccion = obtenerValorCelda(row.getCell(6));
                String cp = obtenerValorCelda(row.getCell(7));
                String poblacion = obtenerValorCelda(row.getCell(8));
                String provincia = obtenerValorCelda(row.getCell(9));
                
                StringBuilder direccionCompleta = new StringBuilder();
                if (direccion != null && !direccion.isEmpty()) {
                    direccionCompleta.append(direccion);
                }
                if (cp != null && !cp.isEmpty()) {
                    if (direccionCompleta.length() > 0) direccionCompleta.append(", ");
                    direccionCompleta.append(cp);
                }
                if (poblacion != null && !poblacion.isEmpty()) {
                    if (direccionCompleta.length() > 0) direccionCompleta.append(" ");
                    direccionCompleta.append(poblacion);
                }
                if (provincia != null && !provincia.isEmpty() && !provincia.equalsIgnoreCase(poblacion)) {
                    if (direccionCompleta.length() > 0) direccionCompleta.append(" (");
                    direccionCompleta.append(provincia);
                    direccionCompleta.append(")");
                }
                proveedor.setDireccion(direccionCompleta.toString());
                
                proveedor.setNotas(obtenerValorCelda(row.getCell(17)));
                proveedor.setIban(obtenerValorCelda(row.getCell(18)));
                proveedor.setActivo(true);
                
                proveedores.add(proveedor);
            }
        }
        
        return proveedores;
    }

    private String obtenerValorCelda(Cell cell) {
        if (cell == null) return null;
        
        CellType cellType = cell.getCellType();
        
        if (cellType == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cellType == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            if (value == Math.floor(value)) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        } else if (cellType == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cellType == CellType.FORMULA) {
            try {
                return cell.getStringCellValue();
            } catch (Exception e) {
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e2) {
                    return null;
                }
            }
        }
        return null;
    }

    private List<ProveedorDTO> leerCSV(File archivo) throws IOException {
        List<ProveedorDTO> proveedores = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            
            String linea;
            boolean primeraLinea = true;
            
            while ((linea = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                
                if (linea.trim().isEmpty()) continue;
                
                String[] campos = linea.contains(";") ? linea.split(";") : linea.split(",");
                
                if (campos.length >= 1 && !campos[0].trim().isEmpty()) {
                    ProveedorDTO proveedor = new ProveedorDTO();
                    proveedor.setNombre(limpiarCampo(campos, 0));
                    proveedor.setNif(limpiarCampo(campos, 1));
                    proveedor.setDireccion(limpiarCampo(campos, 2));
                    proveedor.setTelefono(limpiarCampo(campos, 3));
                    proveedor.setEmail(limpiarCampo(campos, 4));
                    proveedor.setContacto(limpiarCampo(campos, 5));
                    proveedor.setIban(limpiarCampo(campos, 6));
                    proveedor.setNotas(limpiarCampo(campos, 7));
                    proveedor.setActivo(true);
                    
                    proveedores.add(proveedor);
                }
            }
        }
        
        return proveedores;
    }

    private String limpiarCampo(String[] campos, int indice) {
        if (indice >= campos.length) return "";
        String valor = campos[indice].trim();
        if (valor.startsWith("\"") && valor.endsWith("\"")) {
            valor = valor.substring(1, valor.length() - 1);
        }
        return valor;
    }

    @FXML
    private void exportarProveedores() {
        if (listaProveedores.isEmpty()) {
            mostrarError("Error", "No hay proveedores para exportar");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Proveedores");
        fileChooser.setInitialFileName("proveedores_export.xlsx");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx"),
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        File archivo = fileChooser.showSaveDialog(tablaProveedores.getScene().getWindow());
        if (archivo == null) return;

        try {
            if (archivo.getName().toLowerCase().endsWith(".xlsx")) {
                escribirExcel(archivo, listaProveedores);
            } else {
                escribirCSV(archivo, listaProveedores);
            }
            mostrarInfo("Exportación completada", 
                    "Se exportaron " + listaProveedores.size() + " proveedores a:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            mostrarError("Error de exportación", "No se pudo escribir el archivo: " + e.getMessage());
        }
    }

    private void escribirExcel(File archivo, List<ProveedorDTO> proveedores) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Proveedores");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Row headerRow = sheet.createRow(0);
            String[] cabeceras = {"NIF", "Nombre", "Email", "Teléfono", "Dirección", "Contacto", "IBAN", "Notas", "Activo"};
            for (int i = 0; i < cabeceras.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cabeceras[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (ProveedorDTO p : proveedores) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getNif() != null ? p.getNif() : "");
                row.createCell(1).setCellValue(p.getNombre() != null ? p.getNombre() : "");
                row.createCell(2).setCellValue(p.getEmail() != null ? p.getEmail() : "");
                row.createCell(3).setCellValue(p.getTelefono() != null ? p.getTelefono() : "");
                row.createCell(4).setCellValue(p.getDireccion() != null ? p.getDireccion() : "");
                row.createCell(5).setCellValue(p.getContacto() != null ? p.getContacto() : "");
                row.createCell(6).setCellValue(p.getIban() != null ? p.getIban() : "");
                row.createCell(7).setCellValue(p.getNotas() != null ? p.getNotas() : "");
                row.createCell(8).setCellValue(p.getActivo() != null && p.getActivo() ? "Sí" : "No");
            }
            
            for (int i = 0; i < cabeceras.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                workbook.write(fos);
            }
        }
    }

    private void escribirCSV(File archivo, List<ProveedorDTO> proveedores) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            
            writer.write('\ufeff');
            writer.write("Nombre;NIF;Dirección;Teléfono;Email;Contacto;IBAN;Notas;Activo");
            writer.newLine();
            
            for (ProveedorDTO p : proveedores) {
                writer.write(String.join(";",
                        escaparCSV(p.getNombre()),
                        escaparCSV(p.getNif()),
                        escaparCSV(p.getDireccion()),
                        escaparCSV(p.getTelefono()),
                        escaparCSV(p.getEmail()),
                        escaparCSV(p.getContacto()),
                        escaparCSV(p.getIban()),
                        escaparCSV(p.getNotas()),
                        p.getActivo() != null && p.getActivo() ? "Sí" : "No"
                ));
                writer.newLine();
            }
        }
    }

    private String escaparCSV(String valor) {
        if (valor == null) return "";
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) tablaProveedores.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.initOwner(tablaProveedores.getScene().getWindow());
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.initOwner(tablaProveedores.getScene().getWindow());
        alert.showAndWait();
    }
}
