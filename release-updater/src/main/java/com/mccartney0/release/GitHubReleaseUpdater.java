package com.mccartney0.release;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small Java 8 compatible client for the public GitHub Releases API. */
public final class GitHubReleaseUpdater {
    private static final int CONNECT_TIMEOUT_MS = 7_000;
    private static final int READ_TIMEOUT_MS = 12_000;
    private static final Pattern TAG_PATTERN = Pattern.compile("\\\"tag_name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern ASSET_PATTERN = Pattern.compile(
            "\\\"name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\".*?\\\"browser_download_url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"",
            Pattern.DOTALL);

    private GitHubReleaseUpdater() { }

    public static UpdateInfo checkLatest(String repository, String currentVersion, String assetName)
            throws IOException {
        String apiUrl = "https://api.github.com/repos/" + repository + "/releases/latest";
        String json = get(apiUrl, "application/vnd.github+json");
        Matcher tagMatcher = TAG_PATTERN.matcher(json);
        if (!tagMatcher.find()) {
            throw new IOException("GitHub Release sem tag_name");
        }
        String tag = tagMatcher.group(1);
        String releaseVersion = normalizeVersion(tag);
        String current = normalizeVersion(currentVersion);
        if (compareVersions(releaseVersion, current) <= 0) {
            return null;
        }

        String downloadUrl = null;
        String checksumUrl = null;
        Matcher assetMatcher = ASSET_PATTERN.matcher(json);
        while (assetMatcher.find()) {
            String name = assetMatcher.group(1);
            if (assetName.equals(name)) {
                downloadUrl = assetMatcher.group(2);
            } else if ((assetName + ".sha256").equals(name)) {
                checksumUrl = assetMatcher.group(2);
            }
        }
        if (downloadUrl == null) {
            throw new IOException("Asset de atualização não encontrado: " + assetName);
        }
        return new UpdateInfo(releaseVersion, tag, assetName, downloadUrl, checksumUrl);
    }

    public static byte[] download(String downloadUrl) throws IOException {
        return getBytes(downloadUrl, "application/octet-stream");
    }

    public static String downloadText(String downloadUrl) throws IOException {
        return new String(getBytes(downloadUrl, "text/plain"), StandardCharsets.UTF_8).trim();
    }

    public static boolean sha256Matches(byte[] content, String expectedSha256) {
        if (expectedSha256 == null || expectedSha256.trim().isEmpty()) {
            return true;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuilder actual = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                actual.append(String.format(Locale.ROOT, "%02x", value));
            }
            return actual.toString().equalsIgnoreCase(expectedSha256.trim());
        } catch (Exception error) {
            return false;
        }
    }

    public static String normalizeVersion(String raw) {
        String value = raw == null ? "0.0.0" : raw.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("v")) value = value.substring(1);
        int dash = value.indexOf('-');
        if (dash >= 0) value = value.substring(0, dash);
        return value;
    }

    public static int compareVersions(String left, String right) {
        String[] a = normalizeVersion(left).split("\\.");
        String[] b = normalizeVersion(right).split("\\.");
        for (int index = 0; index < Math.max(a.length, b.length); index++) {
            int leftPart = index < a.length ? number(a[index]) : 0;
            int rightPart = index < b.length ? number(b[index]) : 0;
            if (leftPart != rightPart) return Integer.compare(leftPart, rightPart);
        }
        return 0;
    }

    private static int number(String value) {
        try {
            return Integer.parseInt(value.replaceAll("[^0-9].*", ""));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static String get(String url, String accept) throws IOException {
        return new String(getBytes(url, accept), StandardCharsets.UTF_8);
    }

    private static byte[] getBytes(String urlValue, String accept) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = URI.create(urlValue).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", accept);
            connection.setRequestProperty("X-GitHub-Api-Version", "2026-03-10");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("HTTP " + status + " em " + urlValue);
            }
            try (InputStream input = new BufferedInputStream(connection.getInputStream());
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[16 * 1024];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    output.write(buffer, 0, read);
                }
                return output.toByteArray();
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public static final class UpdateInfo {
        public final String version;
        public final String tagName;
        public final String assetName;
        public final String downloadUrl;
        public final String checksumUrl;

        public UpdateInfo(String version, String tagName, String assetName, String downloadUrl, String checksumUrl) {
            this.version = version;
            this.tagName = tagName;
            this.assetName = assetName;
            this.downloadUrl = downloadUrl;
            this.checksumUrl = checksumUrl;
        }
    }
}
