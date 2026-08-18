package com.mccartney0.gamepingpong;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.mccartney0.gamepingpong.services.MonetizationService;
import com.mccartney0.gamepingpong.services.RewardCallback;
import com.mccartney0.gamepingpong.services.RewardDeliveryGate;

public final class AndroidMonetizationService implements MonetizationService {

    private final Activity activity;
    private volatile RewardedAd rewardedAd;
    private AdView banner;
    private boolean bannerRequestedVisible;
    private RewardDeliveryGate rewardDeliveryGate = new RewardDeliveryGate();

    public AndroidMonetizationService(Activity activity) {
        this.activity = activity;
        activity.runOnUiThread(this::loadRewardedOnUiThread);
    }

    public void attachBanner(FrameLayout root) {
        activity.runOnUiThread(() -> {
            if (banner != null) {
                return;
            }
            banner = new AdView(activity);
            banner.setAdUnitId(activity.getString(R.string.banner_ad_unit_id));
            int widthPixels = activity.getResources().getDisplayMetrics().widthPixels;
            float density = activity.getResources().getDisplayMetrics().density;
            int widthDp = Math.max(1, Math.round(widthPixels / density));
            banner.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    activity, widthDp));
            banner.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    banner.setVisibility(bannerRequestedVisible ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError error) {
                    banner.setVisibility(View.GONE);
                }
            });
            banner.setVisibility(bannerRequestedVisible ? View.VISIBLE : View.GONE);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);
            root.addView(banner, params);
            banner.loadAd(new AdRequest.Builder().build());
        });
    }

    @Override
    public boolean isRewardedReady() {
        return rewardedAd != null;
    }

    @Override
    public void showRewarded(String placement, RewardCallback callback) {
        activity.runOnUiThread(() -> showRewardedOnUiThread(placement, callback));
    }

    private void showRewardedOnUiThread(String placement, RewardCallback callback) {
        RewardedAd ad = rewardedAd;
        if (ad == null) {
            notifyUnavailable(callback, placement, "ad_not_ready");
            loadRewardedOnUiThread();
            return;
        }

        rewardedAd = null;
        rewardDeliveryGate = new RewardDeliveryGate();
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadRewardedOnUiThread();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                loadRewardedOnUiThread();
                notifyUnavailable(callback, placement, "show_failed");
            }
        });
        ad.show(activity, rewardItem -> {
            if (rewardDeliveryGate.tryDeliver()) {
                notifyReward(callback, placement, rewardItem);
            }
        });
    }

    private void loadRewardedOnUiThread() {
        RewardedAd.load(
                activity,
                activity.getString(R.string.rewarded_ad_unit_id),
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        rewardedAd = null;
                    }
                });
    }

    @Override
    public void setBannerVisible(boolean visible) {
        activity.runOnUiThread(() -> {
            bannerRequestedVisible = visible;
            if (banner != null) {
                banner.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void destroy() {
        activity.runOnUiThread(() -> {
            if (banner != null) {
                ViewGroup parent = (ViewGroup) banner.getParent();
                if (parent != null) {
                    parent.removeView(banner);
                }
                banner.destroy();
                banner = null;
            }
            rewardedAd = null;
        });
    }

    private void notifyReward(RewardCallback callback, String placement,
            RewardItem rewardItem) {
        if (callback != null) {
            callback.onRewardEarned(
                    placement, Math.max(1, rewardItem.getAmount()));
        }
    }

    private void notifyUnavailable(RewardCallback callback, String placement,
            String reason) {
        if (callback != null) {
            callback.onAdUnavailable(placement, reason);
        }
    }
}
