package com.mccartney0.gamepingpong;

import android.app.Activity;
import com.badlogic.gdx.utils.Logger;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.mccartney0.gamepingpong.services.GameServices;
import com.mccartney0.gamepingpong.services.GameServicesCallback;

public final class AndroidGameServices implements GameServices {

    private static final int LEADERBOARD_UI_REQUEST = 9004;
    private static final int ACHIEVEMENT_UI_REQUEST = 9003;
    private static final String TAG = "GamePingPongPGS";

    private final Activity activity;
    private final GamesSignInClient signInClient;
    private final Logger logger = new Logger(TAG, Logger.INFO);
    private volatile boolean signedIn;

    public AndroidGameServices(Activity activity) {
        this.activity = activity;
        this.signInClient = PlayGames.getGamesSignInClient(activity);
        refreshAuthentication(null);
    }

    @Override
    public void signIn(GameServicesCallback callback) {
        signInClient.signIn().addOnCompleteListener(task -> {
            boolean authenticated = task.isSuccessful()
                    && task.getResult() != null
                    && task.getResult().isAuthenticated();
            signedIn = authenticated;
            if (authenticated) {
                notifySuccess(callback, "Google Play Games autenticado");
            } else {
                notifyFailure(callback, "Nao foi possivel autenticar no Google Play Games");
            }
        });
    }

    public void refreshAuthentication(GameServicesCallback callback) {
        signInClient.isAuthenticated().addOnCompleteListener(task -> {
            boolean authenticated = task.isSuccessful()
                    && task.getResult() != null
                    && task.getResult().isAuthenticated();
            signedIn = authenticated;
            if (callback == null) {
                return;
            }
            if (authenticated) {
                notifySuccess(callback, "Google Play Games autenticado");
            } else {
                notifyFailure(callback, "Google Play Games ainda nao autenticado");
            }
        });
    }

    @Override
    public boolean isSignedIn() {
        return signedIn;
    }

    @Override
    public void submitScore(String leaderboardId, long score, GameServicesCallback callback) {
        if (leaderboardId == null || leaderboardId.trim().isEmpty()) {
            notifyFailure(callback, "Leaderboard ID vazio");
            return;
        }
        if (score < 0L) {
            notifyFailure(callback, "Score negativo rejeitado localmente");
            return;
        }
        if (!signedIn) {
            notifyFailure(callback, "Score nao enviado: jogador nao autenticado");
            return;
        }

        LeaderboardsClient leaderboardsClient = PlayGames.getLeaderboardsClient(activity);
        leaderboardsClient.submitScoreImmediate(leaderboardId, score)
                .addOnSuccessListener(result -> {
                    logger.info("Score enviado: " + leaderboardId + "=" + score);
                    notifySuccess(callback, "Score enviado ao leaderboard");
                })
                .addOnFailureListener(error -> {
                    logger.error("Falha ao enviar score", error);
                    notifyFailure(callback, "Falha ao enviar score: " + safeMessage(error));
                });
    }

    @Override
    public void showLeaderboard(String leaderboardId) {
        if (leaderboardId == null || leaderboardId.trim().isEmpty()) {
            return;
        }
        ensureSignedIn(() -> {
            LeaderboardsClient leaderboardsClient = PlayGames.getLeaderboardsClient(activity);
            leaderboardsClient.getLeaderboardIntent(leaderboardId)
                    .addOnSuccessListener(intent -> activity.startActivityForResult(
                            intent, LEADERBOARD_UI_REQUEST))
                    .addOnFailureListener(error -> logger.error(
                            "Falha ao abrir leaderboard", error));
        });
    }

    @Override
    public void showAllLeaderboards() {
        ensureSignedIn(() -> {
            LeaderboardsClient leaderboardsClient = PlayGames.getLeaderboardsClient(activity);
            leaderboardsClient.getAllLeaderboardsIntent()
                    .addOnSuccessListener(intent -> activity.startActivityForResult(
                            intent, LEADERBOARD_UI_REQUEST))
                    .addOnFailureListener(error -> logger.error(
                            "Falha ao abrir lista de leaderboards", error));
        });
    }

    @Override
    public void unlockAchievement(String achievementId,
            GameServicesCallback callback) {
        if (!signedIn || !validAchievementId(achievementId)) {
            notifyFailure(callback, "Achievement indisponivel sem autenticacao");
            return;
        }
        AchievementsClient achievements = PlayGames.getAchievementsClient(activity);
        try {
            achievements.unlock(achievementId);
            notifySuccess(callback, "Achievement desbloqueado");
        } catch (RuntimeException error) {
            notifyFailure(callback, "Falha ao desbloquear achievement: "
                    + safeMessage(error));
        }
    }

    @Override
    public void incrementAchievement(String achievementId, int steps,
            GameServicesCallback callback) {
        if (!signedIn || !validAchievementId(achievementId) || steps <= 0) {
            notifyFailure(callback, "Incremento de achievement invalido");
            return;
        }
        AchievementsClient achievements = PlayGames.getAchievementsClient(activity);
        try {
            achievements.increment(achievementId, steps);
            notifySuccess(callback, "Achievement incrementado");
        } catch (RuntimeException error) {
            notifyFailure(callback, "Falha ao incrementar achievement: "
                    + safeMessage(error));
        }
    }

    @Override
    public void showAchievements() {
        ensureSignedIn(() -> {
            AchievementsClient achievements = PlayGames.getAchievementsClient(activity);
            achievements.getAchievementsIntent()
                    .addOnSuccessListener(intent -> activity.startActivityForResult(
                            intent, ACHIEVEMENT_UI_REQUEST))
                    .addOnFailureListener(error -> logger.error(
                            "Falha ao abrir achievements", error));
        });
    }

    private boolean validAchievementId(String achievementId) {
        return achievementId != null && !achievementId.trim().isEmpty();
    }

    private void ensureSignedIn(Runnable action) {
        if (signedIn) {
            action.run();
            return;
        }
        signInClient.signIn().addOnCompleteListener(task -> {
            boolean authenticated = task.isSuccessful()
                    && task.getResult() != null
                    && task.getResult().isAuthenticated();
            signedIn = authenticated;
            if (authenticated) {
                action.run();
            } else {
                logger.info("Leaderboard indisponivel sem autenticacao");
            }
        });
    }

    private void notifySuccess(GameServicesCallback callback, String message) {
        if (callback != null) {
            callback.onSuccess(message);
        }
    }

    private void notifyFailure(GameServicesCallback callback, String message) {
        if (callback != null) {
            callback.onFailure(message);
        }
    }

    private String safeMessage(Exception error) {
        String message = error == null ? null : error.getMessage();
        return message == null || message.trim().isEmpty() ? "erro desconhecido" : message;
    }
}
