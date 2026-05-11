package org.example.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.entities.Programme;
import org.example.entities.Seance;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

public class GeminiService {

    // Clé API Gemini configurée
    private static final String API_KEY = "AIzaSyC4JuMu69yhcfQLITA0rKLKEKw-QEVl6N4";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key="
            + API_KEY;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String generateResponse(String userMessage, List<Programme> programmes) {
        return generateResponse(userMessage, programmes, null);
    }

    public String generateTherapistRecommendation(String userInput, List<org.example.entities.Therapeute> therapeutes) {
        try {
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("Tu es un expert en orientation thérapeutique pour la plateforme 'MindGrow'. ");
            contextBuilder.append(
                    "Ton rôle est d'analyser les besoins, symptômes ou émotions de l'utilisateur et de lui recommander le thérapeute le plus adapté parmi la liste fournie. ");
            contextBuilder.append("Sois empathique, professionnel et concis.\n\n");
            contextBuilder.append("Voici la liste des thérapeutes disponibles :\n");

            for (org.example.entities.Therapeute t : therapeutes) {
                contextBuilder.append(
                        String.format("- %s %s (Spécialité: %s)\n", t.getNom(), t.getPrenom(), t.getSpecialite()));
            }

            contextBuilder.append("\nInstructions :\n");
            contextBuilder.append("1. Analyse le message de l'utilisateur.\n");
            contextBuilder.append("2. Identifie la spécialité requise.\n");
            contextBuilder.append("3. Recommande 1 ou 2 thérapeutes maximum en expliquant pourquoi.\n");
            contextBuilder.append(
                    "4. Si aucun ne correspond vraiment, suggère celui qui s'en rapproche le plus ou donne un conseil général.\n");

            return callGemini(contextBuilder.toString(), userInput);
        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur est survenue lors de la recommandation.";
        }
    }

    public String generateSeanceRecommendation(String userInput, List<Seance> seances) {
        try {
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append(
                    "Tu es un expert en bien-être MindGrow, spécialisé dans les séances de groupe et individuelles. ");
            contextBuilder.append(
                    "Ton rôle est d'écouter les intérêts, objectifs ou préoccupations de l'utilisateur et de lui recommander les séances les plus adaptées parmi la liste fournie. ");
            contextBuilder.append("Sois motivant, bienveillant et utilise un ton inspirant.\n\n");
            contextBuilder.append("Voici la liste des séances disponibles :\n");

            for (Seance s : seances) {
                contextBuilder.append(String.format("- %s (Lieu: %s, Date: %s): %s\n",
                        s.getTitre(), s.getLieu(), s.getDateDebut(), s.getDescription()));
            }

            contextBuilder.append("\nInstructions :\n");
            contextBuilder.append("1. Analyse les centres d'intérêt ou le moral de l'utilisateur.\n");
            contextBuilder.append("2. Sélectionne 1 à 3 séances pertinentes.\n");
            contextBuilder.append("3. Explique en quoi chaque séance choisie va l'aider spécifiquement.\n");
            contextBuilder.append("4. Termine par un message d'encouragement.\n");

            return callGemini(contextBuilder.toString(), userInput);
        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur est survenue lors de la génération de la recommandation de séance.";
        }
    }

    private String callGemini(String context, String userInput) {
        try {
            JsonObject body = new JsonObject();
            JsonArray contentsArray = new JsonArray();
            JsonObject contentObject = new JsonObject();
            JsonArray partsArray = new JsonArray();
            JsonObject textPart = new JsonObject();

            textPart.addProperty("text", context + "\n\nMessage de l'utilisateur : " + userInput);
            partsArray.add(textPart);
            contentObject.add("parts", partsArray);
            contentsArray.add(contentObject);
            body.add("contents", contentsArray);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                return jsonResponse.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            }
            return "Erreur API (" + response.statusCode() + ")";
        } catch (Exception e) {
            return "Erreur communication : " + e.getMessage();
        }
    }

    public String generateResponse(String userMessage, List<Programme> programmes,
            List<org.example.entities.Seance> seances) {
        try {
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("Tu es un assistant IA polyvalent et amical intégré à la plateforme 'MindGrow'. ");
            contextBuilder.append("Tu peux répondre à TOUTES les questions de l'utilisateur. ");

            if (programmes != null && !programmes.isEmpty()) {
                contextBuilder.append("\nVoici les programmes de développement disponibles :\n");
                contextBuilder.append(programmes.stream()
                        .map(p -> "- " + p.getTitre() + " (" + p.getCategorie().getNom() + "): " + p.getDescription())
                        .collect(Collectors.joining("\n")));
            }

            if (seances != null && !seances.isEmpty()) {
                contextBuilder.append("\nVoici les séances de planification prévues :\n");
                contextBuilder.append(seances.stream()
                        .map(s -> "- " + s.getTitre() + " à " + s.getLieu() + " le " + s.getDateDebut()
                                + ". Capacité restante: " + s.getCapacite())
                        .collect(Collectors.joining("\n")));
            }

            contextBuilder.append(
                    "\n\nRéponds de manière naturelle. Si la question n'a rien à voir avec MindGrow, réponds quand même normalement.");

            return callGemini(contextBuilder.toString(), userMessage);

        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur est survenue lors de la communication avec l'assistant : " + e.getMessage();
        }
    }
}
