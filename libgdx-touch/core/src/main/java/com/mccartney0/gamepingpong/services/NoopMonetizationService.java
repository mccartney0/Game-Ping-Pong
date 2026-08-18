package com.mccartney0.gamepingpong.services;

public final class NoopMonetizationService implements MonetizationService {

    @Override
    public boolean isRewardedReady() {
        return false;
    }

    @Override
    public void showRewarded(String placement, RewardCallback callback) {
        if (callback != null) {
            callback.onAdUnavailable(placement, "ads_indisponiveis_offline");
        }
    }

    @Override
    public void setBannerVisible(boolean visible) {
        // O desktop não possui banner Android.
    }
}
