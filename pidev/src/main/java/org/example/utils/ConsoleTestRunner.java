package org.example.utils;

import org.example.entities.*;
import org.example.services.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * ConsoleTestRunner: A comprehensive test script for MindGrow application.
 * Allows testing all core services via console interaction.
 */
public class ConsoleTestRunner {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UtilisateurService utilisateurService = new UtilisateurService();
    private static final TherapeuteService therapeuteService = new TherapeuteService();
    private static final SeanceService seanceService = new SeanceService();
    private static final CategorieService categorieService = new CategorieService();
    private static final ProgrammeService programmeService = new ProgrammeService();
    private static final AbonnementService abonnementService = new AbonnementService();
    private static final GeminiService geminiService = new GeminiService();

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      MindGrow - CONSOLE TEST RUNNER           ");
        System.out.println("=================================================");

        boolean exit = false;
        while (!exit) {
            printMenu();
            int choice = readInt("Votre choix : ");
            switch (choice) {
                case 1:
                    testUtilisateur();
                    break;
                case 2:
                    testTherapeute();
                    break;
                case 3:
                    testSeance();
                    break;
                case 4:
                    testProgramme();
                    break;
                case 5:
                    testAbonnement();
                    break;
                case 6:
                    testChatbot();
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Choix invalide !");
            }
        }
        System.out.println("Fin du test. Au revoir !");
    }

    private static void printMenu() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Tester Gestion Utilisateurs");
        System.out.println("2. Tester Gestion Thérapeutes");
        System.out.println("3. Tester Gestion Séances");
        System.out.println("4. Tester Gestion Programmes & Catégories");
        System.out.println("5. Tester Gestion Abonnements");
        System.out.println("6. Tester Assistant IA (Chatbot)");
        System.out.println("0. Quitter");
    }

    // --- TEST UTILISATEUR ---
    private static void testUtilisateur() {
        System.out.println("\n--- [TEST UTILISATEUR] ---");
        System.out.println("1. Afficher tous les utilisateurs");
        System.out.println("2. Ajouter un utilisateur test");
        int subChoice = readInt("Action : ");
        if (subChoice == 1) {
            utilisateurService.afficherUtilisateurs().forEach(System.out::println);
        } else if (subChoice == 2) {
            Utilisateur u = new Utilisateur(0, "Test", "User", "test" + System.currentTimeMillis() + "@mindgrow.com",
                    "password123", null, "client");
            utilisateurService.ajouterUtilisateur(u);
        }
    }

    // --- TEST THERAPEUTE ---
    private static void testTherapeute() {
        System.out.println("\n--- [TEST THÉRAPEUTE] ---");
        System.out.println("1. Afficher tous les thérapeutes");
        System.out.println("2. Ajouter un thérapeute test");
        int subChoice = readInt("Action : ");
        if (subChoice == 1) {
            therapeuteService.afficherTherapeutes().forEach(System.out::println);
        } else if (subChoice == 2) {
            Therapeute t = new Therapeute(0, "Dr.", "Expert", "logo.png", "cert.pdf", "Psychologie",
                    "expert@mindgrow.com", "12345678", LocalDateTime.now());
            therapeuteService.ajouterTherapeute(t);
            System.out.println("Thérapeute ajouté !");
        }
    }

    // --- TEST SEANCE ---
    private static void testSeance() {
        System.out.println("\n--- [TEST SÉANCE] ---");
        System.out.println("1. Afficher toutes les séances");
        System.out.println("2. Ajouter une séance test");
        int subChoice = readInt("Action : ");
        if (subChoice == 1) {
            seanceService.afficherSeances().forEach(System.out::println);
        } else if (subChoice == 2) {
            Seance s = new Seance(0, "Méditation de Groupe", "Session de test", "Salle 1", LocalDateTime.now(),
                    LocalDateTime.now().plusHours(1), 20, "seance.jpg");
            seanceService.ajouterSeance(s);
        }
    }

    // --- TEST PROGRAMME ---
    private static void testProgramme() {
        System.out.println("\n--- [TEST PROGRAMME & CATÉGORIE] ---");
        System.out.println("1. Afficher toutes les catégories");
        System.out.println("2. Afficher tous les programmes");
        System.out.println("3. Ajouter une catégorie + programme test");
        int subChoice = readInt("Action : ");
        if (subChoice == 1) {
            categorieService.afficherCategories().forEach(System.out::println);
        } else if (subChoice == 2) {
            programmeService.afficherProgrammes().forEach(System.out::println);
        } else if (subChoice == 3) {
            Categorie cat = new Categorie(0, "Bien-être", "Catégorie de test");
            categorieService.ajouterCategorie(cat);
            // On récupère la catégorie ajoutée (normalement on devrait chercher l'ID, mais
            // on prend la dernière pour le test)
            List<Categorie> cats = categorieService.afficherCategories();
            Categorie lastCat = cats.get(cats.size() - 1);
            Programme p = new Programme(0, lastCat, "Sophrologie Découverte", "Apprendre à respirer", "sophro.png",
                    "https://youtube.com");
            programmeService.ajouterProgramme(p);
        }
    }

    // --- TEST ABONNEMENT ---
    private static void testAbonnement() {
        System.out.println("\n--- [TEST ABONNEMENT] ---");
        System.out.println("1. Afficher tous les abonnements");
        System.out.println("2. Ajouter un abonnement test");
        int subChoice = readInt("Action : ");
        if (subChoice == 1) {
            abonnementService.afficherAbonnements().forEach(System.out::println);
        } else if (subChoice == 2) {
            Abonnement a = new Abonnement(0, "Pack Premium", "Accès illimité", 49.99, 12);
            abonnementService.ajouterAbonnement(a);
        }
    }

    // --- TEST CHATBOT ---
    private static void testChatbot() {
        System.out.println("\n--- [TEST ASSISTANT IA] ---");
        System.out.print("Posez votre question à l'IA MindGrow : ");
        scanner.nextLine(); // Consume newline
        String question = scanner.nextLine();

        System.out.println("L'IA réfléchit...");
        List<Programme> progs = programmeService.afficherProgrammes();
        List<Seance> seances = seanceService.afficherSeances();

        String response = geminiService.generateResponse(question, progs, seances);
        System.out.println("\nAssistant IA : " + response);
    }

    // --- UTILS ---
    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Veuillez entrer un nombre valide.");
            scanner.next();
        }
        return scanner.nextInt();
    }
}
