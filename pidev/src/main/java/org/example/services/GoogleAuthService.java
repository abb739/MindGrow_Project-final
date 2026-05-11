package org.example.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class GoogleAuthService {

    private static final String CLIENT_ID = "144002859163-8tpadgaodt9j7v0oju7ptpfa3k38q43i.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    
    // On demande l'accès au profil basique et à l'email via les scopes Google standards
    private static final List<String> SCOPES = Arrays.asList(
            "openid",
            "email",
            "profile"
    );
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Lance le flux d'autorisation OAuth2 dans le navigateur par défaut de l'utilisateur.
     * @return L'adresse e-mail de l'utilisateur si succès, null sinon.
     */
    public String authenticateAndGetEmail() {
        try {
            NetHttpTransport httpTransport = new NetHttpTransport();

            // Création du flux à partir de nos secrets en dur
            String jsonSecrets = "{\"installed\":{\"client_id\":\"" + CLIENT_ID + "\",\"project_id\":\"test\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"" + CLIENT_SECRET + "\",\"redirect_uris\":[\"http://localhost\"]}}";
            
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new StringReader(jsonSecrets));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setAccessType("offline")
                    .build();

            // On lance un mini serveur local Jettty sur un port disponible aléatoire (-1) 
            // pour éviter l'erreur "BindException: Address already in use" si l'appli est relancée.
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(-1).build();
            
            // Cette ligne ouvre le navigateur !
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

            if (credential != null && credential.getAccessToken() != null) {
                // Succès : Récupération de l'e-mail via l'API UserInfo
                return getUserEmailInfo(credential.getAccessToken());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'authentification Google : " + e.getMessage());
        }
        return null;
    }

    /**
     * Fait une requête HTTP simple vers Google API pour récupérer l'e-mail avec l'Access Token
     */
    private String getUserEmailInfo(String accessToken) {
        try {
            java.net.URL url = new java.net.URL("https://www.googleapis.com/oauth2/v2/userinfo");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parsing Json simplifié pour extraire "email" : "..."
                String jsonResponse = response.toString();
                String emailKey = "\"email\":";
                int emailIndex = jsonResponse.indexOf(emailKey);
                if (emailIndex != -1) {
                    int startQuote = jsonResponse.indexOf("\"", emailIndex + emailKey.length());
                    int endQuote = jsonResponse.indexOf("\"", startQuote + 1);
                    return jsonResponse.substring(startQuote + 1, endQuote);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
