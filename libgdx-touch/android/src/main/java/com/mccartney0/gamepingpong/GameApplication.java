package com.mccartney0.gamepingpong;

import android.app.Application;

import com.google.android.gms.games.PlayGamesSdk;

public final class GameApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PlayGamesSdk.initialize(this);
    }
}
