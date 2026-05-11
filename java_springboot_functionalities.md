# MindGrow — Java Spring Boot Functionalities Documentation

> **Framework:** Spring Boot 3.x (Java 17+)  
> **Project:** MindGrow — Mental Wellness Platform  
> **Modules covered:** Séance · Abonnement · Thérapeute · Utilisateur · Programme

> This document describes how all functionalities identified in the Symfony project would be implemented in a Java Spring Boot equivalent.

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

The Spring Boot equivalent follows a standard layered architecture:

| Layer | Spring Component | Role |
|---|---|---|
| Entity | `@Entity` (JPA/Hibernate) | Data model & DB mapping |
| Repository | `JpaRepository` | JPQL/query methods |
| Service | `@Service` | Business logic isolation |
| Controller | `@RestController` / `@Controller` | HTTP handling |
| View | Thymeleaf or REST JSON responses | Rendering |

All entities are mapped using JPA annotations (`@Entity`, `@Column`, `@Id`, `@GeneratedValue`). Validation uses Jakarta Bean Validation (`@NotBlank`, `@Size`, `@Positive`, etc.).

**Key dependencies:**
- `spring-boot-starter-data-jpa` — Hibernate ORM
- `spring-boot-starter-validation` — Bean Validation 3.0
- `spring-boot-starter-security` — Spring Security
- `spring-boot-starter-mail` — JavaMailSender
- `spring-boot-starter-web` — MVC and REST
- `spring-boot-starter-thymeleaf` — Template engine
- `com.stripe:stripe-java` — Stripe SDK
- `com.google.zxing` — QR code generation

---

## Module: Séance

### Entity — `Seance.java`

```java
@Entity
@Table(name = "seance")
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seance")
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit contenir 3 à 100 caractères")
    @Column(name = "titre", length = 100)
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 5, message = "La description doit contenir au moins 5 caractères")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Le lieu est obligatoire")
    @Column(name = "lieu", length = 150)
    private String lieu;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @NotNull
    @Positive(message = "La capacité doit être supérieure à 0")
    @Column(name = "capacite")
    private Integer capacite;

    @Column(name = "image", length = 255)
    private String image;

    // Getters & setters omitted for brevity
}
```

The `dateFin > dateDebut` cross-field rule is enforced in the `@Service` layer or via a custom `@Constraint` class-level annotation:

```java
@SeanceDateValid  // custom class-level constraint
public class Seance { ... }
```

### Repository — `SeanceRepository.java`

Extends `JpaRepository<Seance, Long>`:

```java
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    // Search by title or location
    @Query("SELECT s FROM Seance s WHERE s.titre LIKE %:kw% OR s.lieu LIKE %:kw%")
    List<Seance> findBySearch(@Param("kw") String keyword);

    // Future sessions only
    @Query("SELECT s FROM Seance s WHERE s.dateDebut > :now ORDER BY s.dateDebut ASC")
    List<Seance> findSeancesAVenir(@Param("now") LocalDateTime now);

    // Sort by capacity
    List<Seance> findAllByOrderByCapaciteDesc();
    List<Seance> findAllByOrderByCapaciteAsc();
}
```

### Service — `SeanceService.java`

```java
@Service
public class SeanceService {

    public boolean isDurationValid(Seance seance) {
        long seconds = Duration.between(seance.getDateDebut(), seance.getDateFin()).toSeconds();
        return seconds >= 1800 && seconds <= 14400; // 30 min to 4 hours
    }

    public boolean isFutureDateValid(Seance seance) {
        return seance.getDateDebut().isAfter(LocalDateTime.now().plusHours(1));
    }

    public boolean isDateOccupee(List<Seance> all, LocalDateTime start, LocalDateTime end, Long ignoreId) {
        return all.stream()
            .filter(s -> !s.getId().equals(ignoreId))
            .anyMatch(s ->
                (start.isBefore(s.getDateFin()) && end.isAfter(s.getDateDebut()))
            );
    }
}
```

### Controller — `SeanceController.java`

```java
@Controller
@RequestMapping
public class SeanceController {

    @GetMapping("/seances")
    public String indexClient(@RequestParam(defaultValue = "") String search,
                              HttpSession session, Model model) { ... }

    @GetMapping("/admin/seances")
    public String adminIndex(...) { ... }

    @PostMapping("/admin/seances/new")
    public String create(@Valid @ModelAttribute Seance seance,
                         BindingResult result, ...) { ... }

    @PostMapping("/admin/seances/edit/{id}")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute Seance seance,
                       BindingResult result, ...) { ... }

    @GetMapping("/admin/seances/delete/{id}")
    public String delete(@PathVariable Long id, ...) { ... }
}
```

`@Valid` triggers Bean Validation automatically; errors are captured in `BindingResult`. Overlap detection is delegated to `SeanceService.isDateOccupee()`.

---

## Module: Abonnement

### Entity — `Abonnement.java`

```java
@Entity
@Table(name = "abonnement")
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abonnement")
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(name = "nom", length = 100, unique = true)
    private String nom;

    @NotBlank
    @Size(min = 5, max = 255)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("9999.99")
    @Column(name = "prix", precision = 10, scale = 2)
    private BigDecimal prix;

    @NotNull
    @Column(name = "duree_mois")
    private Integer dureeMois; // Allowed: 1, 3, 6, 12, 24
}
```

A separate `Achat` entity:

```java
@Entity
@Table(name = "achat")
public class Achat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idAbonnement;
    private Long idUtilisateur;
    private String statut; // "actif" | "annulé"
    private LocalDateTime dateAchat;
}
```

### Repository — `AbonnementRepository.java`

```java
public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    // Smart ordering: 12m first, then 6m, 3m, 24m, rest — then by price
    @Query("""
        SELECT a FROM Abonnement a ORDER BY
            CASE a.dureeMois
                WHEN 12 THEN 1 WHEN 6 THEN 2
                WHEN 3  THEN 3 WHEN 24 THEN 4
                ELSE 5
            END ASC, a.prix ASC
        """)
    List<Abonnement> findAllOrderedByPrix();

    // Combined text + duration + sort filter
    @Query("""
        SELECT a FROM Abonnement a
        WHERE (:q = '' OR a.nom LIKE %:q% OR a.description LIKE %:q%)
          AND (:duree = 0 OR a.dureeMois = :duree)
        """)
    List<Abonnement> findByFilters(@Param("q") String q, @Param("duree") int duree,
                                   Sort sort);

    Optional<Abonnement> findByNom(String nom);

    @Query("SELECT a FROM Abonnement a WHERE a.prix BETWEEN :min AND :max ORDER BY a.prix ASC")
    List<Abonnement> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);
}
```

### Service — `AbonnementService.java`

```java
@Service
public class AbonnementService {

    public boolean isPremium(Abonnement a) {
        return a.getPrix().compareTo(new BigDecimal("100")) > 0;
    }

    public boolean isLongTerm(Abonnement a) {
        return a.getDureeMois() != null && a.getDureeMois() > 6;
    }

    public boolean isDureeAllowed(int duree) {
        return List.of(1, 3, 6, 12, 24).contains(duree);
    }
}
```

### Controller — `AbonnementController.java`

Key payment flow endpoint:

```java
@PostMapping("/abonnements/payer/{id}")
public String payer(@PathVariable Long id,
                    @RequestParam String stripeToken,
                    @RequestParam String cardHolder,
                    HttpSession session, Model model) {

    Abonnement abo = abonnementRepo.findById(id).orElseThrow();

    // Stripe charge
    Stripe.apiKey = stripeSecretKey;
    ChargeCreateParams params = ChargeCreateParams.builder()
        .setAmount((long)(abo.getPrix().doubleValue() * 100))
        .setCurrency("eur")
        .setSource(stripeToken)
        .setDescription("MindGrow — " + abo.getNom())
        .build();
    Charge charge = Charge.create(params);

    // Cancel old purchase, create new one
    achatRepo.findActivByUser(userId).ifPresent(a -> { a.setStatut("annulé"); achatRepo.save(a); });
    Achat achat = new Achat(abo.getId(), userId, "actif", LocalDateTime.now());
    achatRepo.save(achat);

    // Generate QR + send email
    String qrBase64 = qrCodeService.generateBase64(buildQrData(...), 180);
    emailService.sendConfirmationAbonnement(..., qrBase64);

    return "client/abonnement_succes";
}
```

Other routes mirror the Symfony controller: listing, payment page, cancellation, PDF receipt, admin CRUD.

---

## Module: Thérapeute

### Entity — `Therapeute.java`

```java
@Entity
@Table(name = "therapeute")
public class Therapeute {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_therapeute")
    private Long id;

    @NotBlank @Size(min = 2, max = 100)
    private String nom, prenom;

    @NotBlank
    private String specialite;

    @Email
    private String email;

    @Pattern(regexp = "\\d{8,15}", message = "Téléphone invalide")
    private String telephone;

    private String image;
    private String certificat;

    @Column(name = "statut_certificat", length = 20)
    private String statutCertificat; // "en_attente" | "verifie" | "refuse"

    @Column(name = "certificat_texte", columnDefinition = "TEXT")
    private String certificatTexte;

    private LocalDateTime dateInscription;
}
```

### Repository — `TherapeuteRepository.java`

```java
public interface TherapeuteRepository extends JpaRepository<Therapeute, Long> {

    @Query("""
        SELECT t FROM Therapeute t
        WHERE (:search = '' OR LOWER(t.nom) LIKE %:search% OR LOWER(t.prenom) LIKE %:search%)
          AND (:specialite = '' OR t.specialite = :specialite)
        """)
    List<Therapeute> findBySearch(@Param("search") String search,
                                  @Param("specialite") String specialite);

    @Query("SELECT DISTINCT t.specialite FROM Therapeute t WHERE t.specialite IS NOT NULL")
    List<String> findDistinctSpecialites();

    Optional<Therapeute> findByEmail(String email);
}
```

### Service — `TherapeuteService.java`

```java
@Service
public class TherapeuteService {

    public boolean isApproved(Therapeute t) {
        return "Approuvé".equals(t.getStatutCertificat());
    }

    public boolean hasCompleteContactInfo(Therapeute t) {
        return StringUtils.hasText(t.getEmail()) && StringUtils.hasText(t.getTelephone());
    }
}
```

### Controller — `TherapeuteController.java`

Image upload with Spring `MultipartFile`:

```java
@PostMapping("/admin/therapeutes/new")
public String create(@Valid @ModelAttribute Therapeute therapeute,
                     BindingResult result,
                     @RequestParam("image") MultipartFile imageFile,
                     @RequestParam("certificat_file") MultipartFile certFile, ...) {

    if (!imageFile.isEmpty()) {
        String filename = UUID.randomUUID() + "." + getExtension(imageFile);
        Path dest = uploadDir.resolve("therapeutes").resolve(filename);
        Files.copy(imageFile.getInputStream(), dest);
        therapeute.setImage("/uploads/therapeutes/" + filename);
    }

    if (!certFile.isEmpty()) {
        // Save certificate then call Google Vision
        String certText = googleVisionService.extractTextFromFile(certPath);
        therapeute.setCertificatTexte(certText);
        therapeute.setStatutCertificat(googleVisionService.isCertificatValid(certText)
            ? "en_attente" : "refuse");
    }

    therapeuteRepo.save(therapeute);
    return "redirect:/admin/therapeutes";
}
```

Certificate approval:

```java
@GetMapping("/admin/therapeutes/{id}/certifier/{statut}")
public String certifier(@PathVariable Long id, @PathVariable String statut) {
    Therapeute t = therapeuteRepo.findById(id).orElseThrow();
    if (List.of("verifie", "refuse").contains(statut)) {
        t.setStatutCertificat(statut);
        therapeuteRepo.save(t);
    }
    return "redirect:/admin/therapeutes";
}
```

---

## Module: Utilisateur

### Entity — `Utilisateur.java`

```java
@Entity
@Table(name = "utilisateur")
public class Utilisateur implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long id;

    @Column(unique = true, length = 150)
    @Email @NotBlank
    private String email;

    @Column(name = "mot_de_passe", length = 255)
    private String motDePasse;

    @Column(length = 20)
    private String role; // "client" | "admin"

    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @Column(name = "theme_preference", length = 10)
    private String themePreference = "auto";

    @Column(name = "date_inscription")
    private LocalDateTime dateInscription;
}
```

### Spring Security Configuration — `SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/login", "/register", "/forgot-password",
                                 "/reset-password/**", "/verify-email/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/client"))
            .oauth2Login(oauth -> oauth.defaultSuccessUrl("/client"))
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login"))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/abonnements/payer/**"))
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Controller — `AuthController.java`

Key authentication routes:

| Route | Description |
|---|---|
| `POST /login` | Spring Security handles; custom `UserDetailsService` loads user by email |
| `GET /auth/google` | Managed by Spring Security OAuth2 (`spring-security-oauth2-client`) |
| `POST /register` | `@Valid` user form, `BCryptPasswordEncoder`, sends verification email |
| `POST /forgot-password` | Generates reset token, sets 1-hour expiry, emails link |
| `POST /reset-password/{token}` | Validates token expiry, updates password with BCrypt |
| `GET /verify-email/{token}` | Sets `isVerified = true`, clears token |

**Google OAuth2 with Spring Security:**

```yaml
# application.yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_OAUTH_CLIENT_ID}
            client-secret: ${GOOGLE_OAUTH_CLIENT_SECRET}
            scope: openid, email, profile
```

Spring Security handles the full OAuth2 flow. A custom `OAuth2UserService` saves new Google users automatically.

**reCAPTCHA verification:**

```java
@Service
public class RecaptchaService {
    public boolean verify(String token) {
        RestTemplate rt = new RestTemplate();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", token);
        Map<?, ?> resp = rt.postForObject(VERIFY_URL, params, Map.class);
        return Boolean.TRUE.equals(resp.get("success"));
    }
}
```

### Service — `UtilisateurService.java`

```java
@Service
public class UtilisateurService {

    public boolean isAdmin(Utilisateur u) {
        return "ROLE_ADMIN".equals(u.getRole()) || "ADMIN".equals(u.getRole());
    }

    public boolean isPasswordSecure(Utilisateur u) {
        return u.getMotDePasse() != null && u.getMotDePasse().length() >= 8;
    }
}
```

---

## Module: Programme

### Entity — `Programme.java`

```java
@Entity
@Table(name = "programme")
public class Programme {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_programme")
    private Long id;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-'À-ÿ]+$",
             message = "Titre contient des caractères non autorisés")
    @Column(name = "titre", length = 100)
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "id_categorie")
    private Long idCategorie;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "video", length = 255)
    private String video;
}
```

A `Categorie` entity stores `nom` and `description`. Name validated with `@Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$")`.

### Repository — `ProgrammeRepository.java`

```java
public interface ProgrammeRepository extends JpaRepository<Programme, Long> {

    @Query("""
        SELECT p FROM Programme p
        WHERE (:search = '' OR p.titre LIKE %:search% OR p.description LIKE %:search%)
          AND (:catId IS NULL OR p.idCategorie = :catId)
        """)
    List<Programme> findByFilters(@Param("search") String search,
                                  @Param("catId") Long catId);
}
```

### Service — `ProgrammeService.java`

```java
@Service
public class ProgrammeService {

    public boolean isDescriptionUseful(Programme p) {
        return p.getDescription() != null && p.getDescription().length() >= 20;
    }

    public boolean hasVisualMedia(Programme p) {
        return StringUtils.hasText(p.getImage()) || StringUtils.hasText(p.getVideo());
    }
}
```

### Controller — `ProgrammeController.java`

File size detection for multipart uploads:

```java
@PostMapping("/admin/programmes/new")
public String create(@RequestParam String titre,
                     @RequestParam MultipartFile imageFile,
                     @RequestParam MultipartFile videoFile,
                     RedirectAttributes ra) {

    // Spring Boot auto-rejects uploads larger than spring.servlet.multipart.max-file-size
    // Configure in application.yml:
    // spring.servlet.multipart.max-file-size=50MB
    // spring.servlet.multipart.max-request-size=50MB

    // Handle MaxUploadSizeExceededException globally:
    // @ControllerAdvice + @ExceptionHandler(MaxUploadSizeExceededException.class)

    String imgPath = fileUploadService.saveFile(imageFile, "programmes/images", ALLOWED_IMG_EXT);
    String vidPath = fileUploadService.saveFile(videoFile, "programmes/videos", ALLOWED_VID_EXT);
    // ...
}
```

CSRF is automatically handled by Spring Security's default configuration — the `_csrf` token is embedded in Thymeleaf templates via `th:action`.

---

## Cross-Cutting Services

### `EmailService.java` (JavaMailSender)

```java
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendConfirmationAbonnement(String toEmail, String toName,
                                           String plan, BigDecimal prix,
                                           int dureeMois, String qrBase64) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("adminmindrow@gmail.com", "MindGrow");
        helper.setTo(new InternetAddress(toEmail, toName));
        helper.setSubject("✅ Confirmation abonnement MindGrow — " + plan);
        helper.setText(buildHtml(toName, plan, prix, dureeMois, qrBase64), true);
        mailSender.send(message);
    }

    public void sendReservationSeanceEmail(String toEmail, String nom,
                                           String titre, String date, String lieu) { ... }
}
```

Configuration in `application.yml`:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties.mail.smtp.starttls.enable: true
```

### `QrCodeService.java` (ZXing)

```java
@Service
public class QrCodeService {

    public String generateBase64(String data, int size) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public String buildAbonnementQrData(String numero, String plan, String dateExp) {
        return String.join("\n",
            "MINDGROW", "-------------------",
            "Reference : " + numero,
            "Plan      : " + plan,
            "Expiration: " + dateExp,
            "-------------------", "Abonnement Actif"
        );
    }
}
```

### `GoogleVisionService.java`

```java
@Service
public class GoogleVisionService {

    public String extractTextFromFile(String filePath) {
        // Google Vision API call via google-cloud-vision client library
        // Requires billing; returns "" in free tier
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));
            Image img = Image.newBuilder().setContent(imgBytes).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Type.TEXT_DETECTION))
                .setImage(img).build();
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(List.of(request));
            return response.getResponses(0).getFullTextAnnotation().getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isCertificatValid(String text) {
        List<String> keywords = List.of(
            "certificat", "diplôme", "attestation", "université",
            "psycholog", "thérapie", "médecin", "master", "doctorat"
        );
        String lower = text.toLowerCase();
        return keywords.stream().anyMatch(lower::contains);
    }
}
```

### `PdfService.java` (iText / OpenPDF)

```java
@Service
public class PdfService {

    public byte[] generateRecuAbonnement(Map<String, String> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("MindGrow — Reçu Abonnement"));
            document.add(new Paragraph("Référence : " + data.get("numero")));
            document.add(new Paragraph("Plan : " + data.get("abonnement")));
            document.add(new Paragraph("Prix : " + data.get("prix") + " TND"));
            document.add(new Paragraph("Expiration : " + data.get("dateExpiration")));
            document.close();
            return baos.toByteArray();
        }
    }
}
```

Controller response:
```java
@GetMapping("/abonnements/recu/{id}")
public ResponseEntity<byte[]> recu(@PathVariable Long id, HttpSession session) {
    byte[] pdf = pdfService.generateRecuAbonnement(data);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=recu_" + id + ".pdf")
        .body(pdf);
}
```

---

## Security & Authentication

| Feature | Spring Boot Implementation |
|---|---|
| Password hashing | `BCryptPasswordEncoder` bean; `passwordEncoder.encode()` / `.matches()` |
| Session management | Spring Security `SecurityContext`, server-side `HttpSession` |
| Google OAuth 2.0 | `spring-security-oauth2-client` autoconfiguration; custom `OAuth2UserService` for user creation |
| reCAPTCHA v2 | `RecaptchaService` using `RestTemplate` to call Google's verify API |
| Email verification | Token stored in DB; `@GetMapping("/verify-email/{token}")` activates account |
| Password reset | Time-limited token (`LocalDateTime` expiry), cleared after use |
| Admin access control | `@PreAuthorize("hasRole('ADMIN')")` or `SecurityFilterChain` URL patterns |
| CSRF protection | Spring Security enabled by default; tokens in Thymeleaf via `th:action` |
| File upload limits | `spring.servlet.multipart.max-file-size`; `@ExceptionHandler(MaxUploadSizeExceededException)` |
| Input validation | `@Valid` + `BindingResult` on all form models; global `@ControllerAdvice` for error handling |

### Exception Handler — `GlobalExceptionHandler.java`

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleFileTooLarge(RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Fichier trop volumineux !");
        return "redirect:/admin/programmes";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleNotFound(Model model) {
        model.addAttribute("error", "Ressource introuvable.");
        return "error/404";
    }
}
```

---

*Generated as a Java Spring Boot translation of the MindGrow Symfony project functionalities.*
