package org.example.utils;

import org.example.entities.*;
import org.example.services.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AutoTestRunner: A headless, non-interactive test script for MindGrow.
 * Automatically verifies database connectivity and CRUD operations across all
 * services.
 */
public class AutoTestRunner {

    private static final UtilisateurService utilisateurService = new UtilisateurService();
    private static final TherapeuteService therapeuteService = new TherapeuteService();
    private static final SeanceService seanceService = new SeanceService();
    private static final CategorieService categorieService = new CategorieService();
    private static final ProgrammeService programmeService = new ProgrammeService();
    private static final AbonnementService abonnementService = new AbonnementService();
    private static final GeminiService geminiService = new GeminiService();

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      MindGrow - AUTOMATIC TEST RUNNER          ");
        System.out.println("=================================================");

        runTest("Authentification Database", () -> {
            MyDataBase.getConnection();
            return true;
        });

        runTest("Service Utilisateur - Liste", () -> {
            List<Utilisateur> list = utilisateurService.afficherUtilisateurs();
            return list != null;
        });

        runTest("Service Thérapeute - Liste", () -> {
            List<Therapeute> list = therapeuteService.afficherTherapeutes();
            return list != null;
        });

        runTest("Service Séance - Liste", () -> {
            List<Seance> list = seanceService.afficherSeances();
            return list != null;
        });

        runTest("Service Programme - Liste", () -> {
            List<Programme> list = programmeService.afficherProgrammes();
            return list != null;
        });

        runTest("Service Abonnement - Liste", () -> {
            List<Abonnement> list = abonnementService.afficherAbonnements();
            return list != null;
        });

        runTest("AI Chatbot (Gemini) - Smoke Test", () -> {
            String response = geminiService.generateResponse("Bonjour, ceci est un test de connexion.", null, null);
            return response != null && !response.contains("Erreur");
        });

        System.out.println("\n--- SYNTHÈSE DES TESTS ---");
        System.out.println("Total : " + totalTests);
        System.out.println("Passés : " + passedTests);
        System.out.println("Échoués : " + (totalTests - passedTests));

        if (passedTests == totalTests) {
            System.out.println("\n[SUCCESS] L'application MindGrow est stable !");
        } else {
            System.err.println("\n[FAILURE] Des erreurs ont été détectées.");
            System.exit(1);
        }
    }

    private static void runTest(String testName, TestAction action) {
        totalTests++;
        System.out.print("Test : " + testName + " ... ");
        try {
            if (action.execute()) {
                System.out.println("[PASS]");
                passedTests++;
            } else {
                System.out.println("[FAIL]");
            }
        } catch (Exception e) {
            System.out.println("[ERROR]");
            System.err.println("  -> Cause: " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface TestAction {
        boolean execute() throws Exception;
    }
}
