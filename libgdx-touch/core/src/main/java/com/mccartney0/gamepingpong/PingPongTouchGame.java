package com.mccartney0.gamepingpong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mccartney0.gamepingpong.input.PaddleTouchInput;
import com.mccartney0.gamepingpong.services.GameServices;
import com.mccartney0.gamepingpong.services.AchievementProgress;
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
    private PaddleTouchInput touchInput;
    private String currentLeaderboardId;
    private boolean finalScoreSubmissionRequested;
    private final AchievementProgress achievementProgress = new AchievementProgress();
    private String firstPointAchievementId;
    private String matchWinAchievementId;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(TouchPongWorld.WIDTH, TouchPongWorld.HEIGHT, camera);
        viewport.apply(true);
        camera.position.set(TouchPongWorld.WIDTH / 2f, TouchPongWorld.HEIGHT / 2f, 0f);
        camera.update();
        renderer = new ShapeRenderer();
        touchInput = new PaddleTouchInput(viewport, world);
        monetizationService.setBannerVisible(false);
        Gdx.input.setInputProcessor(touchInput);
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 20f);
        touchInput.update(delta);
        world.update(delta);
        processAchievements();
        submitCurrentMatchScoreIfFinished();

        Gdx.gl.glClearColor(0.005f, 0.01f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        camera.update();
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        world.render(renderer);
        renderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(TouchPongWorld.WIDTH / 2f, TouchPongWorld.HEIGHT / 2f, 0f);
        camera.update();
    }

    @Override
    public void pause() {
        world.setPaused(true);
    }

    @Override
    public void resume() {
        world.setPaused(false);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    public TouchPongWorld getWorld() {
        return world;
    }

    public PaddleTouchInput getTouchInput() {
        return touchInput;
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

    private void processAchievements() {
        if (world.playerScore > 0) {
            achievementProgress.onPlayerPoint(gameServices, firstPointAchievementId);
        }
        if (world.playerScore >= TouchPongWorld.MATCH_SCORE) {
            achievementProgress.onMatchWin(gameServices, matchWinAchievementId);
        }
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
                        // O adapter Android registra o resultado; o jogo permanece jogável offline.
                    }

                    @Override
                    public void onFailure(String message) {
                        // A partida já terminou; falha de rede não altera o resultado local.
                    }
                });
    }
}
