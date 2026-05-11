package org.example.utils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.utils.AuthThemeManager;

import java.net.URL;

public class TestUtilisateurApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL resource = getClass().getResource("/Frontoffice/SignIn.fxml");
        if (resource == null) {
            System.err.println(
                    "Fichier fxml introuvable. Vérifiez que SignIn.fxml est bien dans src/main/resources/Frontoffice/");
            return;
        }
        Parent root = FXMLLoader.load(resource);
        AuthThemeManager.applyDefaultTheme(root);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/Frontoffice/front.css").toExternalForm());

        primaryStage.setTitle("Plateforme MindGrow - Connexion / Inscription");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
