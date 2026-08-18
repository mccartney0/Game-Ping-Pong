package com.mccartney0.release;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DownloadProgressTest {
    @Test
    public void calculatesPercentAndEta() {
        DownloadProgress progress = new DownloadProgress(512, 1024, 128);
        Assert.assertFalse(progress.isIndeterminate());
        Assert.assertEquals(50, progress.percent());
        Assert.assertEquals(4L, progress.remainingSeconds());

        DownloadProgress unknown = new DownloadProgress(512, -1, 128);
        Assert.assertTrue(unknown.isIndeterminate());
        Assert.assertEquals(-1L, unknown.remainingSeconds());
    }

    @Test
    public void streamsToFinalFileAndReportsProgress() throws Exception {
        byte[] payload = new byte[96 * 1024];
        for (int index = 0; index < payload.length; index++) payload[index] = (byte) (index % 251);
        TestServer server = new TestServer(payload, 0L);
        File directory = Files.createTempDirectory("release-updater-test").toFile();
        File destination = new File(directory, "asset.bin");
        AtomicReference<DownloadProgress> last = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        GitHubReleaseUpdater.downloadToFile(
                server.url(), destination, new AtomicBoolean(false), new DownloadListener() {
                    @Override
                    public void onProgress(DownloadProgress progress) {
                        last.set(progress);
                    }

                    @Override
                    public void onCompleted(File file) {
                        completed.set(true);
                    }

                    @Override
                    public void onCancelled() {
                        Assert.fail("download should not be cancelled");
                    }

                    @Override
                    public void onError(Exception error) {
                        Assert.fail(error.getMessage());
                    }
                });
        server.close();

        Assert.assertTrue(completed.get());
        Assert.assertTrue(last.get().downloadedBytes >= payload.length);
        Assert.assertEquals(payload.length, destination.length());
        Assert.assertArrayEquals(payload, read(destination));
        Assert.assertFalse(new File(destination.getPath() + ".part").exists());
        delete(directory);
    }

    @Test
    public void cancellationRemovesPartialFile() throws Exception {
        byte[] payload = new byte[256 * 1024];
        Arrays.fill(payload, (byte) 7);
        TestServer server = new TestServer(payload, 20L);
        File directory = Files.createTempDirectory("release-updater-cancel-test").toFile();
        File destination = new File(directory, "asset.bin");
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean callbackCalled = new AtomicBoolean(false);

        GitHubReleaseUpdater.downloadToFile(
                server.url(), destination, cancelled, new DownloadListener() {
                    @Override
                    public void onProgress(DownloadProgress progress) {
                        cancelled.set(true);
                    }

                    @Override
                    public void onCompleted(File file) {
                        Assert.fail("cancelled download must not complete");
                    }

                    @Override
                    public void onCancelled() {
                        callbackCalled.set(true);
                    }

                    @Override
                    public void onError(Exception error) {
                        Assert.fail(error.getMessage());
                    }
                });
        server.close();

        Assert.assertTrue(callbackCalled.get());
        Assert.assertFalse(destination.exists());
        Assert.assertFalse(new File(destination.getPath() + ".part").exists());
        delete(directory);
    }

    @Test
    public void validatesFileChecksumWithoutLoadingItAsBytes() throws Exception {
        File file = File.createTempFile("release-updater", ".bin");
        Files.write(file.toPath(), "streamed checksum".getBytes(StandardCharsets.UTF_8));
        Assert.assertTrue(GitHubReleaseUpdater.sha256Matches(
                file, "ab3db5fcf969b552ec278733651168f03ccc4fb2fb4a06a127a82b8f0316be5d"));
        file.delete();
    }

    private static byte[] read(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toByteArray();
        }
    }

    private static void delete(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) delete(child);
        file.delete();
    }

    private static final class TestServer implements AutoCloseable {
        private final ServerSocket socket;
        private final Thread thread;
        private final byte[] payload;
        private final long delayMs;

        private TestServer(byte[] payload, long delayMs) throws IOException {
            this.payload = payload;
            this.delayMs = delayMs;
            socket = new ServerSocket(0);
            thread = new Thread(this::serve, "release-updater-test-server");
            thread.setDaemon(true);
            thread.start();
        }

        private String url() {
            return "http://127.0.0.1:" + socket.getLocalPort() + "/asset.bin";
        }

        private void serve() {
            try (Socket client = socket.accept()) {
                while (client.getInputStream().read() != '\n') { }
                OutputStream output = client.getOutputStream();
                String headers = "HTTP/1.1 200 OK\r\nContent-Length: " + payload.length
                        + "\r\nContent-Type: application/octet-stream\r\n\r\n";
                output.write(headers.getBytes(StandardCharsets.US_ASCII));
                int offset = 0;
                while (offset < payload.length) {
                    int length = Math.min(8192, payload.length - offset);
                    output.write(payload, offset, length);
                    output.flush();
                    offset += length;
                    if (delayMs > 0L) Thread.sleep(delayMs);
                }
            } catch (Exception ignored) {
                // The client may close the socket after cancellation.
            }
        }

        @Override
        public void close() throws Exception {
            socket.close();
            thread.join(2000L);
        }
    }
}
