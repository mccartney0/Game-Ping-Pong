package com.mccartney0.gamepingpong.services;

public final class NoopGameServices implements GameServices {

    @Override
    public void signIn(GameServicesCallback callback) {
        notifyFailure(callback, "Google Play Games indisponivel nesta plataforma");
    }

    @Override
    public boolean isSignedIn() {
        return false;
    }

    @Override
    public void submitScore(String leaderboardId, long score, GameServicesCallback callback) {
        notifyFailure(callback, "Score mantido apenas localmente: servico indisponivel");
    }

    @Override
    public void showLeaderboard(String leaderboardId) {
        // Sem UI de plataforma no desktop.
    }

    @Override
    public void showAllLeaderboards() {
        // Sem UI de plataforma no desktop.
    }

    @Override
    public void unlockAchievement(String achievementId,
            GameServicesCallback callback) {
        notifyFailure(callback, "Achievements indisponiveis nesta plataforma");
    }

    @Override
    public void incrementAchievement(String achievementId, int steps,
            GameServicesCallback callback) {
        notifyFailure(callback, "Achievements indisponiveis nesta plataforma");
    }

    @Override
    public void showAchievements() {
        // Sem UI de plataforma no desktop.
    }

    private void notifyFailure(GameServicesCallback callback, String message) {
        if (callback != null) {
            callback.onFailure(message);
        }
    }
}
