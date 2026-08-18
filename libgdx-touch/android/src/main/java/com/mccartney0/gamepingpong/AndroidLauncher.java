package com.mccartney0.gamepingpong;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

    private PingPongTouchGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        configuration.hideStatusBar = true;
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        configuration.numSamples = 0;
        game = new PingPongTouchGame(new AndroidGameServices(this));
        game.setCurrentLeaderboardId(getString(R.string.leaderboard_survival_score));
        initialize(game, configuration);
    }

    public PingPongTouchGame getGame() {
        return game;
    }
}
