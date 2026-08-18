package com.mccartney0.release;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ReleaseVersion {
    private ReleaseVersion() { }

    public static String current(String fallback) {
        String property = System.getProperty("game.version");
        if (property != null && !property.trim().isEmpty()) return property.trim();
        try (InputStream input = ReleaseVersion.class.getResourceAsStream("/release-version.properties")) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);
                String version = properties.getProperty("version");
                if (version != null && !version.trim().isEmpty()) return version.trim();
            }
        } catch (IOException ignored) {
            // Use fallback for IDE and local classpath runs.
        }
        return fallback;
    }
}
