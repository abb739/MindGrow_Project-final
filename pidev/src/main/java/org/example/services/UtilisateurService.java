package org.example.services;

import org.example.entities.Utilisateur;
import org.example.utils.MyDataBase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    // Helper method to ensure we have a valid connection
    private Connection getValidConnection() {
        Connection conn = MyDataBase.getConnection();
        if (conn == null) {
            System.err.println("✗ FATAL: Database connection is null!");
            throw new RuntimeException("Database connection failed");
        }
        return conn;
    }

    // ==========================================
    // MÉTHODES CRUD CLASSIQUES
    // ==========================================

    public boolean ajouterUtilisateur(Utilisateur utilisateur) {
        String query = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, date_inscription, role, is_verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection connection = getValidConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, utilisateur.getNom());
                ps.setString(2, utilisateur.getPrenom());
                ps.setString(3, utilisateur.getEmail());

                String password = utilisateur.getMotDePasse();
                if (password == null) {
                    password = "";
                }
                if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                    password = BCrypt.hashpw(password, BCrypt.gensalt());
                }
                ps.setString(4, password);

                ps.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                ps.setString(6, utilisateur.getRole());
                ps.setBoolean(7, true);  // Set to true so Symfony won't block login

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✓ Utilisateur ajouté avec succès! (Email: " + utilisateur.getEmail() + ")");
                    return true;
                } else {
                    System.err.println("✗ Aucune ligne insérée");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ SQL ERROR lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("✗ EXCEPTION lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Utilisateur> afficherUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur";
        try {
            Connection connection = getValidConnection();
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(query)) {
                while (rs.next()) {
                    Utilisateur u = new Utilisateur(
                            rs.getInt("id_utilisateur"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe"),
                            rs.getTimestamp("date_inscription"),
                            rs.getString("role")
                    );
                    utilisateurs.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ SQL ERROR lors de la récupération des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
        return utilisateurs;
    }

    public Utilisateur getUtilisateurById(int id) {
        String query = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        try {
            Connection connection = getValidConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new Utilisateur(
                            rs.getInt("id_utilisateur"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe"),
                            rs.getTimestamp("date_inscription"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ SQL ERROR lors de la récupération de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void modifierUtilisateur(Utilisateur utilisateur) {
        String query = "UPDATE utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, role=? WHERE id_utilisateur=?";
        try {
            Connection connection = getValidConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, utilisateur.getNom());
                ps.setString(2, utilisateur.getPrenom());
                ps.setString(3, utilisateur.getEmail());
                
                // Pour la modification, on vérifie d'abord si le mot de passe a déjà été hashé (il commence par $2a$)
                // Sinon on le hash. (Pratique si l'utilisateur met à jour son mdp depuis le profil)
                String pw = utilisateur.getMotDePasse();
                if (pw == null) {
                    pw = "";
                }
                if (!pw.startsWith("$2a$") && !pw.startsWith("$2b$") && !pw.startsWith("$2y$")) {
                    pw = BCrypt.hashpw(pw, BCrypt.gensalt());
                }
                ps.setString(4, pw);
                
                ps.setString(5, utilisateur.getRole());
                ps.setInt(6, utilisateur.getIdUtilisateur());
                
                ps.executeUpdate();
                System.out.println("✓ Utilisateur modifié avec succès!");
            }
        } catch (SQLException e) {
            System.err.println("✗ SQL ERROR lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean modifierMotDePasseParEmail(String email, String nouveauMdpClair) {
        String query = "UPDATE utilisateur SET mot_de_passe=? WHERE email=?";
        try {
            Connection connection = getValidConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                // Hachage du nouveau mot de passe
                String hash = BCrypt.hashpw(nouveauMdpClair, BCrypt.gensalt());
                ps.setString(1, hash);
                ps.setString(2, email);
                
                int rowsAffected = ps.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("✗ SQL ERROR lors de la modification du mot de passe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void supprimerUtilisateur(int id) {
        String query = "DELETE FROM utilisateur WHERE id_utilisateur=?";
        try {
            Connection connection = getValidConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                System.out.println("✓ Utilisateur supprimé avec succès!");
            }
        } catch (SQLException e) {
            System.err.println("✗ SQL ERROR lors de la suppression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // MÉTHODES SPÉCIFIQUES (SIGN UP / SIGN IN)
    // ==========================================

    /**
     * Méthode pour l'inscription (Sign Up)
     * Renvoie true si l'inscription a réussi, false si l'email existe déjà.
     */
    public boolean inscrire(Utilisateur utilisateur) {
        // 1. Vérifier si l'email existe déjà
        if (emailExiste(utilisateur.getEmail())) {
            System.err.println("Cet email est déjà utilisé !");
            return false;
        }

        // 2. Enforce that only 'client' role is allowed on registration (match Symfony API)
        String role = utilisateur.getRole();
        if (role == null || role.isEmpty() || !"client".equalsIgnoreCase(role)) {
            utilisateur.setRole("client");
            System.out.println("Role forced to 'client' for new registration");
        }
        
        return ajouterUtilisateur(utilisateur);
    }

    /**
     * Méthode pour vérifier si un email est déjà enregistré
     */
    public boolean emailExiste(String email) {
        String query = "SELECT count(*) FROM utilisateur WHERE email = ?";
        try {
            Connection connection = getValidConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("✓ Email check: '" + email + "' - Count: " + count);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ SQL ERROR lors de la vérification de l'email: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Méthode pour la connexion (Sign In)
     * Renvoie l'objet Utilisateur si les identifiants sont corrects, sinon null.
     */
    public Utilisateur authentifier(String email, String motDePasseClair) {
        if (email != null) {
            email = email.trim();
        }
        if (motDePasseClair != null) {
            motDePasseClair = motDePasseClair.trim();
        }
        String query = "SELECT * FROM utilisateur WHERE email = ?";
        try {
            Connection connection = getValidConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, email);
            
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String hashEnBase = rs.getString("mot_de_passe");
                    if (hashEnBase != null) {
                        hashEnBase = hashEnBase.trim();
                    }

                    String hashPrefix = hashEnBase != null && hashEnBase.length() >= 4 ? hashEnBase.substring(0, 4) : "null";
                    System.out.println("✓ Authentification tentative pour '" + email + "' ; hashPrefix=" + hashPrefix);

                    // On vérifie le mot de passe clair avec le hash de la BDD.
                    // Symfony/PHP peut générer des hashes bcrypt avec le préfixe $2y$.
                    boolean isValid = false;
                    try {
                        String normalizedHash = normalizeBcryptHash(hashEnBase);
                        isValid = BCrypt.checkpw(motDePasseClair, normalizedHash);
                        if (!isValid && !normalizedHash.equals(hashEnBase)) {
                            isValid = BCrypt.checkpw(motDePasseClair, hashEnBase);
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("✗ BCrypt vérification invalide pour '" + email + "' : " + e.getMessage());
                        // Si le hash n'est pas un hash BCrypt valide (anciens mots de passe en clair),
                        // on fait une comparaison basique en attendant qu'il soit mis à jour.
                        isValid = hashEnBase != null && hashEnBase.equals(motDePasseClair);
                    }

                    System.out.println("✓ Authentification resultat pour '" + email + "' : " + isValid);
                    
                    if (isValid) {
                        return new Utilisateur(
                                rs.getInt("id_utilisateur"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("email"),
                                hashEnBase,
                                rs.getTimestamp("date_inscription"),
                                rs.getString("role")
                        );
                    }
                } else {
                    System.out.println("✗ Authentification echec : email introuvable='" + email + "'");
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ SQL ERROR lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Échec de l'authentification (email inexistant ou mot de passe incorrect)
    }

    /**
     * Normalize PHP bcrypt hashes so they are compatible with the Java jBCrypt verifier.
     */
    private String normalizeBcryptHash(String hash) {
        if (hash != null && hash.startsWith("$2y$")) {
            return "$2a$" + hash.substring(4);
        }
        return hash;
    }
}
