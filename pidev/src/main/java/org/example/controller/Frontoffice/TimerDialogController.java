package org.example.controller.Frontoffice;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class TimerDialogController {

    @FXML
    private Label timerLabel;
    @FXML
    private TextField minutesInput;
    @FXML
    private Button startBtn;
    @FXML
    private Button pauseBtn;

    private Timeline timeline;
    private int secondsRemaining;

    @FXML
    public void initialize() {
        // Validation: allow only numbers in the input
        minutesInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                minutesInput.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    void handleStart(ActionEvent event) {
        if (timeline != null && timeline.getStatus() == Timeline.Status.PAUSED) {
            timeline.play();
            updateUI(true);
            return;
        }

        String input = minutesInput.getText();
        if (input.isEmpty() || Integer.parseInt(input) <= 0) {
            showAlert("Erreur", "Veuillez entrer un nombre de minutes valide.");
            return;
        }

        secondsRemaining = Integer.parseInt(input) * 60;
        startTimer();
    }

    private void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            updateLabel();
            if (secondsRemaining <= 0) {
                timeline.stop();
                handleTimerFinished();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateUI(true);
    }

    @FXML
    void handlePause(ActionEvent event) {
        if (timeline != null) {
            timeline.pause();
            updateUI(false);
        }
    }

    @FXML
    void handleReset(ActionEvent event) {
        if (timeline != null) {
            timeline.stop();
        }
        secondsRemaining = 0;
        updateLabel();
        updateUI(false);
        minutesInput.clear();
    }

    private void updateLabel() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateUI(boolean running) {
        startBtn.setDisable(running);
        pauseBtn.setDisable(!running);
        minutesInput.setDisable(running);
    }

    private void handleTimerFinished() {
        updateUI(false);
        showAlert("Temps écoulé !", "Votre séance est terminée. Bravo !");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
