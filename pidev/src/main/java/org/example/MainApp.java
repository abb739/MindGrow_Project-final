package org.example;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    private BorderPane root;
    private Scene scene;
    private boolean darkMode = false;
    private Button themeToggleButton;

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new BorderPane();
        root.getStyleClass().add("theme-light");

        scene = new Scene(root, 1400, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        showDashboard();

        primaryStage.setTitle("MindGrow - Bienvenue");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void showDashboard() {
        VBox dashboard = new VBox(30);
        dashboard.setAlignment(Pos.TOP_CENTER);
        dashboard.setPadding(new Insets(60));
        dashboard.getStyleClass().add("body-panel");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_RIGHT);
        topRow.setPrefWidth(Double.MAX_VALUE);

        themeToggleButton = new Button(darkMode ? "☀ Mode clair" : "🌙 Mode sombre");
        themeToggleButton.getStyleClass().add("theme-toggle-button");
        themeToggleButton.setOnAction(e -> {
            toggleTheme();
            playClickAnimation(themeToggleButton);
        });
        topRow.getChildren().add(themeToggleButton);

        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("🌱 Bienvenue sur MindGrow");
        welcomeLabel.getStyleClass().add("hero-title");

        Label subtitleLabel = new Label("STAYS • EXPERIENCES • EVENTS");
        subtitleLabel.getStyleClass().add("hero-subtitle");

        headerBox.getChildren().addAll(welcomeLabel, subtitleLabel);

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);

        VBox cardClients = createStatCard("👤", "Clients", "Gérer vos utilisateurs");
        statsBox.getChildren().addAll(cardClients);

        VBox actionsBox = new VBox(20);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setMaxWidth(600);

        Label actionsTitle = new Label("Choisissez votre interface");
        actionsTitle.getStyleClass().add("section-title");

        Button btnBackOffice = createActionButton("🔧 BackOffice (Administration)", "primary", () -> showBackOffice());
        Button btnFrontOffice = createActionButton("🏖️ FrontOffice (Réservations Utilisateurs)", "accent", () -> showFrontOffice());

        actionsBox.getChildren().addAll(actionsTitle, btnBackOffice, btnFrontOffice);

        dashboard.getChildren().addAll(topRow, headerBox, statsBox, actionsBox);
        showContent(dashboard);
    }

    private void showBackOffice() {
        try {
            System.out.println("Chargement du BackOffice...");
            BorderPane backOfficePane = new BorderPane();

            HBox header = new HBox(20);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(20));
            header.getStyleClass().add("backoffice-header");

            Button btnBack = createActionButton("⬅ Retour au Dashboard", "secondary", this::showDashboard);
            btnBack.setStyle("-fx-background-color: #D2691E; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 10 20; -fx-cursor: hand;");
            btnBack.setMinWidth(180);

            Label headerTitle = new Label("BackOffice - Administration");
            headerTitle.getStyleClass().add("section-title");
            headerTitle.setStyle("-fx-text-fill: white;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            header.getChildren().addAll(btnBack, spacer, headerTitle);
            backOfficePane.setTop(header);

            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            backOfficePane.setCenter(tabPane);
            showContent(backOfficePane);

            System.out.println("BackOffice chargé avec succès!");

        } catch (Exception e) {
            showError("Impossible de charger le BackOffice", e);
        }
    }

    private void showFrontOffice() {
        try {
            System.out.println("Chargement du FrontOffice...");
            BorderPane frontOfficePane = new BorderPane();

            HBox header = new HBox(20);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(20));
            header.getStyleClass().add("frontoffice-header");

            Button btnBack = createActionButton("⬅ Retour au Dashboard", "secondary", this::showDashboard);
            btnBack.setStyle("-fx-background-color: #D2691E; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-padding: 10 20; -fx-cursor: hand;");
            btnBack.setMinWidth(180);

            Label headerTitle = new Label("FrontOffice - Réservations");
            headerTitle.getStyleClass().add("section-title");
            headerTitle.setStyle("-fx-text-fill: white;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            header.getChildren().addAll(btnBack, spacer, headerTitle);
            frontOfficePane.setTop(header);

            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            frontOfficePane.setCenter(tabPane);
            showContent(frontOfficePane);

            System.out.println("FrontOffice chargé avec succès!");

        } catch (Exception e) {
            showError("Impossible de charger le FrontOffice", e);
        }
    }

    private Button createActionButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-button", styleClass);
        button.setPrefWidth(350);
        button.setPrefHeight(60);
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.03);
            button.setScaleY(1.03);
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1);
            button.setScaleY(1);
        });
        button.setOnAction(e -> {
            playClickAnimation(button);
            action.run();
        });
        return button;
    }

    private VBox createStatCard(String icon, String title, String description) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(250);
        card.setPrefHeight(180);
        card.getStyleClass().add("stat-card");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 56px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0B7A8F;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        card.setOnMouseEntered(e -> {
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1);
            card.setScaleY(1);
        });
        card.setOnMouseClicked(e -> playClickAnimation(card));

        return card;
    }

    private void showContent(Node content) {
        Node oldContent = root.getCenter();
        if (oldContent != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(180), oldContent);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                root.setCenter(content);
                playEntranceAnimation(content);
            });
            fadeOut.play();
        } else {
            root.setCenter(content);
            playEntranceAnimation(content);
        }
    }

    private void playEntranceAnimation(Node node) {
        node.setOpacity(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(280), node);
        slide.setFromY(20);
        slide.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(280), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition transition = new ParallelTransition(node, slide, fadeIn);
        transition.play();
    }

    private void playClickAnimation(Node node) {
        ScaleTransition click = new ScaleTransition(Duration.millis(140), node);
        click.setFromX(1);
        click.setFromY(1);
        click.setToX(0.96);
        click.setToY(0.96);
        click.setAutoReverse(true);
        click.setCycleCount(2);
        click.play();
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        root.getStyleClass().removeAll("theme-light", "theme-dark");
        root.getStyleClass().add(darkMode ? "theme-dark" : "theme-light");
        if (themeToggleButton != null) {
            themeToggleButton.setText(darkMode ? "☀ Mode clair" : "🌙 Mode sombre");
        }
    }

    private void showError(String title, Exception e) {
        System.err.println("ERREUR: " + title);
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText("Erreur: " + e.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
