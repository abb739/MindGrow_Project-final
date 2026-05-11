# MindGrow — Symfony Functionalities Documentation

> **Framework:** Symfony 6/7 (PHP)  
> **Project:** MindGrow — Mental Wellness Platform  
> **Modules covered:** Séance · Abonnement · Thérapeute · Utilisateur · Programme

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Module: Séance (Sessions)](#module-séance)
3. [Module: Abonnement (Subscriptions)](#module-abonnement)
4. [Module: Thérapeute (Therapists)](#module-thérapeute)
5. [Module: Utilisateur (Users)](#module-utilisateur)
6. [Module: Programme (Wellness Programs)](#module-programme)
7. [Cross-Cutting Services](#cross-cutting-services)
8. [Security & Authentication](#security--authentication)

---

## Architecture Overview

The project follows a layered MVC architecture using Symfony conventions:

| Layer | Symfony Component | Role |
|---|---|---|
| Entity | `Doctrine ORM` | Data model & DB mapping |
| Repository | `ServiceEntityRepository` | Custom DQL queries |
| Controller | `AbstractController` | HTTP request/response handling |
| Service | Plain PHP classes | Business logic isolation |
| View | `Twig` | HTML rendering |

All entities are mapped to a MySQL database via Doctrine ORM annotations (`#[ORM\...]`). Validation is handled using Symfony Validator constraints (`#[Assert\...]`).

---

## Module: Séance

### Entity — `App\Entity\Seance`

Mapped to the `seance` table. Key fields and their validation rules:

| Field | Type | Validation |
|---|---|---|
| `titre` | `string(100)` | `@NotBlank`, length 3–100 chars |
| `description` | `text` | `@NotBlank`, min 5 chars |
| `lieu` | `string(150)` | `@NotBlank` |
| `dateDebut` | `datetime` | `@NotNull`, `@GreaterThan("today")` |
| `dateFin` | `datetime` | `@NotNull`, must be after `dateDebut` via `@Expression` |
| `capacite` | `int` | `@NotNull`, `@Positive` |
| `image` | `string(255)` | `@File`, max 2MB, JPEG/PNG only |

The `@Expression` constraint on `dateFin` uses a custom expression:
```php
#[Assert\Expression(
    "this.getDateFin() > this.getDateDebut()",
    message: "La date de fin doit être après la date de début"
)]
```

### Repository — `SeanceRepository`

Extends `ServiceEntityRepository`. Custom query methods:

- **`findBySearch(string $search)`** — Full-text DQL search on `titre` and `lieu` fields using `LIKE`.
- **`findSeancesAVenir()`** — Returns only future sessions ordered by `dateDebut ASC`.
- **`findAllOrderedByCapacite(string $order)`** — Sorts all sessions by capacity, ascending or descending.
- **`findSeancesAvecNbReservations()`** — Returns all sessions ordered by start date (reservations counted in controller).

### Controller — `SeanceController`

Routes and features:

| Route | Method | Description |
|---|---|---|
| `GET /seances` | `indexClient` | Lists sessions for clients, computes available places per session, checks if user already booked |
| `GET /admin/seances` | `adminIndex` | Admin view with tri (sort by date or capacity), search, and reservation stats |
| `POST /admin/seances/new` | `new` | Creates a session with Symfony Validator constraint checking + custom date validation |
| `POST /admin/seances/edit/{id}` | `edit` | Updates session with full revalidation and overlap-checking via `dateOccupee()` |
| `GET /admin/seances/delete/{id}` | `delete` | Removes a session and redirects with flash message |

**Session overlap detection** is implemented via a private `dateOccupee()` helper that iterates all existing sessions and checks three overlap scenarios:
```php
($dateDebut >= $s->getDateDebut() && $dateDebut < $s->getDateFin()) ||
($dateFin > $s->getDateDebut() && $dateFin <= $s->getDateFin()) ||
($dateDebut <= $s->getDateDebut() && $dateFin >= $s->getDateFin())
```

**Client-side features:**
- Available places computed per session: `capacite - count(active reservations)`
- Per-user booking status check: `dejaReserve[id]` map passed to Twig

### Business Service — `SeanceBusinessService`

Isolated business rules:

- **`isDurationValid(Seance)`** — Returns `true` if session duration is between 30 minutes (1800s) and 4 hours (14400s).
- **`isFutureDateValid(Seance)`** — Returns `true` if start date is at least 1 hour from now.

---

## Module: Abonnement

### Entity — `App\Entity\Abonnement`

Mapped to the `abonnement` table.

| Field | Type | Notes |
|---|---|---|
| `nom` | `string(100)` | Subscription plan name |
| `description` | `text` | Plan description |
| `prix` | `decimal(10,2)` | Price in TND |
| `dureeMois` | `int` | Duration in months (allowed: 1, 3, 6, 12, 24) |

A related `Achat` entity tracks user purchases with `idAbonnement`, `idUtilisateur`, `statut` (actif/annulé), and `dateAchat`.

### Repository — `AbonnementRepository`

Key custom queries:

- **`findAllOrderedByPrix()`** — Custom CASE ordering: 12 months → 6 months → 3 months → 24 months → others, then by price ASC.
- **`findByFilters(q, duree, tri)`** — Combined filter: text search on `nom`/`description`, exact duration filter, tri options (`prix_asc`, `prix_desc`, `duree_asc`, `duree_desc`, `populaire`).
- **`findByPriceRange(min, max)`** — Returns subscriptions within a price range.

### Controller — `AbonnementController`

Routes and features:

| Route | Description |
|---|---|
| `GET /abonnements` | Client listing with active purchase check, stats, currency conversion |
| `GET /abonnements/paiement/{id}` | Payment page with auto-computed expiry date, Stripe public key injected |
| `POST /abonnements/payer/{id}` | Stripe charge via `Stripe\Charge::create()`, saves `Achat`, sends confirmation email with QR code |
| `GET /abonnements/annuler/{id}` | Cancels active subscription (ownership check) |
| `GET /abonnements/recu/{id}` | Generates and streams a PDF receipt via `PdfService` |
| `GET /admin/abonnements` | Admin CRUD view with per-plan stats |
| `POST /admin/abonnements/new` | Full server-side validation: name uniqueness, price range, duration whitelist, description length |
| `POST /admin/abonnements/edit/{id}` | Same validation with duplicate name exclusion for current record |
| `DELETE /admin/abonnements/delete/{id}` | Removes plan with flash confirmation |

**Stripe Integration:** Uses `Stripe\Charge::create()` with token from Stripe.js. Charge amount is converted to cents. On success, the old active purchase is cancelled before creating the new one.

**Post-payment flow:**
1. Cancel previous active `Achat` → set `statut = 'annulé'`
2. Create new `Achat` with `statut = 'actif'`
3. Generate a QR code via `QrCodeService` with plan name, reference number, and expiration
4. Send HTML confirmation email via `EmailService` (Symfony Mailer)
5. Render success page with QR code embedded as base64 SVG

**PDF Receipt:** `PdfService::generateRecuAbonnement()` generates a downloadable receipt with purchase reference, dates, and pricing details.

### Business Service — `AbonnementBusinessService`

- **`isPremium(Abonnement)`** — Returns `true` if price exceeds 100 TND.
- **`isLongTerm(Abonnement)`** — Returns `true` if duration exceeds 6 months.

---

## Module: Thérapeute

### Entity — `App\Entity\Therapeute`

Mapped to the `therapeute` table.

| Field | Type | Notes |
|---|---|---|
| `nom`, `prenom` | `string(100)` | Full name |
| `specialite` | `string(100)` | Therapy specialty |
| `email`, `telephone` | `string` | Contact info |
| `image` | `string(255)` | Profile photo path |
| `certificat` | `string(255)` | Certificate file path |
| `statutCertificat` | `string(20)` | `en_attente`, `verifie`, or `refuse` |
| `certificatTexte` | `text` | OCR-extracted certificate text |
| `dateInscription` | `datetime` | Registration timestamp |

### Controller — `TherapeuteController`

| Route | Description |
|---|---|
| `GET /therapeutes` | Client view with search by name and specialty filter; shows average rating and first 5 reviews per therapist |
| `GET /admin/therapeutes` | Admin view with all therapists and average ratings |
| `POST /admin/therapeutes/new` | Creates therapist with full validation; handles profile image upload; processes certificate via **Google Vision OCR** (`GoogleVisionService`) |
| `POST /admin/therapeutes/edit/{id}` | Updates therapist info and optionally replaces image or certificate; old image file is deleted from disk |
| `GET /admin/therapeutes/{id}/certifier/{statut}` | Admin-only: changes `statutCertificat` to `verifie` or `refuse` |
| `DELETE /admin/therapeutes/delete/{id}` | Removes therapist and deletes associated image file from disk |

**Image Upload Flow:**
- Validates MIME type: JPEG, PNG, WEBP, GIF
- Max file size: 3 MB
- Saved to `/public/uploads/therapeutes/` with a `uniqid()` prefix
- Old file deleted with `@unlink()` on update

**Certificate Processing with Google Vision:**
1. File uploaded to `/public/uploads/certificats/`
2. `GoogleVisionService::extractTextFromFile()` sends the file to Google Vision API
3. `isCertificatValid(text)` scans extracted text for medical/therapy keywords
4. Sets `statutCertificat` to `en_attente` (valid) or `refuse` (invalid)
5. Admin then manually confirms or refuses via the certifier route

**Review Integration:** `AvisRepository::getNoteMoyenne(therapeuteId)` and `findByTherapeute(therapeuteId)` are called to populate ratings and feedback in both client and admin views.

### Business Service — `TherapeuteBusinessService`

- **`isApproved(Therapeute)`** — Returns `true` only if `statutCertificat === 'Approuvé'`.
- **`hasCompleteContactInfo(Therapeute)`** — Returns `true` if both email and telephone are non-empty.

---

## Module: Utilisateur

### Entity — `App\Entity\Utilisateur`

Mapped to the `utilisateur` table. Key security fields:

| Field | Notes |
|---|---|
| `email` | Unique constraint |
| `motDePasse` | Stored as bcrypt hash |
| `role` | `client` or `admin` |
| `isVerified` | Boolean, set to `false` on registration |
| `verificationToken` | Random 32-byte hex token, cleared after verification |
| `resetToken` | Password reset token (sensitive) |
| `resetTokenExpiresAt` | Token TTL: 1 hour |
| `themePreference` | `light`, `dark`, or `auto` (default) |

### Controller — `AuthController`

Full authentication lifecycle:

| Route | Feature |
|---|---|
| `POST /login` | Email/password login with bcrypt verification; supports both hashed and plain-text legacy passwords |
| `GET /auth/google` | Initiates Google OAuth 2.0 flow with CSRF state parameter |
| `GET /auth/google/callback` | Handles OAuth callback: fetches profile, auto-creates user if new, sets session |
| `POST /register` | Registration with full validation + Google reCAPTCHA v2 verification |
| `POST /forgot-password` | Sends password reset email with 1-hour expiring token |
| `POST /reset-password/{token}` | Validates token expiry, updates bcrypt password, clears token |
| `GET /verify-email/{token}` | Activates account, clears verification token |
| `GET /logout` | Clears entire session |

**Session-based auth:** Uses `SessionInterface` to store `user_id`, `user_nom`, and `user_role`. All protected routes check session in the controller.

**Google reCAPTCHA:** Private `verifyRecaptcha()` method sends the token to `https://www.google.com/recaptcha/api/siteverify` via `HttpClientInterface`.

**Registration QR Code:** On successful registration, user credentials are embedded in a QR code (via `QrCodeService`) and displayed on the confirmation page.

**Email Verification Flow:**
1. Account created with `isVerified = false` and a random token
2. Verification link emailed via Symfony Mailer
3. `GET /verify-email/{token}` sets `isVerified = true` and clears the token
4. Login blocked if `isVerified === false`

### Business Service — `UtilisateurBusinessService`

- **`isAdmin(Utilisateur)`** — Returns `true` if role is `ROLE_ADMIN` or `ADMIN`.
- **`isPasswordSecure(Utilisateur)`** — Returns `true` if stored password is at least 8 characters.

---

## Module: Programme

### Entity — `App\Entity\Programme`

Mapped to the `programme` table.

| Field | Notes |
|---|---|
| `titre` | Program title |
| `description` | Rich text description |
| `idCategorie` | FK to `Categorie` entity (stored as int, not a relation) |
| `image` | Path to uploaded image |
| `video` | Path to uploaded video |

A separate `Categorie` entity stores `nom` and `description`.

### Repository — `ProgrammeRepository`

- **`findByFilters(search, catId)`** — Searches by title/description text and optionally filters by category ID.

### Controller — `ProgrammeController`

| Route | Feature |
|---|---|
| `GET /programmes` | Client listing with search and category filter |
| `GET /programmes/{id}` | Single program detail view with category name lookup |
| `GET /admin/programmes` | Admin listing with categories |
| `POST /admin/programmes/new` | Creates program with file uploads for image (JPG/PNG/GIF/WEBP) and video (MP4/WEBM/OGG/MOV); detects oversized POST before PHP parses the body |
| `POST /admin/programmes/edit/{id}` | Updates program; only replaces image/video if new file uploaded; deletes old files with `@unlink()` |
| `POST /admin/programmes/delete/{id}` | CSRF-protected deletion; cleans up image and video files from disk |
| `POST /admin/categories/new` | Creates category with letters-only name validation |
| `POST /admin/categories/delete/{id}` | CSRF-protected category deletion |

**File Upload Handling:**
- Detects `CONTENT_LENGTH > post_max_size` before PHP silently discards the upload
- Validates file extension against whitelist (`IMG_EXT`, `VID_EXT` class constants)
- Files saved to `uploads/programmes/images/` and `uploads/programmes/videos/` with `uniqid()` names

**CSRF Protection:** Delete actions use `isCsrfTokenValid('delete-prog-{id}', token)` to prevent cross-site request forgery.

**Title Validation Regex:**
```php
preg_match('/^[a-zA-Z0-9\s\-\'À-ÿ]+$/u', $titre)
```

### Business Service — `ProgrammeBusinessService`

- **`isDescriptionUseful(Programme)`** — Returns `true` if description is at least 20 characters.
- **`hasVisualMedia(Programme)`** — Returns `true` if either an image or a video is associated with the program.

---

## Cross-Cutting Services

### `EmailService` (Symfony Mailer)

Uses `Symfony\Component\Mailer\MailerInterface` and `Symfony\Component\Mime\Email`.

- **`sendConfirmationAbonnement(...)`** — Sends a styled HTML confirmation email with purchase details, QR code, and receipt number. Logs errors to `var/emails/last_error.txt`.
- **`sendReservationSeanceEmail(...)`** — Sends a session booking confirmation email.
- Email errors are non-blocking; the user flow continues even if email delivery fails.

### `QrCodeService` (endroid/qr-code)

- **`generateBase64(data, size)`** — Generates a base64-encoded SVG QR code using `SvgWriter` with custom brand colors (`#0b7a8f`).
- **`generateSvg(data, size)`** — Returns raw SVG string.
- **`buildAbonnementQrData(numero, plan, dateExp)`** — Formats structured text for subscription QR codes.

### `CurrencyService`

- Provides a `getOptionsList()` method used to populate currency selectors in subscription views.

### `GoogleVisionService` (Google Vision API)

- **`extractTextFromFile(filePath)`** — Sends image to Google Vision API for OCR text extraction (billing required; returns empty string in free tier).
- **`isCertificatValid(text)`** — Scans extracted text for medical/therapy keywords to auto-classify certificates.

### `PdfService` (PDF generation)

- **`generateRecuAbonnement(data)`** — Generates a PDF receipt for subscription purchases, returned as binary content with `application/pdf` headers.

---

## Security & Authentication

| Feature | Implementation |
|---|---|
| Password hashing | `password_hash()` / `password_verify()` with `PASSWORD_BCRYPT` |
| Session management | Symfony `SessionInterface` |
| Google OAuth 2.0 | Manual flow via `HttpClientInterface` with CSRF state parameter |
| reCAPTCHA v2 | Server-side verification on login and registration |
| Email verification | Random token flow; account locked until verified |
| Password reset | Time-limited token (1 hour), stored as `sensitive` column |
| Admin guard | All admin routes check `$session->get('user_role') === 'admin'` |
| CSRF (programmes) | `isCsrfTokenValid()` on all delete actions |

---

*Generated from source analysis of the MindGrow Symfony project.*
