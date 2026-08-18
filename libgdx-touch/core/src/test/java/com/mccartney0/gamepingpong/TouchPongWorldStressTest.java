package com.mccartney0.gamepingpong;

import static org.junit.Assert.assertTrue;

import com.mccartney0.gamepingpong.input.PaddleSide;
import org.junit.Test;

public class TouchPongWorldStressTest {

    @Test
    public void longDeterministicSimulationStaysFinite() {
        TouchPongWorld world = new TouchPongWorld();
        for (int frame = 0; frame < 60 * 60 * 5; frame++) {
            if (frame % 11 == 0) {
                float x = 1f + (frame % 1400) / 100f;
                world.movePaddleTo(PaddleSide.BOTTOM, x);
            }
            if (frame % 17 == 0) {
                world.movePaddleTo(PaddleSide.TOP, TouchPongWorld.WIDTH - 1f);
            }
            if (frame % 900 == 0) {
                world.activateAbility(PaddleSide.BOTTOM);
            }
            world.update(1f / 60f);
            assertFinite(world.ballX);
            assertFinite(world.ballY);
            assertFinite(world.ballDx);
            assertFinite(world.ballDy);
            assertTrue(world.playerScore >= 0);
            assertTrue(world.enemyScore >= 0);
            assertTrue(world.getBallEffects().getEmittedParticles() >= 0);
        }
    }

    private void assertFinite(float value) {
        assertTrue("valor nao finito: " + value,
                !Float.isNaN(value) && !Float.isInfinite(value));
    }
}
