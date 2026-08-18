package com.mccartney0.gamepingpong.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.mccartney0.gamepingpong.BuildConfig;
import com.mccartney0.release.DownloadListener;
import com.mccartney0.release.DownloadProgress;
import com.mccartney0.release.GitHubReleaseUpdater;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AndroidAutoUpdater {
    private static final String REPOSITORY = "mccartney0/Game-Ping-Pong";
    private static final String ASSET_NAME = "game-ping-pong-touch-android.apk";
    private static final String PREFS = "foldcut_release_updates";
    private static final String LAST_CHECK = "last_check_ms";
    private static final long CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AndroidAutoUpdater() { }

    public static void check(Activity activity) {
        if (activity == null || activity.isFinishing() || !shouldCheck(activity)) return;
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putLong(LAST_CHECK, System.currentTimeMillis()).apply();
        EXECUTOR.execute(() -> {
            try {
                GitHubReleaseUpdater.UpdateInfo update = GitHubReleaseUpdater.checkLatest(
                        REPOSITORY, BuildConfig.VERSION_NAME, ASSET_NAME);
                if (update != null && !activity.isFinishing()) {
                    MAIN.post(() -> showUpdateDialog(activity, update));
                }
            } catch (Exception ignored) {
                // Updates are optional and must never block the game startup.
            }
        });
    }

    private static boolean shouldCheck(Context context) {
        long lastCheck = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getLong(LAST_CHECK, 0L);
        return System.currentTimeMillis() - lastCheck >= CHECK_INTERVAL_MS;
    }

    private static void showUpdateDialog(Activity activity, GitHubReleaseUpdater.UpdateInfo update) {
        if (activity.isFinishing()) return;
        new AlertDialog.Builder(activity)
                .setTitle("Atualização disponível")
                .setMessage("Game Ping Pong Touch " + update.version + " está disponível.")
                .setNegativeButton("Depois", null)
                .setPositiveButton("Baixar", (dialog, which) -> downloadAndInstall(activity, update))
                .show();
    }

    private static void downloadAndInstall(Activity activity, GitHubReleaseUpdater.UpdateInfo update) {
        if (activity.isFinishing()) return;
        File directory = new File(activity.getCacheDir(), "updates");
        File apkFile = new File(directory, ASSET_NAME);
        DownloadUi ui = new DownloadUi(activity);

        EXECUTOR.execute(() -> GitHubReleaseUpdater.downloadToFile(
                update.downloadUrl,
                apkFile,
                ui.cancelled,
                new DownloadListener() {
                    @Override
                    public void onProgress(DownloadProgress progress) {
                        MAIN.post(() -> ui.update(progress));
                    }

                    @Override
                    public void onCompleted(File file) {
                        try {
                            String expectedSha = update.checksumUrl == null
                                    ? null
                                    : GitHubReleaseUpdater.downloadText(update.checksumUrl)
                                            .split("\\s+")[0];
                            if (!GitHubReleaseUpdater.sha256Matches(file, expectedSha)) {
                                throw new IllegalStateException("Checksum SHA-256 inválido");
                            }
                            MAIN.post(() -> {
                                ui.close();
                                launchInstaller(activity, file);
                            });
                        } catch (Exception error) {
                            if (file.exists()) file.delete();
                            postError(activity, ui, "A validação da atualização falhou.");
                        }
                    }

                    @Override
                    public void onCancelled() {
                        MAIN.post(() -> {
                            ui.close();
                            if (!activity.isFinishing()) {
                                Toast.makeText(activity, "Download cancelado.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        postError(activity, ui, "Não foi possível baixar a atualização.");
                    }
                }));
    }

    private static void postError(Activity activity, DownloadUi ui, String message) {
        MAIN.post(() -> {
            ui.close();
            if (!activity.isFinishing()) {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void launchInstaller(Activity activity, File apkFile) {
        if (activity.isFinishing()) return;
        try {
            Uri apkUri = FileProvider.getUriForFile(
                    activity,
                    BuildConfig.APPLICATION_ID + ".update-file-provider",
                    apkFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception error) {
            Toast.makeText(activity, "Abra a página da Release para instalar manualmente.", Toast.LENGTH_LONG).show();
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://github.com/" + REPOSITORY + "/releases/latest"));
            activity.startActivity(browser);
        }
    }

    private static final class DownloadUi {
        private final AlertDialog dialog;
        private final ProgressBar progressBar;
        private final TextView status;
        private final TextView detail;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        private DownloadUi(Activity activity) {
            LinearLayout content = new LinearLayout(activity);
            content.setOrientation(LinearLayout.VERTICAL);
            int padding = dp(activity, 22);
            content.setPadding(padding, dp(activity, 8), padding, padding);

            status = new TextView(activity);
            detail = new TextView(activity);
            progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(100);
            progressBar.setIndeterminate(true);

            content.addView(status, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            content.addView(progressBar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            content.addView(detail, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            dialog = new AlertDialog.Builder(activity)
                    .setTitle("Baixando atualização")
                    .setView(content)
                    .setNegativeButton("Cancelar", null)
                    .create();
            dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setOnClickListener(view -> {
                        cancelled.set(true);
                        view.setEnabled(false);
                        status.setText("Cancelando…");
                    }));
            dialog.setOnDismissListener(ignored -> cancelled.set(true));
            status.setText("Conectando…");
            detail.setText("Aguarde");
            dialog.show();
        }

        private void update(DownloadProgress progress) {
            if (!dialog.isShowing()) return;
            if (progress.isIndeterminate()) {
                progressBar.setIndeterminate(true);
                status.setText("Baixando…");
                detail.setText(formatBytes(progress.downloadedBytes));
            } else {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(progress.percent());
                status.setText(progress.percent() + "%");
                long eta = progress.remainingSeconds();
                String etaText = eta < 0 ? "calculando ETA" : "ETA " + formatDuration(eta);
                detail.setText(formatBytes(progress.downloadedBytes) + " de "
                        + formatBytes(progress.totalBytes) + " · "
                        + formatBytes(progress.bytesPerSecond) + "/s · " + etaText);
            }
        }

        private void close() {
            if (dialog.isShowing()) dialog.dismiss();
        }

        private static int dp(Activity activity, int value) {
            return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
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
