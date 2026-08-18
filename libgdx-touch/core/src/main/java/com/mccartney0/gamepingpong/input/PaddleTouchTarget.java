package com.mccartney0.gamepingpong.input;

public interface PaddleTouchTarget {

    void movePaddleTo(PaddleSide side, float worldX);

    void activateAbility(PaddleSide side);

    void cycleAbility(PaddleSide side);

    void togglePause();
}
