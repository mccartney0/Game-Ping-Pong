package com.mccartney0.gamepingpong.update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.mccartney0.gamepingpong.BuildConfig;
import com.mccartney0.release.GitHubReleaseUpdater;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        if (activity == null || !shouldCheck(activity)) return;
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
        new android.app.AlertDialog.Builder(activity)
                .setTitle("Atualização disponível")
                .setMessage("Game Ping Pong Touch " + update.version + " está disponível.")
                .setNegativeButton("Depois", null)
                .setPositiveButton("Baixar", (dialog, which) -> downloadAndInstall(activity, update))
                .show();
    }

    private static void downloadAndInstall(Activity activity, GitHubReleaseUpdater.UpdateInfo update) {
        Toast.makeText(activity, "Baixando atualização…", Toast.LENGTH_SHORT).show();
        EXECUTOR.execute(() -> {
            try {
                byte[] apk = GitHubReleaseUpdater.download(update.downloadUrl);
                String expectedSha = update.checksumUrl == null
                        ? null : GitHubReleaseUpdater.downloadText(update.checksumUrl).split("\\s+")[0];
                if (!GitHubReleaseUpdater.sha256Matches(apk, expectedSha)) {
                    throw new IllegalStateException("Checksum SHA-256 inválido");
                }
                File directory = new File(activity.getCacheDir(), "updates");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IllegalStateException("Não foi possível criar o cache de atualização");
                }
                File apkFile = new File(directory, ASSET_NAME);
                try (FileOutputStream output = new FileOutputStream(apkFile)) {
                    output.write(apk);
                }
                MAIN.post(() -> launchInstaller(activity, apkFile));
            } catch (Exception error) {
                MAIN.post(() -> Toast.makeText(
                        activity, "Não foi possível atualizar agora.", Toast.LENGTH_LONG).show());
            }
        });
    }

    private static void launchInstaller(Activity activity, File apkFile) {
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
}
