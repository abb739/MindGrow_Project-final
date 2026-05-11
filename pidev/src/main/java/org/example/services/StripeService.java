package org.example.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

public class StripeService {

    // Clés fournies par l'utilisateur
    private static final String SECRET_KEY = "sk_test_51T65va2NCpk8FQWj8xYqY13ZS1TSI7aR7fCLSeSyAKVR0UtAINHlPW9kBv6CP0Eg8harf4hDBQbAiYjZOkzdEcLq00a0k89fyS";

    public StripeService() {
        Stripe.apiKey = SECRET_KEY;
    }

    public PaymentIntent createPaymentIntent(long amountInCents, String currency) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                // On peut ajouter des métadonnées ici si besoin
                .build();

        return PaymentIntent.create(params);
    }

    public boolean confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            // Dans un environnement de test simple, on vérifie juste le statut
            // Pour une vraie confirmation, Stripe recommande d'utiliser des webhooks ou de confirmer côté client
            return "succeeded".equals(intent.getStatus());
        } catch (StripeException e) {
            System.err.println("Erreur confirmation Stripe : " + e.getMessage());
            return false;
        }
    }
}
