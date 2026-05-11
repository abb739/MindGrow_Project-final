package org.example.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadPathResolver {

    private static final Path CURRENT_ROOT = Paths.get("").toAbsolutePath().normalize();
    private static final Path SHARED_ROOT = CURRENT_ROOT.getParent() != null ? CURRENT_ROOT.getParent() : CURRENT_ROOT;
    private static final Path PUBLIC_ROOT = SHARED_ROOT.resolve("public").normalize();

    public static String resolve(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        File candidate = new File(imagePath);
        if (candidate.isAbsolute() && candidate.exists()) {
            return candidate.getAbsolutePath();
        }

        if (candidate.exists()) {
            return candidate.getAbsolutePath();
        }

        String normalized = imagePath.replace('\\', '/').replaceFirst("^/+", "");
        if (normalized.startsWith("public/")) {
            normalized = normalized.substring("public/".length());
        }

        if (normalized.startsWith("uploads/")) {
            Path sharedFile = PUBLIC_ROOT.resolve(normalized).normalize();
            if (sharedFile.toFile().exists()) {
                return sharedFile.toAbsolutePath().toString();
            }
        }

        Path sharedPath = PUBLIC_ROOT.resolve(normalized).normalize();
        if (sharedPath.toFile().exists()) {
            return sharedPath.toAbsolutePath().toString();
        }

        Path relativePath = CURRENT_ROOT.resolve(normalized).normalize();
        if (relativePath.toFile().exists()) {
            return relativePath.toAbsolutePath().toString();
        }

        return imagePath;
    }

    public static boolean exists(String imagePath) {
        String resolved = resolve(imagePath);
        return resolved != null && new File(resolved).exists();
    }
}
