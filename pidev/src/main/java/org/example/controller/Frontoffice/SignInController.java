package org.example.controller.Frontoffice;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Utilisateur;
import org.example.services.GoogleAuthService;
import org.example.services.UtilisateurService;
import org.example.utils.AuthThemeManager;
import org.example.controller.Backoffice.AdminDashboardController;
import org.example.controller.Frontoffice.ClientDashboardController;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class SignInController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button themeToggleButton;

    // Nouveaux éléments pour le Captcha
    @FXML
    private ImageView captchaImageView;
    @FXML
    private TextField captchaField;
    private String currentCaptchaText;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        if (rootPane != null) {
            AuthThemeManager.applyDefaultTheme(rootPane);
            playEntranceAnimation(rootPane);
        }
        if (themeToggleButton != null) {
            themeToggleButton.setText(AuthThemeManager.getToggleLabel());
        }
        refreshCaptcha(null); // Afficher le Captcha dès le lancement
    }

    @FXML
    void toggleThemeMode(ActionEvent event) {
        AuthThemeManager.toggleTheme(rootPane);
        if (themeToggleButton != null) {
            themeToggleButton.setText(AuthThemeManager.getToggleLabel());
        }
        playButtonClickAnimation((Button) event.getSource());
    }

    @FXML
    void refreshCaptcha(ActionEvent event) {
        if (event != null && event.getSource() instanceof Button) {
            playButtonClickAnimation((Button) event.getSource());
        }
        currentCaptchaText = generateRandomString(5);
        BufferedImage image = createCaptchaImage(currentCaptchaText);
        Image fxImage = SwingFXUtils.toFXImage(image, null);
        if (captchaImageView != null) {
            captchaImageView.setImage(fxImage);
        }
        if (captchaField != null) {
            captchaField.clear();
        }
    }

    private void playEntranceAnimation(Node node) {
        if (node == null) return;
        node.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(360), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void playButtonClickAnimation(Button button) {
        if (button == null) return;
        ScaleTransition click = new ScaleTransition(Duration.millis(120), button);
        click.setFromX(1);
        click.setFromY(1);
        click.setToX(0.95);
        click.setToY(0.95);
        click.setAutoReverse(true);
        click.setCycleCount(2);
        click.play();
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

    private BufferedImage createCaptchaImage(String text) {
        int width = 120;
        int height = 45;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, height);

        Random r = new Random();
        g2d.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 6; i++) {
            g2d.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            g2d.drawLine(r.nextInt(width), r.nextInt(height), r.nextInt(width), r.nextInt(height));
        }

        g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24));
        for (int i = 0; i < text.length(); i++) {
            g2d.setColor(new Color(50 + r.nextInt(100), 50 + r.nextInt(100), 50 + r.nextInt(100)));
            int x = 15 + (i * 18);
            int y = 30 + r.nextInt(8) - 4;
            g2d.drawString(String.valueOf(text.charAt(i)), x, y);
        }

        g2d.dispose();
        return bufferedImage;
    }

    @FXML
    void handleSignIn(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            playButtonClickAnimation((Button) event.getSource());
        }
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String userCaptcha = captchaField != null ? captchaField.getText().trim() : "";

        if (email.isEmpty() || password.isEmpty() || userCaptcha.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs et le Captcha.");
            return;
        }

        if (!userCaptcha.equalsIgnoreCase(currentCaptchaText)) {
            errorLabel.setText("Captcha incorrect. Veuillez réessayer.");
            refreshCaptcha(null);
            return;
        }

        // Tentative d'authentification
        Utilisateur utilisateurConnecte = utilisateurService.authentifier(email, password);

        if (utilisateurConnecte != null) {
            connectUser(utilisateurConnecte, event);
        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            if (!utilisateurService.emailExiste(email)) {
                errorLabel.setText("Email introuvable. Vérifiez votre adresse.");
            } else {
                errorLabel.setText("Mot de passe incorrect. Vérifiez votre saisie.");
            }
        }
    }

    private void connectUser(Utilisateur u, ActionEvent event) {
        errorLabel.setStyle("-fx-text-fill: green;");
        errorLabel.setText("Connexion réussie ! Bienvenue " + u.getPrenom() + ".");

        try {
            FXMLLoader loader;
            Parent root;
            Scene scene;
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            if ("admin".equalsIgnoreCase(u.getRole())) {
                loader = new FXMLLoader(getClass().getResource("/Backoffice/AdminDashboard.fxml"));
                root = loader.load();
                AdminDashboardController adminControl = loader.getController();
                adminControl.initData(u);
                stage.setTitle("Espace Administrateur");
            } else {
                loader = new FXMLLoader(getClass().getResource("/Frontoffice/ClientDashboard.fxml"));
                root = loader.load();
                ClientDashboardController clientControl = loader.getController();
                clientControl.initData(u);
                stage.setTitle("Espace Client - MindGrow");
            }

            scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la redirection vers votre espace.");
        }
        System.out.println("Utilisateur connecté : " + u);
    }

    @FXML
    void goToSignUp(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            playButtonClickAnimation((Button) event.getSource());
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Frontoffice/SignUp.fxml"));
            AuthThemeManager.applyDefaultTheme(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement de la page d'inscription.");
        }
    }

    @FXML
    void goToForgotPassword(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            playButtonClickAnimation((Button) event.getSource());
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Frontoffice/ForgotPassword.fxml"));
            AuthThemeManager.applyDefaultTheme(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mot de passe oublié");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement de la page de récupération.");
        }
    }

    @FXML
    void handleGoogleAuth(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            playButtonClickAnimation((Button) event.getSource());
        }
        errorLabel.setStyle("-fx-text-fill: blue;");
        errorLabel.setText("Ouverture du navigateur pour connexion Google...");

        new Thread(() -> {
            GoogleAuthService googleAuth = new GoogleAuthService();
            String googleEmail = googleAuth.authenticateAndGetEmail();

            Platform.runLater(() -> {
                if (googleEmail != null) {
                    processGoogleLogin(googleEmail, event);
                } else {
                    errorLabel.setStyle("-fx-text-fill: red;");
                    errorLabel.setText("Échec ou annulation de la connexion Google.");
                }
            });
        }).start();
    }

    private void processGoogleLogin(String googleEmail, ActionEvent event) {
        System.out.println("Google Auth réussi pour : " + googleEmail);

        boolean exists = utilisateurService.emailExiste(googleEmail);
        Utilisateur u = null;

        if (exists) {
            for (Utilisateur util : utilisateurService.afficherUtilisateurs()) {
                if (util.getEmail().equalsIgnoreCase(googleEmail)) {
                    u = util;
                    break;
                }
            }
            if (u != null) {
                connectUser(u, event);
            } else {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Utilisateur Google trouvé, mais impossible de charger le profil.");
            }
            return;
        }

        String randomPassword = String.valueOf(System.currentTimeMillis());
        u = new Utilisateur("User_" + System.currentTimeMillis() % 1000, "Google", googleEmail, randomPassword,
                "client");

        String hash = BCrypt.hashpw(randomPassword, BCrypt.gensalt());
        u.setMotDePasse(hash);
        boolean inserted = utilisateurService.ajouterUtilisateur(u);
        if (!inserted) {
            System.err.println("Erreur lors de l'ajout du compte Google dans la BDD.");
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Impossible de créer le compte Google.");
            return;
        }

        for (Utilisateur util : utilisateurService.afficherUtilisateurs()) {
            if (util.getEmail().equalsIgnoreCase(googleEmail)) {
                u = util;
                break;
            }
        }

        if (u != null) {
            connectUser(u, event);
        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Erreur inattendue après connexion Google.");
        }
    }
}
