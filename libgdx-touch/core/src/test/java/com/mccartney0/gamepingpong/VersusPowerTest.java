package com.mccartney0.gamepingpong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mccartney0.gamepingpong.input.PaddleSide;
import org.junit.Test;

public class VersusPowerTest {

    @Test
    public void bothPlayersActivatePowersAndCollectPowerUps() {
        TouchPongWorld world = new TouchPongWorld();
        world.setMode(MobileGameMode.VERSUS);

        assertEquals(AbilityType.OVERDRIVE, world.getAbility(PaddleSide.BOTTOM));
        assertEquals(AbilityType.SHIELD, world.getAbility(PaddleSide.TOP));

        world.activateAbility(PaddleSide.BOTTOM);
        assertTrue(world.isOverdriveActive(PaddleSide.BOTTOM));
        world.activateAbility(PaddleSide.TOP);
        assertTrue(world.isShieldActive(PaddleSide.TOP));

        world.spawnPowerUpAt(world.enemyX, world.enemyY, PowerUpType.MULTI);
        world.update(0.01f);
        assertEquals(1, world.powerUpsCollected);
        assertEquals(2, world.enemyPointMultiplier);

        world.cycleAbility(PaddleSide.TOP);
        assertEquals(AbilityType.WIDE, world.getAbility(PaddleSide.TOP));
        world.activateAbility(PaddleSide.TOP);
        assertTrue(world.isWideActive(PaddleSide.TOP));
        assertTrue(world.enemyAbilityActivations >= 2);
    }
}
