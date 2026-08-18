package com.mccartney0.gamepingpong.services;

public interface GameServices {

    void signIn(GameServicesCallback callback);

    boolean isSignedIn();

    void submitScore(String leaderboardId, long score, GameServicesCallback callback);

    void showLeaderboard(String leaderboardId);

    void showAllLeaderboards();

    void unlockAchievement(String achievementId, GameServicesCallback callback);

    void incrementAchievement(String achievementId, int steps,
            GameServicesCallback callback);

    void showAchievements();
}
