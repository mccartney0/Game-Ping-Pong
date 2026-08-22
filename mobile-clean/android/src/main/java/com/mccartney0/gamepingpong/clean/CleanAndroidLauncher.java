package com.mccartney0.gamepingpong.clean;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/** Minimal Android entry point. No ad view, overlay, external service or custom layout. */
public final class CleanAndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        configuration.useGyroscope = false;
        configuration.numSamples = 0;
        configuration.useGL30 = false;
        configuration.disableAudio = true;
        initialize(new CleanPongGame(), configuration);
    }
}
