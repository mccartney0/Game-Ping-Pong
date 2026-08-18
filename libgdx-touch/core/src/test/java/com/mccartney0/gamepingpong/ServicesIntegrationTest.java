package com.mccartney0.gamepingpong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mccartney0.gamepingpong.services.GameServices;
import com.mccartney0.gamepingpong.services.GameServicesCallback;
import com.mccartney0.gamepingpong.services.MonetizationService;
import com.mccartney0.gamepingpong.services.RewardCallback;
import org.junit.Test;

public class ServicesIntegrationTest {

    private static final class FakeGameServices implements GameServices {
        int scoreSubmissions;
        String leaderboardId;
        long score;

        @Override public void signIn(GameServicesCallback callback) { }
        @Override public boolean isSignedIn() { return true; }
        @Override public void submitScore(String id, long value,
                GameServicesCallback callback) {
            scoreSubmissions++;
            leaderboardId = id;
            score = value;
        }
        @Override public void showLeaderboard(String id) { }
        @Override public void showAllLeaderboards() { }
        @Override public void unlockAchievement(String id,
                GameServicesCallback callback) { }
        @Override public void incrementAchievement(String id, int steps,
                GameServicesCallback callback) { }
        @Override public void showAchievements() { }
    }

    private static final class FakeMonetizationService implements MonetizationService {
        boolean bannerVisible;

        @Override public boolean isRewardedReady() { return true; }
        @Override public void showRewarded(String placement,
                RewardCallback callback) { }
        @Override public void setBannerVisible(boolean visible) {
            bannerVisible = visible;
        }
    }

    @Test
    public void finishedMatchSubmitsScoreOnceAndRevealsBanner() {
        FakeGameServices services = new FakeGameServices();
        FakeMonetizationService monetization = new FakeMonetizationService();
        PingPongTouchGame game = new PingPongTouchGame(services, monetization);
        game.setCurrentLeaderboardId("survival_score");
        game.getWorld().playerScore = TouchPongWorld.MATCH_SCORE;

        game.submitCurrentMatchScoreIfFinished();
        game.submitCurrentMatchScoreIfFinished();

        assertEquals(1, services.scoreSubmissions);
        assertEquals("survival_score", services.leaderboardId);
        assertTrue(services.score >= TouchPongWorld.MATCH_SCORE);
        assertTrue(monetization.bannerVisible);
    }
}
