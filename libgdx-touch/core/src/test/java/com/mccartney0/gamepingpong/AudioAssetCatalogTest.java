package com.mccartney0.gamepingpong;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AudioAssetCatalogTest {
    @Test
    public void mapsGameModesToStableMusicTracks() {
        AudioAssetCatalog catalog = new AudioAssetCatalog();
        assertEquals(AudioAssetCatalog.MusicTrack.GAMEPLAY,
                catalog.trackFor(MobileGameMode.CLASSIC));
        assertEquals(AudioAssetCatalog.MusicTrack.GAMEPLAY,
                catalog.trackFor(MobileGameMode.SURVIVAL));
        assertEquals(AudioAssetCatalog.MusicTrack.GAMEPLAY,
                catalog.trackFor(MobileGameMode.TURBO));
        assertEquals(AudioAssetCatalog.MusicTrack.GAMEPLAY,
                catalog.trackFor(MobileGameMode.VERSUS));
        assertEquals(AudioAssetCatalog.MusicTrack.MUTANT,
                catalog.trackFor(MobileGameMode.MUTANT));
        assertEquals(AudioAssetCatalog.MusicTrack.CAMPAIGN,
                catalog.trackFor(MobileGameMode.CAMPAIGN));
        catalog.dispose();
    }

    @Test
    public void disabledCatalogStopsWithoutBackendCalls() {
        AudioAssetCatalog catalog = new AudioAssetCatalog();
        catalog.setEnabled(false);
        assertEquals(false, catalog.isEnabled());
        catalog.playMusic(AudioAssetCatalog.MusicTrack.MENU);
        catalog.play(AudioAssetCatalog.Cue.UI_TAP);
        catalog.dispose();
    }
}
