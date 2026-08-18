package com.mccartney0.release;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.concurrent.atomic.AtomicBoolean;

/** Small Java 8 compatible client for the public GitHub Releases API. */
public final class GitHubReleaseUpdater {
    private static final int CONNECT_TIMEOUT_MS = 7_000;
    private static final int READ_TIMEOUT_MS = 12_000;
    private static final String API_BASE_PROPERTY = "github.api.base";
    private static final Pattern TAG_PATTERN = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern DOWNLOAD_URL_PATTERN = Pattern.compile(
            "\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"");

    private GitHubReleaseUpdater() { }

    public static UpdateInfo checkLatest(String repository, String currentVersion, String assetName)
            throws IOException {
        String apiBase = System.getProperty(API_BASE_PROPERTY, "https://api.github.com")
                .replaceAll("/$", "");
        String apiUrl = apiBase + "/repos/" + repository + "/releases/latest";
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
        Matcher urlMatcher = DOWNLOAD_URL_PATTERN.matcher(json);
        while (urlMatcher.find()) {
            int windowStart = Math.max(0, urlMatcher.start() - 512);
            int windowEnd = Math.min(json.length(), urlMatcher.end() + 512);
            String window = json.substring(windowStart, windowEnd);
            Matcher nameMatcher = NAME_PATTERN.matcher(window);
            String name = null;
            int closestDistance = Integer.MAX_VALUE;
            while (nameMatcher.find()) {
                int absoluteNameStart = windowStart + nameMatcher.start();
                int distance = Math.abs(absoluteNameStart - urlMatcher.start());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    name = nameMatcher.group(1);
                }
            }
            if (name == null) continue;
            if (assetName.equals(name)) {
                downloadUrl = urlMatcher.group(1);
            } else if ((assetName + ".sha256").equals(name)) {
                checksumUrl = urlMatcher.group(1);
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

    /**
     * Faz download incremental para um arquivo .part e só publica o nome final após o sucesso.
     * O método é síncrono de propósito: o chamador controla a thread de trabalho e a UI.
     */
    public static void downloadToFile(
            String downloadUrl,
            File destination,
            AtomicBoolean cancelled,
            DownloadListener listener) {
        File partial = new File(destination.getPath() + ".part");
        HttpURLConnection connection = null;
        try {
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Não foi possível criar o diretório de download");
            }
            if (cancelled != null && cancelled.get()) {
                notifyCancelled(listener, partial);
                return;
            }

            URL url = URI.create(downloadUrl).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/octet-stream");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("HTTP " + status + " em " + downloadUrl);
            }

            long total = connection.getContentLengthLong();
            long downloaded = 0L;
            long startedAt = System.nanoTime();
            long lastNotificationAt = 0L;
            byte[] buffer = new byte[16 * 1024];

            boolean cancelledDuringRead = false;
            try (InputStream input = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream output = new FileOutputStream(partial, false)) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    if (cancelled != null && cancelled.get()) {
                        cancelledDuringRead = true;
                        break;
                    }
                    output.write(buffer, 0, read);
                    downloaded += read;
                    long now = System.nanoTime();
                    if (lastNotificationAt == 0L || now - lastNotificationAt >= 100_000_000L) {
                        notifyProgress(listener, downloaded, total, startedAt);
                        lastNotificationAt = now;
                    }
                }
                output.flush();
            }

            if (cancelledDuringRead || (cancelled != null && cancelled.get())) {
                notifyCancelled(listener, partial);
                return;
            }
            notifyProgress(listener, downloaded, total, startedAt);
            if (destination.exists() && !destination.delete()) {
                throw new IOException("Não foi possível substituir o arquivo anterior");
            }
            if (!partial.renameTo(destination)) {
                throw new IOException("Não foi possível finalizar o arquivo baixado");
            }
            if (listener != null) listener.onCompleted(destination);
        } catch (Exception error) {
            if (partial.exists()) partial.delete();
            if (cancelled != null && cancelled.get()) {
                if (listener != null) listener.onCancelled();
            } else if (listener != null) {
                listener.onError(error);
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public static boolean sha256Matches(File content, String expectedSha256) throws IOException {
        if (expectedSha256 == null || expectedSha256.trim().isEmpty()) return true;
        MessageDigest digest = newDigest();
        byte[] buffer = new byte[16 * 1024];
        try (FileInputStream input = new FileInputStream(content)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return sha256Hex(digest.digest()).equalsIgnoreCase(expectedSha256.trim());
    }

    public static boolean sha256Matches(byte[] content, String expectedSha256) {
        if (expectedSha256 == null || expectedSha256.trim().isEmpty()) {
            return true;
        }
        try {
            MessageDigest digest = newDigest();
            byte[] hash = digest.digest(content);
            return sha256Hex(hash).equalsIgnoreCase(expectedSha256.trim());
        } catch (Exception error) {
            return false;
        }
    }

    private static MessageDigest newDigest() throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception error) {
            throw new IOException("SHA-256 indisponível", error);
        }
    }

    private static String sha256Hex(byte[] hash) {
        StringBuilder actual = new StringBuilder(hash.length * 2);
        for (byte value : hash) {
            actual.append(String.format(Locale.ROOT, "%02x", value));
        }
        return actual.toString();
    }

    private static void notifyProgress(
            DownloadListener listener, long downloaded, long total, long startedAt) {
        if (listener == null) return;
        long elapsedSeconds = Math.max(1L, (System.nanoTime() - startedAt) / 1_000_000_000L);
        listener.onProgress(new DownloadProgress(downloaded, total, downloaded / elapsedSeconds));
    }

    private static void notifyCancelled(DownloadListener listener, File partial) {
        if (partial.exists()) partial.delete();
        if (listener != null) listener.onCancelled();
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
