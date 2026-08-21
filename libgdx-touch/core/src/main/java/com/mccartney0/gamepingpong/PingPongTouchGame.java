package com.mccartney0.gamepingpong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mccartney0.gamepingpong.input.PaddleSide;
import com.mccartney0.gamepingpong.input.PaddleTouchInput;
import com.mccartney0.gamepingpong.services.AchievementProgress;
import com.mccartney0.gamepingpong.services.GameServices;
import com.mccartney0.gamepingpong.services.GameServicesCallback;
import com.mccartney0.gamepingpong.services.MonetizationService;
import com.mccartney0.gamepingpong.services.NoopGameServices;
import com.mccartney0.gamepingpong.services.NoopMonetizationService;
import com.mccartney0.gamepingpong.services.RewardCallback;

public class PingPongTouchGame extends ApplicationAdapter {
    private final TouchPongWorld world;
    private final GameServices gameServices;
    private final MonetizationService monetizationService;

    public PingPongTouchGame() {
        this(new NoopGameServices(), new NoopMonetizationService());
    }

    public PingPongTouchGame(GameServices gameServices) {
        this(gameServices, new NoopMonetizationService());
    }

    public PingPongTouchGame(GameServices gameServices,
            MonetizationService monetizationService) {
        this.gameServices = gameServices == null ? new NoopGameServices() : gameServices;
        this.monetizationService = monetizationService == null
                ? new NoopMonetizationService() : monetizationService;
        this.world = new TouchPongWorld();
    }

    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer renderer;
    private SpriteBatch batch;
    private ShaderProgram neonShader;
    private NeonAssetCatalog neonAssets;
    private BitmapFont font;
    private GlyphLayout layout;
    private PaddleTouchInput touchInput;
    private MobileMenu menu;
    private final MobileTransition transition = new MobileTransition();
    private MobileGameMode currentMode = MobileGameMode.CLASSIC;
    private String currentLeaderboardId;
    private boolean gameStarted;
    private boolean finalScoreSubmissionRequested;
    private final AchievementProgress achievementProgress = new AchievementProgress();
    private String firstPointAchievementId;
    private String matchWinAchievementId;
    private BallEffectsQuality effectsQuality = BallEffectsQuality.MEDIUM;
    private float visualTime;

    private enum BallEffectsQuality {
        LOW, MEDIUM, HIGH
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(TouchPongWorld.WIDTH, TouchPongWorld.HEIGHT, camera);
        viewport.apply(true);
        camera.position.set(TouchPongWorld.WIDTH / 2f, TouchPongWorld.HEIGHT / 2f, 0f);
        camera.update();
        renderer = new ShapeRenderer();
        batch = new SpriteBatch();
        neonShader = loadNeonShader();
        neonAssets = new NeonAssetCatalog();
        neonAssets.load();
        font = new BitmapFont();
        font.getData().setScale(0.52f);
        layout = new GlyphLayout();
        touchInput = new PaddleTouchInput(viewport, world);
        menu = new MobileMenu(new MenuListener());
        menu.showMain();
        menu.setScreenSize(Gdx.graphics.getHeight());
        monetizationService.setBannerVisible(false);
        Gdx.input.setInputProcessor(new GameInputRouter());
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 20f);
        visualTime += delta;
        updateTransition(delta);
        boolean playing = gameStarted && !transition.isActive()
                && !world.isPaused() && !world.isMatchOver();
        boolean overlayVisible = !playing;
        if (playing) {
            touchInput.update(delta);
            world.update(delta);
            processAchievements();
            submitCurrentMatchScoreIfFinished();
            if (world.isMatchOver() && menu.getPage() != MobileMenu.Page.RESULTS) {
                menu.showResults();
                transition.begin(MobileTransition.Type.SHOW_RESULTS, 0.8f);
            }
        }

        Gdx.gl.glClearColor(0.005f, 0.01f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        camera.update();
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        world.render(renderer);
        if (overlayVisible) {
            renderOverlayBackground();
        }
        renderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.setShader(neonShader);
        batch.begin();
        configureNeonShader();
        renderPowerUpTexture();
        renderHud();
        if (overlayVisible) {
            if (transition.isActive()) {
                renderTransitionText();
            } else {
                renderMenuText();
            }
        }
        batch.end();
        batch.setShader(null);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(TouchPongWorld.WIDTH / 2f, TouchPongWorld.HEIGHT / 2f, 0f);
        camera.update();
        if (menu != null) {
            menu.setScreenSize(height);
        }
    }

    @Override
    public void pause() {
        if (gameStarted && !world.isMatchOver()) {
            world.setPaused(true);
            menu.showPause();
        }
    }

    @Override
    public void resume() {
        if (gameStarted && !world.isMatchOver()) {
            world.setPaused(false);
        }
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (neonAssets != null) {
            neonAssets.dispose();
        }
        if (neonShader != null) {
            neonShader.dispose();
        }
    }

    public TouchPongWorld getWorld() {
        return world;
    }

    public PaddleTouchInput getTouchInput() {
        return touchInput;
    }

    public MobileMenu getMenu() {
        return menu;
    }

    public MobileTransition getTransition() {
        return transition;
    }

    public GameServices getGameServices() {
        return gameServices;
    }

    public MonetizationService getMonetizationService() {
        return monetizationService;
    }

    public boolean isRewardedReady() {
        return monetizationService.isRewardedReady();
    }

    public void showRewarded(String placement, RewardCallback callback) {
        monetizationService.showRewarded(placement, callback);
    }

    public void setBannerVisible(boolean visible) {
        monetizationService.setBannerVisible(visible);
    }

    public void signIn(GameServicesCallback callback) {
        gameServices.signIn(callback);
    }

    public void submitScore(String leaderboardId, long score, GameServicesCallback callback) {
        gameServices.submitScore(leaderboardId, score, callback);
    }

    public void showLeaderboard(String leaderboardId) {
        gameServices.showLeaderboard(leaderboardId);
    }

    public void showAllLeaderboards() {
        gameServices.showAllLeaderboards();
    }

    public void showAchievements() {
        gameServices.showAchievements();
    }

    public void setAchievementIds(String firstPointAchievementId,
            String matchWinAchievementId) {
        this.firstPointAchievementId = firstPointAchievementId;
        this.matchWinAchievementId = matchWinAchievementId;
    }

    public AchievementProgress getAchievementProgress() {
        return achievementProgress;
    }

    public void setCurrentLeaderboardId(String leaderboardId) {
        this.currentLeaderboardId = leaderboardId;
        this.finalScoreSubmissionRequested = false;
        monetizationService.setBannerVisible(false);
    }

    public void submitCurrentMatchScoreIfFinished() {
        if (!world.isMatchOver() || finalScoreSubmissionRequested
                || currentLeaderboardId == null || currentLeaderboardId.trim().isEmpty()) {
            return;
        }
        finalScoreSubmissionRequested = true;
        monetizationService.setBannerVisible(true);
        gameServices.submitScore(currentLeaderboardId, world.getLeaderboardScore(),
                new GameServicesCallback() {
                    @Override
                    public void onSuccess(String message) {
                        // A partida continua disponível offline mesmo após o envio.
                    }

                    @Override
                    public void onFailure(String message) {
                        // O placar local permanece válido sem rede.
                    }
                });
    }

    private void processAchievements() {
        if (world.playerScore > 0) {
            achievementProgress.onPlayerPoint(gameServices, firstPointAchievementId);
        }
        if (world.playerScore >= TouchPongWorld.MATCH_SCORE) {
            achievementProgress.onMatchWin(gameServices, matchWinAchievementId);
        }
    }

    private void renderPowerUpTexture() {
        if (neonAssets == null || !neonAssets.isLoaded()) {
            return;
        }
        PowerUp powerUp = world.getActivePowerUp();
        if (powerUp == null || !powerUp.isActive()) {
            return;
        }
        Texture texture = neonAssets.getPowerUpTexture(powerUp.getType());
        if (texture == null) {
            return;
        }
        float size = 0.86f;
        batch.draw(texture, powerUp.getX() - size / 2f, powerUp.getY() - size / 2f, size, size);
    }

    private ShaderProgram loadNeonShader() {
        ShaderProgram.pedantic = false;
        try {
            String vertex = Gdx.files.internal("shaders/neon_glow_scanlines.vert").readString();
            String fragment = Gdx.files.internal("shaders/neon_glow_scanlines.frag").readString();
            ShaderProgram shader = new ShaderProgram(vertex, fragment);
            if (!shader.isCompiled()) {
                if (Gdx.app != null) {
                    Gdx.app.error("NeonShader", shader.getLog());
                }
                shader.dispose();
                return null;
            }
            return shader;
        } catch (RuntimeException exception) {
            if (Gdx.app != null) {
                Gdx.app.error("NeonShader", "Shader indisponivel; usando render padrao", exception);
            }
            return null;
        }
    }

    private void configureNeonShader() {
        if (neonShader == null) {
            return;
        }
        float glow = effectsQuality == BallEffectsQuality.LOW ? 0.55f
                : effectsQuality == BallEffectsQuality.HIGH ? 1.35f : 0.95f;
        float scanlines = effectsQuality == BallEffectsQuality.LOW ? 0f
                : effectsQuality == BallEffectsQuality.HIGH ? 0.65f : 0.35f;
        neonShader.setUniformf("u_time", visualTime);
        neonShader.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        neonShader.setUniformf("u_texelSize", 1f / 256f, 1f / 256f);
        neonShader.setUniformf("u_glowStrength", glow);
        neonShader.setUniformf("u_glowRadius", effectsQuality == BallEffectsQuality.HIGH ? 3f : 2f);
        neonShader.setUniformf("u_scanlineStrength", scanlines);
        neonShader.setUniformf("u_tint", 0.25f, 0.85f, 1f, 1f);
    }

    private void updateTransition(float delta) {
        if (!transition.isActive()) {
            return;
        }
        transition.update(delta);
        if (transition.isActive()) {
            return;
        }
        if (transition.getType() == MobileTransition.Type.START_MATCH) {
            world.setPaused(false);
        } else if (transition.getType() == MobileTransition.Type.RETURN_TO_MENU) {
            gameStarted = false;
            world.setPaused(true);
        }
    }

    private void renderTransitionText() {
        MobileTransition.Type type = transition.getType();
        String title = type == MobileTransition.Type.SHOW_RESULTS
                ? "MATCH COMPLETE" : type == MobileTransition.Type.RETURN_TO_MENU
                        ? "RETURNING TO MENU" : currentMode.getLabel();
        font.setColor(new Color(0.42f, 0.92f, 1f, 1f));
        drawText(title, TouchPongWorld.WIDTH / 2f, 8.6f, true);
        font.setColor(new Color(1f, 0.84f, 0.35f, 1f));
        drawText(type == MobileTransition.Type.START_MATCH
                ? transition.getCountdownText() : "...", TouchPongWorld.WIDTH / 2f, 6.5f, true);
        font.setColor(new Color(0.75f, 0.86f, 0.95f, 1f));
        drawText(type == MobileTransition.Type.START_MATCH
                ? currentMode.getDescription() : "Aguarde a proxima tela", TouchPongWorld.WIDTH / 2f, 4.8f, true);
    }

    private void renderOverlayBackground() {
        renderer.setColor(0.015f, 0.025f, 0.08f, 0.86f);
        renderer.rect(0f, 0f, TouchPongWorld.WIDTH, TouchPongWorld.HEIGHT);
        renderer.setColor(0.08f, 0.22f, 0.36f, 0.8f);
        renderer.rect(0.45f, 0.35f, TouchPongWorld.WIDTH - 0.9f, TouchPongWorld.HEIGHT - 0.7f);
    }

    private void renderHud() {
        if (!gameStarted || transition.isActive()) {
            return;
        }
        font.setColor(Color.WHITE);
        drawText("" + world.playerScore + "  :  " + world.enemyScore,
                TouchPongWorld.WIDTH / 2f, TouchPongWorld.HEIGHT - 0.42f, true);
        drawText(world.getModeLabel() + "  |  " + world.getProgressLabel(),
                0.7f, TouchPongWorld.HEIGHT - 0.42f, false);
        drawText("P1 ENERGY " + Math.round(world.getEnergy(PaddleSide.BOTTOM))
                + "  " + world.getAbilityLabel(PaddleSide.BOTTOM),
                0.7f, 0.33f, false);
        drawText("P2 ENERGY " + Math.round(world.getEnergy(PaddleSide.TOP))
                + "  " + world.getAbilityLabel(PaddleSide.TOP),
                TouchPongWorld.WIDTH - 0.7f, 0.33f, true);
        drawText("DOUBLE TAP = POWER  |  EDGE TAP = CYCLE  |  CENTER = COLLECT",
                TouchPongWorld.WIDTH / 2f, 0.82f, true);
        if (!"READY".equals(world.getLastEvent())) {
            font.setColor(new Color(1f, 0.84f, 0.35f, 1f));
            drawText(world.getLastEvent(), TouchPongWorld.WIDTH / 2f,
                    TouchPongWorld.HEIGHT - 1.0f, true);
        }
    }

    private void renderMenuText() {
        if (menu == null) {
            return;
        }
        font.setColor(new Color(0.42f, 0.92f, 1f, 1f));
        drawText(menu.getPage() == MobileMenu.Page.MAIN ? "NEON PING PONG" : menu.getPage().name(),
                TouchPongWorld.WIDTH / 2f, TouchPongWorld.HEIGHT - 1.0f, true);
        if (menu.getPage() == MobileMenu.Page.HELP) {
            drawText("ARRASTE cada metade para mover uma raquete", 1.0f, 9.8f, false);
            drawText("TOQUE DUPLO ativa o poder selecionado", 1.0f, 8.8f, false);
            drawText("COLETE power-ups coloridos no centro", 1.0f, 7.8f, false);
            drawText("PAUSE pelo canto superior esquerdo", 1.0f, 6.8f, false);
            drawText("VOLTAR", TouchPongWorld.WIDTH / 2f, 2.0f, true);
            return;
        }
        for (int index = 0; index < menu.getItemCount(); index++) {
            boolean selected = index == menu.getSelection();
            font.setColor(selected ? new Color(1f, 0.84f, 0.35f, 1f)
                    : new Color(0.75f, 0.86f, 0.95f, 1f));
            float y = TouchPongWorld.HEIGHT - 2.0f - index * 0.85f;
            drawText((selected ? "> " : "  ") + menu.getItemLabel(index),
                    TouchPongWorld.WIDTH / 2f, y, true);
        }
        if (menu.getPage() == MobileMenu.Page.MODES) {
            MobileGameMode selected = menu.getSelectedMode();
            font.setColor(new Color(0.65f, 0.9f, 1f, 1f));
            drawText(selected.getDescription(), TouchPongWorld.WIDTH / 2f, 1.25f, true);
        }
    }

    private void drawText(String text, float x, float y, boolean centered) {
        layout.setText(font, text);
        float drawX = centered ? x - layout.width / 2f : x;
        font.draw(batch, text, drawX, y);
    }

    private final class MenuListener implements MobileMenu.Listener {
        @Override
        public void onStartMode(MobileGameMode mode) {
            currentMode = mode;
            world.setMode(mode);
            gameStarted = true;
            world.setPaused(true);
            finalScoreSubmissionRequested = false;
            monetizationService.setBannerVisible(false);
            transition.begin(MobileTransition.Type.START_MATCH, 1.6f);
        }

        @Override
        public void onResumeGame() {
            gameStarted = true;
            world.setPaused(false);
        }

        @Override
        public void onOpenMainMenu() {
            world.resetMatch();
            world.setPaused(true);
            gameStarted = false;
            monetizationService.setBannerVisible(false);
            transition.begin(MobileTransition.Type.RETURN_TO_MENU, 0.55f);
        }

        @Override
        public void onOpenHelp() {
            // A página é controlada pelo MobileMenu.
        }

        @Override
        public void onOpenSettings() {
            // A página é controlada pelo MobileMenu.
        }

        @Override
        public void onShowLeaderboards() {
            gameServices.showAllLeaderboards();
        }

        @Override
        public void onShowAchievements() {
            gameServices.showAchievements();
        }

        @Override
        public void onShowRewarded() {
            monetizationService.showRewarded("mobile-menu-energy", new RewardCallback() {
                @Override
                public void onRewardEarned(String placement, int amount) {
                    world.grantRewardEnergy(amount);
                }

                @Override
                public void onAdUnavailable(String placement, String reason) {
                    // O menu permanece funcional offline.
                }
            });
        }

        @Override
        public void onCycleEffectsQuality() {
            switch (effectsQuality) {
            case LOW:
                effectsQuality = BallEffectsQuality.MEDIUM;
                world.setEffectsQuality(com.mccartney0.gamepingpong.visual.BallEffects.Quality.MEDIUM);
                break;
            case MEDIUM:
                effectsQuality = BallEffectsQuality.HIGH;
                world.setEffectsQuality(com.mccartney0.gamepingpong.visual.BallEffects.Quality.HIGH);
                break;
            default:
                effectsQuality = BallEffectsQuality.LOW;
                world.setEffectsQuality(com.mccartney0.gamepingpong.visual.BallEffects.Quality.LOW);
                break;
            }
        }
    }

    private final class GameInputRouter extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (isOverlayVisible()) {
                return true;
            }
            return touchInput.touchDown(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (isOverlayVisible()) {
                return false;
            }
            return touchInput.touchDragged(screenX, screenY, pointer);
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (transition.isActive()) {
                return true;
            }
            if (isOverlayVisible()) {
                return menu.touchUp(screenX, screenY, pointer, button);
            }
            if (screenX < 150 && screenY < 150) {
                world.togglePause();
                menu.showPause();
                return true;
            }
            return touchInput.touchUp(screenX, screenY, pointer, button);
        }

        @Override
        public boolean keyDown(int keycode) {
            if (isOverlayVisible()) {
                return menu.keyDown(keycode);
            }
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE
                    || keycode == Input.Keys.P) {
                world.togglePause();
                menu.showPause();
                return true;
            }
            if (keycode == Input.Keys.SPACE) {
                world.activateAbility(PaddleSide.BOTTOM);
                return true;
            }
            if (keycode == Input.Keys.Q) {
                world.cycleAbility(PaddleSide.BOTTOM);
                return true;
            }
            return false;
        }

        private boolean isOverlayVisible() {
            return transition.isActive() || !gameStarted || world.isPaused() || world.isMatchOver();
        }
    }
}
