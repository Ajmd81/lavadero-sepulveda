package com.lavaderosepulveda.crm.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StageManager {
    
    private static StageManager instance;
    private Stage primaryStage;
    private final Map<String, Parent> cachedViews = new HashMap<>();
    
    private StageManager() {}
    
    public static synchronized StageManager getInstance() {
        if (instance == null) {
            instance = new StageManager();
        }
        return instance;
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public void switchScene(String fxmlPath) {
        try {
            Parent root = loadView(fxmlPath);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            log.error("Error al cambiar de escena: " + fxmlPath, e);
        }
    }
    
    public void switchScene(String fxmlPath, String title) {
        primaryStage.setTitle(title);
        switchScene(fxmlPath);
    }
    
    public void openDialog(String fxmlPath, String title) {
        try {
            Parent root = loadView(fxmlPath);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            log.error("Error al abrir diálogo: " + fxmlPath, e);
        }
    }
    
    public <T> T loadController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }
    
    private Parent loadView(String fxmlPath) throws IOException {
        // Cachear vistas principales para mejor rendimiento
        if (cachedViews.containsKey(fxmlPath)) {
            return cachedViews.get(fxmlPath);
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        
        // Cachear solo vistas principales
        if (fxmlPath.contains("main") || fxmlPath.contains("dashboard")) {
            cachedViews.put(fxmlPath, root);
        }
        
        return root;
    }
    
    public void clearCache() {
        cachedViews.clear();
    }
}
