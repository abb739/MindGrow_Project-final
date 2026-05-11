package org.example.controller.Frontoffice;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Utilisateur;
import org.example.services.UtilisateurService;
import org.example.utils.AuthThemeManager;

import java.io.IOException;
import java.util.Random;

public class ForgotPasswordController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private Label errorMessage;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        AuthThemeManager.applyDefaultTheme(rootPane);
        playEntranceAnimation(rootPane);
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            playButtonClickAnimation((Button) event.getSource());
        }
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();

        if (nom.isEmpty() || email.isEmpty()) {
            errorMessage.setText("Veuillez remplir votre nom et votre e-mail.");
            return;
        }

        // Vérifier si l'utilisateur existe avec ce Nom ET avec cet Email
        boolean nomCorrespond = false;
        for (Utilisateur u : utilisateurService.afficherUtilisateurs()) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getNom().equalsIgnoreCase(nom)) {
                nomCorrespond = true;
                break;
            }
        }

        if (!nomCorrespond) {
            errorMessage.setText("Les informations saisies sont incorrectes ou introuvables.");
            return;
        }

        // Générer un nouveau mot de passe temporaire
        String tempPassword = generateRandomString(8) + "1@A"; // S'assure qu'il passe les validations futures (lettre
                                                               // min, maj, chiffre, spé)

        // Mettre à jour en BDD avec le nouveau mot de passe haché
        boolean modifie = utilisateurService.modifierMotDePasseParEmail(email, tempPassword);

        if (modifie) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mot de passe réinitialisé");
            alert.setHeaderText("Succès !");
            alert.setContentText("Votre mot de passe a été réinitialisé avec succès.\n\n" +
                    "Voici votre nouveau mot de passe temporaire :\n" +
                    "==> " + tempPassword + " <==\n\n" +
                    "Veuillez le copier et le modifier après votre connexion.");
            alert.showAndWait();

            // Retour à la page de connexion
            backToLogin(event);
        } else {
            errorMessage.setText("Erreur inattendue lors de la mise à jour de la base de données.");
        }
    }

    @FXML
    void backToLogin(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            playButtonClickAnimation((Button) event.getSource());
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Frontoffice/SignIn.fxml"));
            AuthThemeManager.applyDefaultTheme(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playEntranceAnimation(Node node) {
        if (node == null) return;
        node.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(280), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void playButtonClickAnimation(Button button) {
        if (button == null) return;
        ScaleTransition click = new ScaleTransition(Duration.millis(120), button);
        click.setFromX(1);
        click.setFromY(1);
        click.setToX(0.96);
        click.setToY(0.96);
        click.setAutoReverse(true);
        click.setCycleCount(2);
        click.play();
    }
}
