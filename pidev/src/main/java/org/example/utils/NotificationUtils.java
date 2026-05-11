package org.example.utils;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.File;

public class NotificationUtils {

    public static void showNotification(String title, String message) {
        System.out.println("Tentative d'affichage de la notification: " + title);
        if (!SystemTray.isSupported()) {
            System.err.println("SystemTray n'est pas supporté sur ce système.");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            Image image;
            File iconFile = new File("icon.png");
            if (iconFile.exists()) {
                image = Toolkit.getDefaultToolkit().createImage(iconFile.getAbsolutePath());
            } else {
                image = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            }

            TrayIcon trayIcon = new TrayIcon(image, "MindGrow");
            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);

            // Délai nécessaire sur certains systèmes Windows pour que l'icône soit prête
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    trayIcon.displayMessage(title, message, MessageType.INFO);
                    System.out.println("Message de notification envoyé.");

                    Thread.sleep(10000);
                    tray.remove(trayIcon);
                } catch (Exception e) {
                    System.err.println("Erreur dans le thread de notification: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Erreur SystemTray: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
