package com.mccartney0.gamepingpong;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
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

    public Texture getAbilityTexture(AbilityType ability) {
        return getTexture(regionFor("power_" + ability.name().toLowerCase()));
    }

    public Texture getPowerUpTexture(PowerUpType powerUp) {
        return getTexture(regionFor("power_" + powerUp.name().toLowerCase()));
    }

    public Texture getMenuButtonTexture() {
        return getTexture(regionFor("menu_button"));
    }

    public Texture getArenaGridTexture() {
        return getTexture(regionFor("arena_grid"));
    }

    public void dispose() {
        manager.dispose();
        loaded = false;
    }

    private Texture getTexture(TextureRegion region) {
        return region == null ? null : region.getTexture();
    }

    private TextureRegion regionFor(String name) {
        if (!loaded || atlas == null) {
            return null;
        }
        return atlas.findRegion(name);
    }
}
