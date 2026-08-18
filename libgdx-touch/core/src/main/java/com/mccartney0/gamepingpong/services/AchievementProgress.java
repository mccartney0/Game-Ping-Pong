package com.mccartney0.gamepingpong.services;

public final class AchievementProgress {

    private boolean firstPointSent;
    private boolean matchWinSent;

    public void onPlayerPoint(GameServices services, String achievementId) {
        if (firstPointSent || services == null
                || achievementId == null || achievementId.trim().isEmpty()) {
            return;
        }
        firstPointSent = true;
        services.unlockAchievement(achievementId, null);
    }

    public void onMatchWin(GameServices services, String achievementId) {
        if (matchWinSent || services == null
                || achievementId == null || achievementId.trim().isEmpty()) {
            return;
        }
        matchWinSent = true;
        services.unlockAchievement(achievementId, null);
    }

    public boolean isFirstPointSent() {
        return firstPointSent;
    }

    public boolean isMatchWinSent() {
        return matchWinSent;
    }
}
