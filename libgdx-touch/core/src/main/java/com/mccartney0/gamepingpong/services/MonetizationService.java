package com.mccartney0.gamepingpong.services;

public interface MonetizationService {

    boolean isRewardedReady();

    void showRewarded(String placement, RewardCallback callback);

    void setBannerVisible(boolean visible);
}
