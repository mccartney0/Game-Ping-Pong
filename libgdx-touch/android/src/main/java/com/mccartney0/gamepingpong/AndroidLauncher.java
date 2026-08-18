package com.mccartney0.gamepingpong;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

    private PingPongTouchGame game;
    private AndroidMonetizationService monetization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        configuration.hideStatusBar = true;
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
        FrameLayout root = new FrameLayout(this);
        root.addView(gameView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        monetization.attachBanner(root);
        setContentView(root);
    }

    public PingPongTouchGame getGame() {
        return game;
    }

    @Override
    protected void onDestroy() {
        if (monetization != null) {
            monetization.destroy();
        }
        super.onDestroy();
    }
}
