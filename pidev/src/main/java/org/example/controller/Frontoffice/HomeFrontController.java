package org.example.controller.Frontoffice;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HomeFrontController {

    @FXML
    private ImageView heroImage;
    @FXML
    private Label progCountLabel;
    @FXML
    private Label seanceCountLabel;
    @FXML
    private Label therapeuteCountLabel;

    private ClientDashboardController parentController;

    @FXML
    public void initialize() {
        // Fallback or random image for hero
        try {
            heroImage.setImage(new Image(getClass().getResourceAsStream("/Frontoffice/hero_bg.jpg")));
        } catch (Exception e) {
            System.out.println("Hero image not found, using default styling.");
        }

        // Load some stats (placeholder logic if real counts aren't easily available)
        // In a real app, we'd call service methods like countAll()
        progCountLabel.setText("Plus de 10 programmes");
        seanceCountLabel.setText("Prochaines sessions disponibles");
        therapeuteCountLabel.setText("Experts certifiés");
    }

    public void setParentController(ClientDashboardController parentController) {
        this.parentController = parentController;
    }

    @FXML
    void handleExploreProgrammes(ActionEvent event) {
        if (parentController != null) {
            parentController.showProgrammesView(event);
        }
    }

    @FXML
    void handleExploreSeances(ActionEvent event) {
        if (parentController != null) {
            parentController.showSeancesView(event);
        }
    }

    @FXML
    void handleExploreTherapeutes(ActionEvent event) {
        if (parentController != null) {
            parentController.showTherapeutesView(event);
        }
    }
}
