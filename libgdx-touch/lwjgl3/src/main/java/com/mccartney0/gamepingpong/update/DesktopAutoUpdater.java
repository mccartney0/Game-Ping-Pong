package com.mccartney0.gamepingpong.update;

import com.mccartney0.release.GitHubReleaseUpdater;
import com.mccartney0.release.ReleaseVersion;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DesktopAutoUpdater {
    private static final String REPOSITORY = "mccartney0/Game-Ping-Pong";
    private static final String ASSET_NAME = "game-ping-pong-touch-desktop.zip";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "release-update-check");
        thread.setDaemon(true);
        return thread;
    });

    private DesktopAutoUpdater() { }

    public static void check() {
        if (GraphicsEnvironment.isHeadless()) return;
        EXECUTOR.execute(() -> {
            try {
                String version = ReleaseVersion.current("1.0.0");
                GitHubReleaseUpdater.UpdateInfo update = GitHubReleaseUpdater.checkLatest(
                        REPOSITORY, version, ASSET_NAME);
                if (update == null) return;
                javax.swing.SwingUtilities.invokeLater(() -> {
                    int choice = JOptionPane.showConfirmDialog(
                            null,
                            "A versão " + update.version + " de Game Ping Pong Touch está disponível.\nBaixar agora?",
                            "Atualização disponível",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) download(update);
                });
            } catch (Exception ignored) {
                // A falha de rede nunca impede o lançamento do jogo.
            }
        });
    }

    private static void download(GitHubReleaseUpdater.UpdateInfo update) {
        EXECUTOR.execute(() -> {
            try {
                byte[] content = GitHubReleaseUpdater.download(update.downloadUrl);
                String expectedSha = update.checksumUrl == null
                        ? null : GitHubReleaseUpdater.downloadText(update.checksumUrl).split("\\s+")[0];
                if (!GitHubReleaseUpdater.sha256Matches(content, expectedSha)) {
                    throw new IllegalStateException("Checksum SHA-256 inválido");
                }
                File directory = new File(System.getProperty("user.home"), ".game-ping-pong/updates");
                if (!directory.exists() && !directory.mkdirs()) throw new IllegalStateException("Falha no cache");
                File target = new File(directory, ASSET_NAME);
                try (FileOutputStream output = new FileOutputStream(target)) {
                    output.write(content);
                }
                File checksum = new File(directory, ASSET_NAME + ".sha256");
                Files.write(checksum.toPath(), (expectedSha == null ? "" : expectedSha).getBytes(StandardCharsets.UTF_8));
                javax.swing.SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Download concluído em:\n" + target.getAbsolutePath() +
                                    "\nExtraia o pacote depois de fechar o jogo para aplicar a atualização.",
                            "Atualização pronta", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(directory);
                    } catch (Exception ignored) { }
                });
            } catch (Exception error) {
                javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        null, "Não foi possível baixar a atualização.", "Atualizador", JOptionPane.WARNING_MESSAGE));
            }
        });
    }
}
