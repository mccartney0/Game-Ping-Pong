package com.mccartney0.gamepingpong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mccartney0.gamepingpong.input.PaddleSide;
import org.junit.Test;

public class PowersAndModesTest {

    @Test
    public void abilitiesSpendEnergyAndCycle() {
        TouchPongWorld world = new TouchPongWorld();
        assertEquals(AbilityType.OVERDRIVE, world.getAbility(PaddleSide.BOTTOM));
        world.activateAbility(PaddleSide.BOTTOM);
        assertEquals(1, world.playerAbilityActivations);
        assertTrue(world.isOverdriveActive(PaddleSide.BOTTOM));
        assertTrue(world.getEnergy(PaddleSide.BOTTOM) < 70f);

        world.cycleAbility(PaddleSide.BOTTOM);
        assertEquals(AbilityType.SHIELD, world.getAbility(PaddleSide.BOTTOM));
        world.activateAbility(PaddleSide.BOTTOM);
        assertTrue(world.isShieldActive(PaddleSide.BOTTOM));
    }

    @Test
    public void rewardEnergyIsCappedAndPowerUpSpawns() {
        TouchPongWorld world = new TouchPongWorld();
        world.grantRewardEnergy(1000);
        assertEquals(100f, world.getEnergy(PaddleSide.BOTTOM), 0.001f);
        for (int frame = 0; frame < 240; frame++) {
            world.update(1f / 60f);
        }
        assertTrue(world.powerUpsSpawned > 0);
        assertNotNull(world.getActivePowerUp());
        world.movePaddleTo(PaddleSide.BOTTOM, world.getActivePowerUp().getX());
        world.spawnPowerUpAt(world.playerX, world.playerY, PowerUpType.SPLIT);
        world.update(0.01f);
        assertEquals(1, world.powerUpsCollected);
        assertTrue(world.getSplitTicks() > 0f);
    }

    @Test
    public void survivalLosesLivesAndCampaignAdvancesBosses() {
        TouchPongWorld survival = new TouchPongWorld();
        survival.setMode(MobileGameMode.SURVIVAL);
        survival.ballY = -1f;
        survival.ballDy = -1f;
        survival.update(0.01f);
        assertEquals(2, survival.getSurvivalLives());
        assertFalse(survival.isMatchOver());

        TouchPongWorld campaign = new TouchPongWorld();
        campaign.setMode(MobileGameMode.CAMPAIGN);
        for (int point = 0; point < 3; point++) {
            campaign.ballY = TouchPongWorld.HEIGHT + 1f;
            campaign.ballDy = 1f;
            campaign.update(0.01f);
        }
        assertEquals(1, campaign.getBossIndex());
        assertEquals("MIRROR", campaign.getBossName());
        assertEquals(3, campaign.getBossHealth());
    }

    @Test
    public void menuNavigatesModesAndPauseActions() {
        final int[] starts = {0};
        final int[] resumes = {0};
        MobileMenu menu = new MobileMenu(new MobileMenu.Listener() {
            @Override
            public void onStartMode(MobileGameMode mode) {
                starts[0]++;
            }

            @Override
            public void onResumeGame() {
                resumes[0]++;
            }

            @Override public void onOpenMainMenu() { }
            @Override public void onOpenHelp() { }
            @Override public void onOpenSettings() { }
            @Override public void onShowLeaderboards() { }
            @Override public void onShowAchievements() { }
            @Override public void onShowRewarded() { }
            @Override public void onCycleEffectsQuality() { }
        });

        menu.showModes();
        menu.tapRow(MobileGameMode.TURBO.ordinal());
        assertEquals(1, starts[0]);
        assertEquals(MobileGameMode.TURBO, menu.getSelectedMode());
        menu.showPause();
        menu.tapRow(0);
        assertEquals(1, resumes[0]);
    }
}
