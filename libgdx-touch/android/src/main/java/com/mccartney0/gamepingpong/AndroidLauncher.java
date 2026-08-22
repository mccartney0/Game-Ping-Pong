package com.mccartney0.gamepingpong;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mccartney0.gamepingpong.update.AndroidAutoUpdater;

public class AndroidLauncher extends AndroidApplication {

    private PingPongTouchGame game;
    private AndroidMonetizationService monetization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidAutoUpdater.check(this);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        configuration.numSamples = 0;
        monetization = new AndroidMonetizationService(this);
        game = new PingPongTouchGame(new AndroidGameServices(this), monetization);
        game.setCurrentLeaderboardId(getString(R.string.leaderboard_survival_score));
        game.setAchievementIds(
                getString(R.string.achievement_first_point),
                getString(R.string.achievement_match_win));

        View gameView = initializeForView(game, configuration);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(android.graphics.Color.BLACK);

        FrameLayout gameHost = new FrameLayout(this);
        gameHost.setBackgroundColor(android.graphics.Color.BLACK);
        root.addView(gameHost, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        gameHost.addView(gameView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout adHost = new FrameLayout(this);
        adHost.setBackgroundColor(android.graphics.Color.BLACK);
        root.addView(adHost, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        monetization.attachBanner(adHost);
        setContentView(root);
    }

    public PingPongTouchGame getGame() {
        return game;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (game != null) {
            game.onAndroidWindowFocusChanged(hasFocus);
        }
    }

    @Override
    protected void onPause() {
        if (monetization != null) {
            monetization.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (monetization != null) {
            monetization.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (monetization != null) {
            monetization.destroy();
        }
        super.onDestroy();
    }
}
