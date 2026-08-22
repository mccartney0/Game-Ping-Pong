package com.mccartney0.gamepingpong;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/** Catálogo e controlador leve de áudio para desktop e Android. */
public final class AudioAssetCatalog {
    public enum MusicTrack {
        MENU("audio/music/menu_neon_loop.ogg", true, 0.42f),
        GAMEPLAY("audio/music/gameplay_neon_loop.ogg", true, 0.34f),
        MUTANT("audio/music/mutant_arena_loop.ogg", true, 0.38f),
        CAMPAIGN("audio/boss/campaign_boss_loop.ogg", true, 0.40f);

        private final String path;
        private final boolean looping;
        private final float volume;

        MusicTrack(String path, boolean looping, float volume) {
            this.path = path;
            this.looping = looping;
            this.volume = volume;
        }
    }

    public enum Cue {
        UI_TAP("audio/sfx/ui/ui_tap.wav", 0.42f),
        UI_CONFIRM("audio/sfx/ui/ui_confirm.wav", 0.48f),
        UI_BACK("audio/sfx/ui/ui_back.wav", 0.42f),
        UI_PAUSE("audio/sfx/ui/ui_pause.wav", 0.42f),
        UI_ERROR("audio/sfx/ui/error.wav", 0.45f),
        TRANSITION_WHOOSH("audio/sfx/transitions/transition_whoosh.wav", 0.44f),
        COUNTDOWN_BEEP("audio/sfx/ui/countdown_beep.wav", 0.42f),
        COUNTDOWN_GO("audio/sfx/ui/countdown_go.wav", 0.50f),
        PADDLE_HIT("audio/sfx/gameplay/paddle_hit.wav", 0.42f),
        WALL_BOUNCE("audio/sfx/gameplay/wall_bounce.wav", 0.34f),
        SCORE_POINT("audio/sfx/gameplay/score_point.wav", 0.50f),
        MATCH_WIN("audio/sfx/results/match_win.wav", 0.52f),
        MATCH_LOSS("audio/sfx/results/match_loss.wav", 0.48f),
        POWERUP_SPAWN("audio/sfx/powerups/powerup_spawn.wav", 0.40f),
        POWERUP_COLLECT("audio/sfx/powerups/powerup_collect.wav", 0.48f),
        POWERUP_ENERGY("audio/sfx/powerups/powerup_energy.wav", 0.48f),
        POWERUP_SLOW("audio/sfx/powerups/powerup_slow.wav", 0.44f),
        POWERUP_SPLIT("audio/sfx/powerups/powerup_split.wav", 0.46f),
        POWERUP_MULTI("audio/sfx/powerups/powerup_multi.wav", 0.50f),
        ABILITY_OVERDRIVE("audio/sfx/abilities/ability_overdrive.wav", 0.50f),
        ABILITY_SHIELD("audio/sfx/abilities/ability_shield.wav", 0.46f),
        ABILITY_WIDE("audio/sfx/abilities/ability_wide.wav", 0.46f),
        ABILITY_DENIED("audio/sfx/abilities/ability_denied.wav", 0.42f),
        BOSS_ALERT("audio/sfx/boss/boss_alert.wav", 0.52f),
        BOSS_PHASE("audio/sfx/boss/boss_phase.wav", 0.48f);

        private final String path;
        private final float volume;

        Cue(String path, float volume) {
            this.path = path;
            this.volume = volume;
        }
    }

    private final AssetManager manager;
    private Music currentMusic;
    private MusicTrack currentTrack;
    private boolean loaded;
    private boolean enabled = true;

    public AudioAssetCatalog() {
        this(new AssetManager());
    }

    public AudioAssetCatalog(AssetManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("manager obrigatorio");
        }
        this.manager = manager;
    }

    public void load() {
        try {
            for (MusicTrack track : MusicTrack.values()) {
                manager.load(track.path, Music.class);
            }
            for (Cue cue : Cue.values()) {
                manager.load(cue.path, Sound.class);
            }
            manager.finishLoading();
            loaded = true;
        } catch (RuntimeException exception) {
            loaded = false;
            manager.clear();
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            stopMusic();
        }
    }

    public void playMusic(MusicTrack track) {
        if (!canPlay() || track == null || track == currentTrack) {
            return;
        }
        stopMusic();
        if (!manager.isLoaded(track.path)) {
            return;
        }
        currentMusic = manager.get(track.path, Music.class);
        currentMusic.setLooping(track.looping);
        currentMusic.setVolume(track.volume);
        currentMusic.play();
        currentTrack = track;
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
        currentTrack = null;
    }

    public void play(Cue cue) {
        if (!canPlay() || cue == null || !manager.isLoaded(cue.path)) {
            return;
        }
        manager.get(cue.path, Sound.class).play(cue.volume);
    }

    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }

    public MusicTrack trackFor(MobileGameMode mode) {
        if (mode == MobileGameMode.MUTANT) {
            return MusicTrack.MUTANT;
        }
        if (mode == MobileGameMode.CAMPAIGN) {
            return MusicTrack.CAMPAIGN;
        }
        return MusicTrack.GAMEPLAY;
    }

    /** Mapeia eventos de gameplay existentes para cues sem acoplar a física ao áudio. */
    public void playEvent(String event) {
        if (!canPlay() || event == null || event.trim().isEmpty()) {
            return;
        }
        String value = event.toUpperCase();
        if (value.contains("POINT")) {
            play(Cue.SCORE_POINT);
        } else if (value.startsWith("POWER ")) {
            play(Cue.POWERUP_SPAWN);
        } else if (value.endsWith("ENERGY")) {
            play(Cue.POWERUP_ENERGY);
        } else if (value.endsWith("SLOW")) {
            play(Cue.POWERUP_SLOW);
        } else if (value.endsWith("SPLIT")) {
            play(Cue.POWERUP_SPLIT);
        } else if (value.endsWith("MULTI")) {
            play(Cue.POWERUP_MULTI);
        } else if (value.contains("ENERGY LOW")) {
            play(Cue.ABILITY_DENIED);
        } else if (value.contains("SHIELD BREAK")) {
            play(Cue.BOSS_PHASE);
        } else if (value.contains("SHIELD")) {
            play(Cue.ABILITY_SHIELD);
        } else if (value.contains("OVERDRIVE")) {
            play(Cue.ABILITY_OVERDRIVE);
        } else if (value.contains("WIDE")) {
            play(Cue.ABILITY_WIDE);
        } else if (value.contains("BOSS HIT")) {
            play(Cue.BOSS_PHASE);
        } else if (value.contains("WALL")) {
            play(Cue.WALL_BOUNCE);
        } else if (value.contains("HIT")) {
            play(Cue.PADDLE_HIT);
        } else if (value.contains("BOSS")) {
            play(Cue.BOSS_ALERT);
        }
    }

    public void dispose() {
        stopMusic();
        manager.dispose();
        loaded = false;
    }

    private boolean canPlay() {
        return enabled && loaded;
    }
}
