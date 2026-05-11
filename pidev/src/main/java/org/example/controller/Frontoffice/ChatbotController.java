package org.example.controller.Frontoffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entities.Programme;
import org.example.services.GeminiService;

import java.util.List;

public class ChatbotController {

    @FXML
    private VBox chatBox;
    @FXML
    private TextField messageInput;
    @FXML
    private ScrollPane scrollPane;

    private final GeminiService geminiService = new GeminiService();
    private List<Programme> programmes;
    private List<org.example.entities.Seance> seances;
    private boolean isRecommendationMode = false;

    public void setProgrammes(List<Programme> programmes) {
        this.programmes = programmes;
        this.isRecommendationMode = false;
        addMessage("Bonjour ! Je suis l'assistant IA de MindGrow. Comment puis-je vous aider aujourd'hui ?", false);
    }

    public void setSeances(List<org.example.entities.Seance> seances) {
        this.seances = seances;
        this.isRecommendationMode = true;
        addMessage(
                "Bonjour ! Je suis votre conseiller bien-être MindGrow. Dites-moi ce qui vous intéresse ou comment vous vous sentez, et je vous recommanderai la séance idéale.",
                false);
    }

    @FXML
    void handleSendMessage() {
        String userMessage = messageInput.getText().trim();
        if (userMessage.isEmpty())
            return;

        addMessage(userMessage, true);
        messageInput.clear();

        // Appel asynchrone pour ne pas bloquer l'UI
        new Thread(() -> {
            String response;
            if (isRecommendationMode) {
                response = geminiService.generateSeanceRecommendation(userMessage, seances);
            } else {
                response = geminiService.generateResponse(userMessage, programmes, seances);
            }
            Platform.runLater(() -> addMessage(response, false));
        }).start();
    }

    private void addMessage(String text, boolean isUser) {
        HBox messageContainer = new HBox();
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);

        if (isUser) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            messageLabel.setStyle(
                    "-fx-background-color: #0b7a8f; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 15 15 0 15;");
        } else {
            messageContainer.setAlignment(Pos.CENTER_LEFT);
            messageLabel.setStyle(
                    "-fx-background-color: #f1f1f1; -fx-text-fill: #2c3e50; -fx-padding: 10; -fx-background-radius: 15 15 15 0;");
        }

        messageContainer.getChildren().add(messageLabel);
        chatBox.getChildren().add(messageContainer);

        // Auto-scroll vers le bas
        chatBox.heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setVvalue(1.0));
    }
}
