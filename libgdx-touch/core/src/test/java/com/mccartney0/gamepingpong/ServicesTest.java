package com.mccartney0.gamepingpong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mccartney0.gamepingpong.services.AchievementProgress;
import com.mccartney0.gamepingpong.services.GameServices;
import com.mccartney0.gamepingpong.services.GameServicesCallback;
import com.mccartney0.gamepingpong.services.NoopMonetizationService;
import com.mccartney0.gamepingpong.services.RewardDeliveryGate;
import org.junit.Test;

public class ServicesTest {

    private static final class FakeGameServices implements GameServices {
        int unlockCount;
        int incrementCount;

        @Override public void signIn(GameServicesCallback callback) { }
        @Override public boolean isSignedIn() { return true; }
        @Override public void submitScore(String id, long score,
                GameServicesCallback callback) { }
        @Override public void showLeaderboard(String id) { }
        @Override public void showAllLeaderboards() { }
        @Override public void unlockAchievement(String id,
                GameServicesCallback callback) { unlockCount++; }
        @Override public void incrementAchievement(String id, int steps,
                GameServicesCallback callback) { incrementCount += steps; }
        @Override public void showAchievements() { }
    }

    @Test
    public void achievementsAreIdempotent() {
        FakeGameServices services = new FakeGameServices();
        AchievementProgress progress = new AchievementProgress();

        progress.onPlayerPoint(services, "first_point");
        progress.onPlayerPoint(services, "first_point");
        progress.onMatchWin(services, "match_win");
        progress.onMatchWin(services, "match_win");

        assertEquals(2, services.unlockCount);
        assertTrue(progress.isFirstPointSent());
        assertTrue(progress.isMatchWinSent());
    }

    @Test
    public void missingAchievementIdDoesNotConsumeProgress() {
        FakeGameServices services = new FakeGameServices();
        AchievementProgress progress = new AchievementProgress();

        progress.onPlayerPoint(services, null);
        assertFalse(progress.isFirstPointSent());
        assertEquals(0, services.unlockCount);
    }

    @Test
    public void rewardDeliveryIsExactlyOnce() {
        RewardDeliveryGate gate = new RewardDeliveryGate();
        assertTrue(gate.tryDeliver());
        assertFalse(gate.tryDeliver());
        assertTrue(gate.isDelivered());
    }

    @Test
    public void monetizationFallbackIsOffline() {
        NoopMonetizationService monetization = new NoopMonetizationService();
        assertFalse(monetization.isRewardedReady());
        monetization.setBannerVisible(true);
        monetization.showRewarded("continue", new com.mccartney0.gamepingpong.services.RewardCallback() {
            @Override public void onRewardEarned(String placement, int amount) {
                throw new AssertionError("nao deveria recompensar offline");
            }
            @Override public void onAdUnavailable(String placement, String reason) {
                assertEquals("continue", placement);
            }
        });
    }
}
