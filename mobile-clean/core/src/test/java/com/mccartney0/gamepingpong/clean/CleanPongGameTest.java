package com.mccartney0.gamepingpong.clean;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CleanPongGameTest {

    @Test
    public void startsOnMenuWithZeroScore() {
        CleanPongGame game = new CleanPongGame();
        assertEquals("MENU", game.getScreenName());
        assertEquals(0, game.getPlayerScore());
        assertEquals(0, game.getEnemyScore());
    }
}
