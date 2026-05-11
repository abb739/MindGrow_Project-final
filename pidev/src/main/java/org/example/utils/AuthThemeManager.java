package org.example.utils;

import javafx.scene.Parent;

public class AuthThemeManager {

    private static boolean darkMode = false;

    public static void applyDefaultTheme(Parent root) {
        setDarkMode(darkMode, root);
    }

    public static void setDarkMode(boolean enabled, Parent root) {
        darkMode = enabled;
        if (root == null) return;
        if (!root.getStyleClass().contains("root")) {
            root.getStyleClass().add("root");
        }
        root.getStyleClass().removeAll("theme-light", "theme-dark", "dark-theme");
        if (darkMode) {
            root.getStyleClass().addAll("theme-dark", "dark-theme");
        } else {
            root.getStyleClass().add("theme-light");
        }
    }

    public static void toggleTheme(Parent root) {
        setDarkMode(!darkMode, root);
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static String getToggleLabel() {
        return darkMode ? "☀️ Mode Clair" : "🌙 Mode Sombre";
    }
}
