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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Utilisateur;
import org.example.services.UtilisateurService;
import org.example.utils.AuthThemeManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class SignUpController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ChoiceBox<String> roleChoiceBox;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressBar strengthBar;
    @FXML
    private Label strengthLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        AuthThemeManager.applyDefaultTheme(rootPane);
        playEntranceAnimation(rootPane);

        roleChoiceBox.getItems().addAll("client", "admin");
        roleChoiceBox.setValue("client"); // Valeur par défaut

        // Écouteurs pour validation en temps réel
        nomField.textProperty().addListener((obs, oldV, newV) -> validerNom(newV));
        prenomField.textProperty().addListener((obs, oldV, newV) -> validerPrenom(newV));
        emailField.textProperty().addListener((obs, oldV, newV) -> validerEmail(newV));
        passwordField.textProperty().addListener((obs, oldV, newV) -> validerPassword(newV));
    }

    private boolean validerNom(String nom) {
        if (nom.trim().isEmpty() || !nom.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$")) {
            nomField.setStyle("-fx-border-color: red; -fx-border-radius: 5;");
            if (!nom.trim().isEmpty()) errorLabel.setText("Le nom ne doit contenir que des lettres.");
            return false;
        }
        nomField.setStyle("-fx-border-color: green; -fx-border-radius: 5;");
        errorLabel.setText("");
        return true;
    }

    private boolean validerPrenom(String prenom) {
        if (prenom.trim().isEmpty() || !prenom.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$")) {
            prenomField.setStyle("-fx-border-color: red; -fx-border-radius: 5;");
            if (!prenom.trim().isEmpty()) errorLabel.setText("Le prénom ne doit contenir que des lettres.");
            return false;
        }
        prenomField.setStyle("-fx-border-color: green; -fx-border-radius: 5;");
        errorLabel.setText("");
        return true;
    }

    private boolean validerEmail(String email) {
        if (email.trim().isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            emailField.setStyle("-fx-border-color: red; -fx-border-radius: 5;");
            if (!email.trim().isEmpty()) errorLabel.setText("Veuillez entrer une adresse email valide.");
            return false;
        }
        emailField.setStyle("-fx-border-color: green; -fx-border-radius: 5;");
        errorLabel.setText("");
        return true;
    }

    private boolean validerPassword(String password) {
        double strength = 0;
        if (password.length() >= 8) strength += 0.25;
        if (password.matches(".*[A-Z].*")) strength += 0.25;
        if (password.matches(".*\\d.*")) strength += 0.25;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength += 0.25;

        strengthBar.setProgress(strength);

        if (strength <= 0.25) {
            strengthBar.setStyle("-fx-accent: red;");
            strengthLabel.setText("Force : Faible");
            strengthLabel.setStyle("-fx-text-fill: red;");
        } else if (strength <= 0.75) {
            strengthBar.setStyle("-fx-accent: orange;");
            strengthLabel.setText("Force : Moyenne");
            strengthLabel.setStyle("-fx-text-fill: orange;");
        } else {
            strengthBar.setStyle("-fx-accent: green;");
            strengthLabel.setText("Force : Forte");
            strengthLabel.setStyle("-fx-text-fill: green;");
        }

        if (password.trim().isEmpty() || password.length() < 8 || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            passwordField.setStyle("-fx-border-color: red; -fx-border-radius: 5;");
            if (!password.trim().isEmpty()) {
                if (password.length() < 8) errorLabel.setText("Mot de passe : 8 caractères minimum.");
                else if (!password.matches(".*\\d.*")) errorLabel.setText("Mot de passe : au moins 1 chiffre.");
                else errorLabel.setText("Mot de passe : au moins 1 caractère spécial.");
            }
            return false;
        }
        passwordField.setStyle("-fx-border-color: green; -fx-border-radius: 5;");
        errorLabel.setText("");
        return true;
    }

    @FXML
    void handleSignUp(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            playButtonClickAnimation((Button) event.getSource());
        }
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleChoiceBox.getValue();

        // Validation globale avant soumission
        boolean isNomValid = validerNom(nom);
        boolean isPrenomValid = validerPrenom(prenom);
        boolean isEmailValid = validerEmail(email);
        boolean isPasswordValid = validerPassword(password);

        if (!isNomValid || !isPrenomValid || !isEmailValid || !isPasswordValid) {
            errorLabel.setText("Veuillez corriger les erreurs avant de continuer.");
            return;
        }

        // Force role to 'client' only (match Symfony behavior)
        if (role == null || !"client".equalsIgnoreCase(role)) {
            role = "client";
        }

        // Création de l'utilisateur (rôle forcé à 'client')
        Utilisateur nouvelUtilisateur = new Utilisateur(nom, prenom, email, password, role);

        // Tentative d'inscription
        boolean success = utilisateurService.inscrire(nouvelUtilisateur);

        if (success) {
            // Générer les données pour le QR Code
            String qrData = String.format("Nom: %s\nPrénom: %s\nEmail: %s\nMot de passe: %s", nom, prenom, email, password);
            Image qrImage = generateQRCodeImage(qrData);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Inscription Réussie");
            alert.setHeaderText("Félicitations " + prenom + " !");
            alert.setContentText("Votre compte a été créé avec succès.\nScannez ce code pour conserver vos informations :");

            if (qrImage != null) {
                ImageView imageView = new ImageView(qrImage);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                alert.setGraphic(imageView);
            }

            alert.showAndWait();

            // Redirection vers la connexion
            goToSignIn(event);
        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Cet email est déjà utilisé !");
        }
    }

    @FXML
    void goToSignIn(ActionEvent event) {
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
            errorLabel.setText("Erreur lors du chargement de la page de connexion.");
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

    private Image generateQRCodeImage(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
