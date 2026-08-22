package com.mccartney0.gamepingpong;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Carrega os assets exportados pelo trainer sem depender de Android. */
public final class NeonAssetCatalog {
    private static final String ATLAS = "generated/atlas/neon.atlas";

    private final AssetManager manager;
    private TextureAtlas atlas;
    private boolean loaded;

    public NeonAssetCatalog() {
        this(new AssetManager());
    }

    public NeonAssetCatalog(AssetManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("manager obrigatorio");
        }
        this.manager = manager;
    }

    public void load() {
        try {
            manager.load(ATLAS, TextureAtlas.class);
            manager.finishLoading();
            atlas = manager.get(ATLAS, TextureAtlas.class);
            loaded = atlas != null;
        } catch (RuntimeException exception) {
            loaded = false;
            atlas = null;
            manager.clear();
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public TextureRegion getAbilityRegion(AbilityType ability) {
        return regionFor("power_" + ability.name().toLowerCase());
    }

    public TextureRegion getPowerUpRegion(PowerUpType powerUp) {
        return regionFor("power_" + powerUp.name().toLowerCase());
    }

    public TextureRegion getMenuButtonRegion() {
        return regionFor("menu_button");
    }

    public TextureRegion getArenaGridRegion() {
        return regionFor("arena_grid");
    }

    public void dispose() {
        manager.dispose();
        loaded = false;
    }

    private TextureRegion regionFor(String name) {
        if (!loaded || atlas == null) {
            return null;
        }
        return atlas.findRegion(name);
    }
}
