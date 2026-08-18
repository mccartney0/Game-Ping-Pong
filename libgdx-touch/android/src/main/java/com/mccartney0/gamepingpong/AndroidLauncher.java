package com.mccartney0.gamepingpong;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        configuration.hideStatusBar = true;
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        configuration.numSamples = 0;
        initialize(new PingPongTouchGame(), configuration);
    }
}
