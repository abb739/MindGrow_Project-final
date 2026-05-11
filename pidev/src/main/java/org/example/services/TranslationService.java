package org.example.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TranslationService {

    public String translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.isBlank())
            return text;

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String langPair = URLEncoder.encode(sourceLang + "|" + targetLang, StandardCharsets.UTF_8);
            String url = String.format("https://api.mymemory.translated.net/get?q=%s&langpair=%s", encodedText,
                    langPair);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                return jsonResponse.getAsJsonObject("responseData").get("translatedText").getAsString();
            }
        } catch (Exception e) {
            System.err.println("Erreur de traduction : " + e.getMessage());
        }
        return "Erreur de traduction";
    }
}
