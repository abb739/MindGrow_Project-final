# MindGrow — Mental Health & Wellness Desktop Application

> A JavaFX desktop application for managing mental health and wellness services, providing a rich GUI interface for both clients and administrators with AI integration, payment processing, and real-time notifications.

---

## Description

**MindGrow Desktop** is the Java counterpart of the MindGrow platform, built with **JavaFX** and connected to the same **MySQL** database. It provides a full-featured graphical user interface for managing wellness programmes, therapy sessions, subscriptions, therapist profiles, and user accounts — all powered by AI recommendations and Stripe payments.

The application supports two modes: a **Frontoffice** for end users and a **Backoffice** for administrators, each with dedicated controllers and views.

---

## Features

### Authentication & User Management
- **Sign In / Sign Up** with form validation
- **Google OAuth 2.0** social login
- **Forgot password** flow with email token reset
- **bcrypt** password hashing
- Role-based navigation (`admin` / `client`)
- Light / Dark **theme switching** (AuthThemeManager)

### Programme Management
- Full **CRUD** for wellness programmes (backoffice)
- **Category** creation and assignment
- Image upload and display
- **Favorites** system (FavoriService)
- Client-side browsing and programme detail views

### Therapist Management
- Full **CRUD** for therapist profiles (backoffice)
- Speciality and contact management
- **Avis (reviews)** with star ratings
- Profanity filtering via **WordFilter** utility
- **Translation** of therapist content (TranslationService)

### Session Management
- Full **CRUD** for wellness sessions
- Interactive **calendar view** (SeanceCalendarController)
- Session capacity and scheduling
- **Reservation** creation and management
- Timed session alerts (TimerDialogController)

### Subscription & Payment
- Subscription plan browsing (frontoffice)
- **Stripe payment integration** (StripeService)
- Purchase history and active subscription tracking
- **PDF receipt** generation (PdfGenerator)
- Automatic **email notifications** (EmailService)

### AI Chatbot (Google Gemini)
- Integrated **Gemini AI chatbot** (ChatbotController + GeminiService)
- Wellness Q&A and programme recommendations
- Natural language interaction within the desktop UI

### Admin Dashboard
- Overview statistics dashboard (DashboardController)
- Full management of users, therapists, programmes, sessions, subscriptions, and reviews
- **Notification system** (NotificationUtils)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17+ |
| UI Framework | JavaFX |
| Build Tool | Maven |
| Database | MySQL 8 |
| Database Access | JDBC (MyDataBase utility) |
| AI | Google Gemini API (GeminiService) |
| Payments | Stripe Java SDK (StripeService) |
| Auth | Google OAuth 2.0 (GoogleAuthService) |
| Email | JavaMail / SMTP (EmailService) |
| PDF | iText / custom PdfGenerator |
| Word Filter | Custom WordFilter utility |
| Translation | TranslationService (external API) |

---

## Installation

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- MySQL 8 server running
- JavaFX SDK

### Setup

```bash
# Clone the repository
git clone https://github.com/abb739/MindGrow_Project-final.git
cd MindGrow_Project-final/pidev

# Configure database connection
# Edit src/main/java/org/example/utils/MyDataBase.java
# Set your MySQL host, port, database name, username, and password

# Build the project
mvn clean install

# Run the application
mvn javafx:run
```

---

## Project Structure

```
src/main/java/org/example/
├── MainApp.java                         # Application entry point
├── controller/
│   ├── Backoffice/                      # Admin controllers
│   │   ├── AdminDashboardController.java
│   │   ├── ProgrammeController.java
│   │   ├── SeanceController.java
│   │   ├── TherapeuteController.java
│   │   ├── AbonnementController.java
│   │   └── ReservationSeanceManageController.java
│   └── Frontoffice/                     # Client controllers
│       ├── SignInController.java
│       ├── SignUpController.java
│       ├── ForgotPasswordController.java
│       ├── HomeFrontController.java
│       ├── ClientDashboardController.java
│       ├── ProgrammeFrontController.java
│       ├── SeanceFrontController.java
│       ├── SeanceCalendarController.java
│       ├── TherapeuteFrontController.java
│       ├── AbonnementFrontController.java
│       ├── PaymentController.java
│       ├── ChatbotController.java
│       └── TimerDialogController.java
├── entities/                            # Data models
│   ├── Utilisateur.java
│   ├── Programme.java
│   ├── Categorie.java
│   ├── Seance.java
│   ├── ReservationSeance.java
│   ├── Therapeute.java
│   ├── Avis.java
│   ├── Abonnement.java
│   └── Achat.java
├── services/                            # Business logic
│   ├── UtilisateurService.java
│   ├── ProgrammeService.java
│   ├── CategorieService.java
│   ├── FavoriService.java
│   ├── SeanceService.java
│   ├── ReservationSeanceService.java
│   ├── TherapeuteService.java
│   ├── AvisService.java
│   ├── AbonnementService.java
│   ├── AchatService.java
│   ├── StripeService.java
│   ├── GeminiService.java
│   ├── GoogleAuthService.java
│   ├── EmailService.java
│   └── TranslationService.java
└── utils/                               # Utilities
    ├── MyDataBase.java
    ├── PdfGenerator.java
    ├── WordFilter.java
    ├── NotificationUtils.java
    ├── AuthThemeManager.java
    └── UploadPathResolver.java
```

---

## Database

The application shares the same **MySQL `mindgrow` database** with the Symfony web application. Tables include:

`utilisateur` · `programme` · `categorie` · `seance` · `reservation_seance` · `therapeute` · `avis` · `abonnement` · `achat` · `favori_programme`

---

## Topics & Keywords

`java` `javafx` `mental-health` `wellness` `desktop-application` `gemini-ai` `stripe-payments` `jdbc` `mysql` `google-oauth` `chatbot` `therapist-management` `session-booking` `subscription` `pdf-generation` `javafx-application` `maven` `bcrypt` `email-service` `crud`

---

## Authors

- **MindGrow Team** — PIDEV 3A Project
- Developed as part of the academic year 2025–2026
