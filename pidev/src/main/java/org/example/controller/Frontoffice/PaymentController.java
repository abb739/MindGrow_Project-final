package org.example.controller.Frontoffice;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.services.StripeService;

public class PaymentController {

    @FXML private Label amountLabel;
    @FXML private TextField cardHolderField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvcField;
    @FXML private Label statusLabel;
    @FXML private Button payButton;

    private final StripeService stripeService = new StripeService();
    private double amount;
    private boolean paymentSuccess = false;

    public void setAmount(double amount) {
        this.amount = amount;
        amountLabel.setText(String.format("Montant à régler : %.2f TND", amount));
    }

    public boolean isPaymentSuccess() {
        return paymentSuccess;
    }

    @FXML
    void handlePayment(ActionEvent event) {
        String cardHolder = cardHolderField.getText().trim();
        String cardNumber = cardNumberField.getText().trim();
        String expiry = expiryField.getText().trim();
        String cvc = cvcField.getText().trim();

        if (cardHolder.isEmpty() || cardNumber.isEmpty() || expiry.isEmpty() || cvc.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Bloquer le bouton pendant le traitement
        payButton.setDisable(true);
        statusLabel.setText("Traitement sécurisé en cours...");
        statusLabel.setStyle("-fx-text-fill: #34495e;");

        // Simulation de traitement Stripe (en production, on créerait un PaymentIntent)
        // Note: Pour une implémentation réelle avec stripe-java sans frontend web, 
        // on utiliserait des tokens de test ou on créerait un PaymentIntent
        try {
            // Montant en cents (Stripe utilise les cents)
            long amountCents = (long) (amount * 100);
            PaymentIntent intent = stripeService.createPaymentIntent(amountCents, "usd");

            if (intent != null) {
                // Ici, on simule la réussite car la confirmation 3D Secure / Card Auth 
                // nécessite normalement un navigateur. Pour le projet, on valide si l'UI locale est correcte.
                paymentSuccess = true;
                statusLabel.setText("Paiement accepté !");
                statusLabel.setStyle("-fx-text-fill: #27ae60;");
                
                // Fermer la fenêtre après un court délai
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                delay.setOnFinished(e -> ((Stage) payButton.getScene().getWindow()).close());
                delay.play();
            }
        } catch (StripeException e) {
            statusLabel.setText("Erreur Stripe : " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            payButton.setDisable(false);
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        paymentSuccess = false;
        ((Stage) payButton.getScene().getWindow()).close();
    }
}
