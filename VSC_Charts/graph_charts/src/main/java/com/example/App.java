package com.example;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar la ventana principal desde el archivo FXML
            BorderPane root = FXMLLoader.load(getClass().getResource("/com/example/MainWindow.fxml"));
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Gráficos con JavaFX y XChart");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
