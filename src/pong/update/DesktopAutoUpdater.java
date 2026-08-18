package pong.update;

import com.mccartney0.release.DownloadListener;
import com.mccartney0.release.DownloadProgress;
import com.mccartney0.release.GitHubReleaseUpdater;
import com.mccartney0.release.ReleaseVersion;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DesktopAutoUpdater {
    private static final String REPOSITORY = "mccartney0/Game-Ping-Pong";
    private static final String ASSET_NAME = "neon-ping-pong-awt.jar";
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
                String currentVersion = ReleaseVersion.current("1.0.0");
                GitHubReleaseUpdater.UpdateInfo update = GitHubReleaseUpdater.checkLatest(
                        REPOSITORY, currentVersion, ASSET_NAME);
                if (update == null) return;
                SwingUtilities.invokeLater(() -> {
                    int choice = JOptionPane.showConfirmDialog(
                            null,
                            "A versão " + update.version + " de Neon Ping Pong está disponível.\nBaixar agora?",
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
        SwingUtilities.invokeLater(() -> {
            DownloadDialog dialog = new DownloadDialog(null, "Atualizando Neon Ping Pong");
            dialog.showDialog();
            EXECUTOR.execute(() -> GitHubReleaseUpdater.downloadToFile(
                    update.downloadUrl,
                    new File(new File(System.getProperty("user.home"), ".neon-ping-pong/updates"), ASSET_NAME),
                    dialog.cancelled,
                    new DownloadListener() {
                        @Override
                        public void onProgress(DownloadProgress progress) {
                            SwingUtilities.invokeLater(() -> dialog.update(progress));
                        }

                        @Override
                        public void onCompleted(File file) {
                            try {
                                String expectedSha = update.checksumUrl == null
                                        ? null : GitHubReleaseUpdater.downloadText(update.checksumUrl).split("\\s+")[0];
                                if (!GitHubReleaseUpdater.sha256Matches(file, expectedSha)) {
                                    throw new IllegalStateException("Checksum SHA-256 inválido");
                                }
                                if (expectedSha != null) {
                                    Files.write(new File(file.getPath() + ".sha256").toPath(),
                                            expectedSha.getBytes(StandardCharsets.UTF_8));
                                }
                                SwingUtilities.invokeLater(() -> {
                                    dialog.close();
                                    JOptionPane.showMessageDialog(null,
                                            "Download concluído em:\n" + file.getAbsolutePath()
                                                    + "\nFeche o jogo e substitua o JAR atual para aplicar a atualização.",
                                            "Atualização pronta", JOptionPane.INFORMATION_MESSAGE);
                                    openFolder(file.getParentFile());
                                });
                            } catch (Exception error) {
                                if (file.exists()) file.delete();
                                SwingUtilities.invokeLater(() -> {
                                    dialog.close();
                                    JOptionPane.showMessageDialog(null,
                                            "A validação da atualização falhou.",
                                            "Atualizador", JOptionPane.WARNING_MESSAGE);
                                });
                            }
                        }

                        @Override
                        public void onCancelled() {
                            SwingUtilities.invokeLater(() -> {
                                dialog.close();
                                JOptionPane.showMessageDialog(null, "Download cancelado.",
                                        "Atualizador", JOptionPane.INFORMATION_MESSAGE);
                            });
                        }

                        @Override
                        public void onError(Exception error) {
                            SwingUtilities.invokeLater(() -> {
                                dialog.close();
                                JOptionPane.showMessageDialog(null,
                                        "Não foi possível baixar a atualização.",
                                        "Atualizador", JOptionPane.WARNING_MESSAGE);
                            });
                        }
                    }));
        });
    }

    private static void openFolder(File directory) {
        try {
            if (Desktop.isDesktopSupported() && directory != null) Desktop.getDesktop().open(directory);
        } catch (Exception ignored) { }
    }

    private static final class DownloadDialog {
        private final JDialog dialog;
        private final JProgressBar progressBar = new JProgressBar(0, 100);
        private final JLabel status = new JLabel("Conectando…");
        private final JLabel detail = new JLabel("Aguarde");
        private final JButton cancel = new JButton("Cancelar");
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        private DownloadDialog(Window owner, String title) {
            dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(true);
            progressBar.setString("Conectando…");
            cancel.addActionListener(event -> {
                cancelled.set(true);
                cancel.setEnabled(false);
                status.setText("Cancelando…");
            });
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    cancelled.set(true);
                    cancel.setEnabled(false);
                    status.setText("Cancelando…");
                }
            });

            JPanel center = new JPanel(new GridLayout(3, 1, 0, 8));
            center.add(status);
            center.add(progressBar);
            center.add(detail);
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.add(cancel);
            dialog.setLayout(new BorderLayout(16, 12));
            dialog.add(center, BorderLayout.CENTER);
            dialog.add(buttons, BorderLayout.SOUTH);
            dialog.setMinimumSize(new Dimension(460, 150));
            dialog.setSize(520, 190);
            dialog.setLocationRelativeTo(null);
        }

        private void showDialog() {
            dialog.setVisible(true);
        }

        private void update(DownloadProgress progress) {
            if (!dialog.isDisplayable()) return;
            if (progress.isIndeterminate()) {
                progressBar.setIndeterminate(true);
                progressBar.setString("Baixando…");
                detail.setText(formatBytes(progress.downloadedBytes));
            } else {
                progressBar.setIndeterminate(false);
                progressBar.setValue(progress.percent());
                progressBar.setString(progress.percent() + "%");
                long eta = progress.remainingSeconds();
                detail.setText(formatBytes(progress.downloadedBytes) + " de "
                        + formatBytes(progress.totalBytes) + " · "
                        + formatBytes(progress.bytesPerSecond) + "/s · ETA "
                        + (eta < 0 ? "calculando" : formatDuration(eta)));
            }
        }

        private void close() {
            if (dialog.isDisplayable()) dialog.dispose();
        }
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024L) return bytes + " B";
        double value = bytes / 1024.0;
        if (value < 1024.0) return String.format(Locale.ROOT, "%.1f KB", value);
        value /= 1024.0;
        if (value < 1024.0) return String.format(Locale.ROOT, "%.1f MB", value);
        return String.format(Locale.ROOT, "%.1f GB", value / 1024.0);
    }

    private static String formatDuration(long seconds) {
        if (seconds < 60L) return seconds + "s";
        return (seconds / 60L) + "m " + (seconds % 60L) + "s";
    }
}
