package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.model.dto.CitaDTO;
import com.lavaderosepulveda.crm.api.service.CitaApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarioController {

    private static final Logger log = LoggerFactory.getLogger(CalendarioController.class);

    @FXML private Label lblMesAnio;
    @FXML private Button btnMesAnterior;
    @FXML private Button btnMesSiguiente;
    @FXML private GridPane gridCalendario;
    @FXML private ListView<String> listCitasDia;
    @FXML private Label lblCitasSeleccionadas;

    private final CitaApiService citaApiService = CitaApiService.getInstance();
    private YearMonth mesActual;
    private List<CitaDTO> todasLasCitas;
    private Map<Integer, Set<LocalDate>> festivosPorAnio;
    private LocalDate diaSeleccionado;

    @FXML
    public void initialize() {
        log.info("Inicializando CalendarioController...");
        mesActual = YearMonth.now();
        festivosPorAnio = new HashMap<>();
        cargarCitas();
    }

    private void cargarCitas() {
        new Thread(() -> {
            try {
                todasLasCitas = citaApiService.findAll();
                log.info("Citas cargadas para calendario: {}", todasLasCitas.size());
                
                Platform.runLater(() -> {
                    mostrarCalendario();
                });
            } catch (Exception e) {
                log.error("Error al cargar citas para calendario", e);
            }
        }).start();
    }

    private void mostrarCalendario() {
        // Actualizar etiqueta del mes
        String mesAnio = mesActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) 
            + " " + mesActual.getYear();
        lblMesAnio.setText(mesAnio.substring(0, 1).toUpperCase() + mesAnio.substring(1));

        // Limpiar calendario
        gridCalendario.getChildren().clear();

        // Encabezados de días de la semana
        String[] diasSemana = {"L", "M", "X", "J", "V", "S", "D"};
        for (int i = 0; i < 7; i++) {
            Label lblDia = new Label(diasSemana[i]);
            lblDia.setFont(Font.font("System", FontWeight.BOLD, 14));
            lblDia.setMaxWidth(Double.MAX_VALUE);
            lblDia.setAlignment(Pos.CENTER);
            lblDia.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 10;");
            gridCalendario.add(lblDia, i, 0);
        }

        // Primer día del mes y número de días
        LocalDate primerDia = mesActual.atDay(1);
        int diaSemanaInicio = primerDia.getDayOfWeek().getValue(); // 1 = Lunes, 7 = Domingo
        int diasEnMes = mesActual.lengthOfMonth();

        // Rellenar días del mes
        int fila = 1;
        int columna = diaSemanaInicio - 1;

        for (int dia = 1; dia <= diasEnMes; dia++) {
            LocalDate fecha = mesActual.atDay(dia);
            
            VBox vboxDia = crearCeldaDia(fecha);
            gridCalendario.add(vboxDia, columna, fila);

            columna++;
            if (columna > 6) {
                columna = 0;
                fila++;
            }
        }
    }

    private VBox crearCeldaDia(LocalDate fecha) {
        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 5;");
        vbox.setPrefSize(100, 80);

        // Número del día
        Label lblDia = new Label(String.valueOf(fecha.getDayOfMonth()));
        lblDia.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Colorear según si es festivo o fin de semana
        Set<LocalDate> festivosAnio = obtenerFestivosAnio(fecha.getYear());
        boolean esFestivo = festivosAnio.contains(fecha);
        boolean esFinDeSemana = fecha.getDayOfWeek().getValue() >= 6;
        boolean esHoy = fecha.equals(LocalDate.now());

        if (esHoy) {
            vbox.setStyle("-fx-border-color: #2196F3; -fx-border-width: 3; -fx-padding: 5; -fx-background-color: #E3F2FD;");
            lblDia.setTextFill(Color.web("#2196F3"));
        } else if (esFestivo) {
            lblDia.setTextFill(Color.RED);
            vbox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 5; -fx-background-color: #FFEBEE;");
        } else if (esFinDeSemana) {
            lblDia.setTextFill(Color.web("#FF6B6B"));
        }

        // Contar citas del día
        long numCitas = contarCitasDia(fecha);
        Label lblCitas = new Label();
        if (numCitas > 0) {
            lblCitas.setText(numCitas + " cita" + (numCitas > 1 ? "s" : ""));
            lblCitas.setFont(Font.font("System", 10));
            lblCitas.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                           "-fx-padding: 2 5 2 5; -fx-background-radius: 3;");
        }

        vbox.getChildren().addAll(lblDia, lblCitas);

        // Hacer clic en el día
        vbox.setOnMouseClicked(event -> {
            diaSeleccionado = fecha;
            mostrarCitasDia(fecha);
            // Actualizar visual
            mostrarCalendario();
        });

        // Hover effect
        vbox.setOnMouseEntered(event -> {
            if (!fecha.equals(diaSeleccionado)) {
                vbox.setStyle("-fx-border-color: #2196F3; -fx-border-width: 2; -fx-padding: 5; -fx-background-color: #f5f5f5;");
            }
        });

        vbox.setOnMouseExited(event -> {
            if (!fecha.equals(diaSeleccionado)) {
                String style = "-fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 5;";
                if (esFestivo) {
                    style += " -fx-background-color: #FFEBEE;";
                } else if (esHoy) {
                    style = "-fx-border-color: #2196F3; -fx-border-width: 3; -fx-padding: 5; -fx-background-color: #E3F2FD;";
                }
                vbox.setStyle(style);
            }
        });

        return vbox;
    }

    private long contarCitasDia(LocalDate fecha) {
        if (todasLasCitas == null) return 0;
        
        return todasLasCitas.stream()
            .filter(cita -> cita.getFechaHora() != null)
            .filter(cita -> cita.getFechaHora().toLocalDate().equals(fecha))
            .count();
    }

    private void mostrarCitasDia(LocalDate fecha) {
        lblCitasSeleccionadas.setText("Citas del " + 
            fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (todasLasCitas == null) {
            listCitasDia.getItems().clear();
            return;
        }

        List<String> citasDelDia = todasLasCitas.stream()
            .filter(cita -> cita.getFechaHora() != null)
            .filter(cita -> cita.getFechaHora().toLocalDate().equals(fecha))
            .sorted(Comparator.comparing(CitaDTO::getFechaHora))
            .map(cita -> {
                String hora = cita.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm"));
                String cliente = cita.getCliente() != null ? cita.getCliente().getNombre() : "Sin nombre";
                String servicio = "";
                if (cita.getServicios() != null && !cita.getServicios().isEmpty()) {
                    servicio = cita.getServicios().get(0).getNombre();
                }
                return hora + " - " + cliente + " (" + servicio + ")";
            })
            .collect(Collectors.toList());

        listCitasDia.setItems(javafx.collections.FXCollections.observableArrayList(citasDelDia));

        if (citasDelDia.isEmpty()) {
            listCitasDia.setPlaceholder(new Label("No hay citas programadas para este día"));
        }
    }

    @FXML
    private void mesAnterior() {
        mesActual = mesActual.minusMonths(1);
        mostrarCalendario();
        listCitasDia.getItems().clear();
        lblCitasSeleccionadas.setText("Selecciona un día");
    }

    @FXML
    private void mesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        mostrarCalendario();
        listCitasDia.getItems().clear();
        lblCitasSeleccionadas.setText("Selecciona un día");
    }

    /**
     * Obtener festivos para un año específico (cache)
     */
    private Set<LocalDate> obtenerFestivosAnio(int anio) {
        if (!festivosPorAnio.containsKey(anio)) {
            festivosPorAnio.put(anio, calcularFestivosAnio(anio));
        }
        return festivosPorAnio.get(anio);
    }

    /**
     * Calcular festivos de España para un año específico
     * Incluye festivos nacionales + Castilla-La Mancha + Albacete
     */
    private Set<LocalDate> calcularFestivosAnio(int anio) {
        Set<LocalDate> festivos = new HashSet<>();
        
        // ===== FESTIVOS FIJOS NACIONALES =====
        festivos.add(LocalDate.of(anio, 1, 1));   // Año Nuevo
        festivos.add(LocalDate.of(anio, 1, 6));   // Reyes Magos
        festivos.add(LocalDate.of(anio, 5, 1));   // Día del Trabajo
        festivos.add(LocalDate.of(anio, 8, 15));  // Asunción de la Virgen
        festivos.add(LocalDate.of(anio, 10, 12)); // Fiesta Nacional de España
        festivos.add(LocalDate.of(anio, 11, 1));  // Todos los Santos
        festivos.add(LocalDate.of(anio, 12, 6));  // Día de la Constitución
        festivos.add(LocalDate.of(anio, 12, 8));  // Inmaculada Concepción
        festivos.add(LocalDate.of(anio, 12, 25)); // Navidad
        
        // ===== FESTIVOS AUTONÓMICOS (Castilla-La Mancha) =====
        festivos.add(LocalDate.of(anio, 5, 31));  // Día de Castilla-La Mancha
        
        // ===== FESTIVOS LOCALES (Albacete) =====
        festivos.add(LocalDate.of(anio, 9, 8));   // Virgen de los Llanos
        
        // ===== FESTIVOS MÓVILES (Semana Santa) =====
        LocalDate viernes = calcularViernesSanto(anio);
        LocalDate jueves = viernes.minusDays(1);
        
        festivos.add(jueves);   // Jueves Santo
        festivos.add(viernes);  // Viernes Santo
        
        log.debug("Calculados {} festivos para el año {}", festivos.size(), anio);
        return festivos;
    }

    /**
     * Calcular Viernes Santo usando el algoritmo de Computus
     * (Viernes Santo varía cada año según el calendario lunar)
     */
    private LocalDate calcularViernesSanto(int anio) {
        // Algoritmo de Computus para calcular la fecha de Pascua
        int a = anio % 19;
        int b = anio / 100;
        int c = anio % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        
        // Domingo de Pascua
        LocalDate domingoPascua = LocalDate.of(anio, mes, dia);
        
        // Viernes Santo es 2 días antes del Domingo de Pascua
        return domingoPascua.minusDays(2);
    }
}