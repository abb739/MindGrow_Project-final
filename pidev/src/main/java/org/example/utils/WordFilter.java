package org.example.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class WordFilter {
    private static final Set<String> BAD_WORDS = new HashSet<>(Arrays.asList(
            // French
            "merde", "putain", "salope", "connard", "con", "enculé", "bite", "couille", "bordel", "pute",
            "salaud", "abruti", "débile", "imbécile", "idiot", "cretin", "connasse", "foutre",
            // English
            "fuck", "shit", "asshole", "bitch", "bastard", "dick", "pussy", "cunt", "faggot", "nigger",
            "retard", "slut", "whore", "damn", "hell", "piss"
    // Add more as needed
    ));

    public static boolean containsBadWords(String text) {
        if (text == null || text.isBlank())
            return false;

        // Basic normalization: lowercase and remove some punctuation
        String normalized = text.toLowerCase().replaceAll("[^a-zA-Z\\sàâäéèêëïîôöùûüç]", " ");
        String[] words = normalized.split("\\s+");

        for (String word : words) {
            if (BAD_WORDS.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public static String filterText(String text) {
        if (text == null || text.isBlank())
            return text;

        String result = text;
        for (String badWord : BAD_WORDS) {
            // Regex to match whole words case-insensitively
            String regex = "(?i)\\b" + badWord + "\\b";
            result = result.replaceAll(regex, "***");
        }
        return result;
    }
}
