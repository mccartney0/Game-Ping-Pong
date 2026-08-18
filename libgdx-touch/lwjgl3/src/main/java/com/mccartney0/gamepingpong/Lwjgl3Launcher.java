package com.mccartney0.gamepingpong;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mccartney0.gamepingpong.update.DesktopAutoUpdater;

public class Lwjgl3Launcher {

    public static void main(String[] args) {
        DesktopAutoUpdater.check();
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Game Ping Pong - Touch Preview");
        configuration.setWindowedMode(960, 720);
        configuration.setForegroundFPS(60);
        configuration.useVsync(true);
        new Lwjgl3Application(new PingPongTouchGame(), configuration);
    }
}
