# MindGrow — Mental Health & Wellness Web Platform

> A full-stack web application built with **Symfony 7** for managing mental health and wellness services, connecting users with therapists, programmes, and sessions through an AI-powered platform.

---

## Description

**MindGrow** is a comprehensive mental health and wellness management platform that provides a complete digital ecosystem for both clients and administrators. The platform allows users to discover and enroll in wellness programmes, book therapy sessions, subscribe to health plans, and interact with an intelligent AI chatbot — all within a secure, modern web interface.

The application is built with a clean role-based architecture distinguishing between **client** users and **administrators**, each with dedicated dashboards and feature sets.

---

## Features

### Authentication & User Management
- Secure registration with **email verification**
- **bcrypt** password hashing
- Token-based **password reset** via email
- **Google reCAPTCHA v2** bot protection
- Role-based access control (`admin` / `client`)
- Light / Dark / Auto **theme preference**

### Programme Management
- Full **CRUD** for wellness programmes (admin)
- **Category-based** organization (yoga, meditation, fitness, medical...)
- Image and video **file upload** support
- **AI-generated descriptions** via Google Gemini
- **AI translation** of programme content
- **PDF export** of programme details
- Client-side browse, search, filter, and **favorites system**

### Therapist Management
- Full **CRUD** for therapist profiles (admin)
- **Certificate upload** with Google Vision AI validation
- Speciality management and **average rating** display
- Client **reviews and ratings** with profanity filtering (PurgoMalum API)

### Session Management
- Full **CRUD** for wellness sessions
- Location, date, time, and **capacity management**
- **Session booking** with PDF ticket generation

### Subscription & Payment
- Multiple **subscription plans**
- Secure payments via **Stripe**
- Automatic **PDF receipt** generation
- **Multi-currency** support
- Purchase history tracking

### AI Chatbot (Google Gemini)
- General **wellness Q&A**
- **Programme recommendations** based on favorites
- **Therapist matching** by user needs
- **Session suggestions**
- **Subscription guidance**

### Admin Dashboard
- Real-time statistics (users, therapists, sessions, subscriptions)
- Full management of all platform entities
- Therapist performance analytics

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Symfony 7 |
| Language | PHP 8.2 |
| Database | MySQL 8 |
| ORM | Doctrine ORM |
| Frontend | Twig, Bootstrap, JavaScript |
| AI | Google Gemini API |
| Payments | Stripe API |
| Email | Gmail SMTP (Symfony Mailer) |
| Image AI | Google Vision API |
| Maps | Google Maps API |
| Auth Guard | Google reCAPTCHA v2 |
| PDF | DomPDF |
| QR Code | endroid/qr-code |

---

## Installation

```bash
# Clone the repository
git clone https://github.com/abb739/MindGrow_Project-final.git
cd MindGrow_Project-final

# Install PHP dependencies
composer install

# Configure environment
cp .env .env.local
# Edit .env.local with your database, API keys, and SMTP credentials

# Create database and run migrations
php bin/console doctrine:database:create
php bin/console doctrine:migrations:migrate

# Clear cache
php bin/console cache:clear

# Start development server
symfony server:start
```

---

## Environment Variables

```env
DATABASE_URL=mysql://root:@127.0.0.1:3306/mindgrow
GEMINI_API_KEY=your_gemini_api_key
STRIPE_SECRET_KEY=your_stripe_secret_key
STRIPE_PUBLIC_KEY=your_stripe_public_key
GOOGLE_MAPS_API_KEY=your_maps_api_key
GOOGLE_VISION_API_KEY=your_vision_api_key
MAILER_DSN=smtps://your_email%40gmail.com:app_password@smtp.gmail.com:465
GOOGLE_RECAPTCHA_SITE_KEY=your_recaptcha_site_key
GOOGLE_RECAPTCHA_SECRET=your_recaptcha_secret
```

---

## Project Structure

```
src/
├── Controller/        # Route handlers (Auth, Programme, Seance, Therapeute, Avis, Abonnement, API...)
├── Entity/            # Doctrine entities (Utilisateur, Programme, Seance, Therapeute, Avis, Abonnement...)
├── Repository/        # Custom Doctrine queries
├── Service/           # Business logic (GeminiService, GoogleVisionService, StripeService...)
├── ValueObject/       # Email value object (Doctrine Embeddable)
└── Command/           # Symfony console commands
templates/
├── admin/             # Admin panel templates
├── client/            # Client-facing templates
└── components/        # Shared components (chatbot...)
```

---

## Topics & Keywords

`symfony` `php` `mental-health` `wellness` `web-application` `gemini-ai` `stripe-payments` `doctrine-orm` `twig` `mysql` `chatbot` `therapist-management` `session-booking` `subscription` `pdf-generation` `google-vision` `recaptcha` `jwt` `rest-api` `bootstrap`

---

## Authors

- **MindGrow Team** — PIDEV 3A Project
- Developed as part of the academic year 2025–2026
