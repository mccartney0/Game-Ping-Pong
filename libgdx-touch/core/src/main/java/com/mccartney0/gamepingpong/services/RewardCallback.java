package com.mccartney0.gamepingpong.services;

public interface RewardCallback {

    void onRewardEarned(String placement, int amount);

    void onAdUnavailable(String placement, String reason);
}
