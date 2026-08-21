package com.mccartney0.gamepingpong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MobileTransitionTest {

    @Test
    public void countdownFinishesAtConfiguredDuration() {
        MobileTransition transition = new MobileTransition();
        transition.begin(MobileTransition.Type.START_MATCH, 1.6f);

        assertTrue(transition.isActive());
        assertEquals("4", transition.getCountdownText());
        assertEquals(0f, transition.getProgress(), 0.0001f);

        transition.update(0.8f);
        assertTrue(transition.isActive());
        assertEquals("2", transition.getCountdownText());
        assertEquals(0.5f, transition.getProgress(), 0.0001f);

        transition.update(0.8f);
        assertFalse(transition.isActive());
        assertEquals(1f, transition.getProgress(), 0.0001f);
        assertEquals("GO!", transition.getCountdownText());
    }

    @Test
    public void nullTypeFallsBackToStartMatch() {
        MobileTransition transition = new MobileTransition();
        transition.begin(null, 0.5f);

        assertEquals(MobileTransition.Type.START_MATCH, transition.getType());
    }
}
