import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mccartney0.gamepingpong.AbilityType;
import com.mccartney0.gamepingpong.MobileGameMode;
import com.mccartney0.gamepingpong.MobileMenu;
import com.mccartney0.gamepingpong.PowerUpType;
import com.mccartney0.gamepingpong.TouchPongWorld;
import com.mccartney0.gamepingpong.input.PaddleSide;
import com.mccartney0.gamepingpong.input.PaddleTouchInput;

public class TouchPlaythrough {

    private static final int SCREEN_WIDTH = 1000;
    private static final int SCREEN_HEIGHT = 600;

    private static final class HeadlessViewport extends Viewport {
        private HeadlessViewport() {
            setWorldSize(TouchPongWorld.WIDTH, TouchPongWorld.HEIGHT);
            setScreenBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        @Override
        public void update(int width, int height, boolean centerCamera) {
            setScreenBounds(0, 0, width, height);
        }

        @Override
        public Vector3 unproject(Vector3 screenCoords) {
            screenCoords.x = (screenCoords.x - getScreenX()) * getWorldWidth() / getScreenWidth();
            screenCoords.y = (getScreenHeight() - screenCoords.y + getScreenY())
                    * getWorldHeight() / getScreenHeight();
            return screenCoords;
        }

        @Override
        public Vector3 project(Vector3 worldCoords) {
            worldCoords.x = getScreenX() + worldCoords.x * getScreenWidth() / getWorldWidth();
            worldCoords.y = getScreenHeight() - worldCoords.y * getScreenHeight() / getWorldHeight()
                    + getScreenY();
            return worldCoords;
        }
    }

    private static final class MenuProbe implements MobileMenu.Listener {
        private final TouchPongWorld world;
        private int starts;
        private int resumes;
        private int qualityChanges;
        private MobileMenu.Page lastPage;

        private MenuProbe(TouchPongWorld world) {
            this.world = world;
        }

        @Override
        public void onStartMode(MobileGameMode mode) {
            starts++;
            world.setMode(mode);
        }

        @Override
        public void onResumeGame() {
            resumes++;
            world.setPaused(false);
        }

        @Override
        public void onOpenMainMenu() {
            lastPage = MobileMenu.Page.MAIN;
        }

        @Override
        public void onOpenHelp() {
            lastPage = MobileMenu.Page.HELP;
        }

        @Override
        public void onOpenSettings() {
            lastPage = MobileMenu.Page.SETTINGS;
        }

        @Override
        public void onShowLeaderboards() {
            lastPage = MobileMenu.Page.MAIN;
        }

        @Override
        public void onShowAchievements() {
            lastPage = MobileMenu.Page.MAIN;
        }

        @Override
        public void onShowRewarded() {
            world.grantRewardEnergy(20);
        }

        @Override
        public void onCycleEffectsQuality() {
            qualityChanges++;
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static int screenX(Viewport viewport, float worldX, float worldY) {
        Vector3 projected = viewport.project(new Vector3(worldX, worldY, 0f));
        return Math.round(projected.x);
    }

    private static int screenY(Viewport viewport, float worldX, float worldY) {
        Vector3 projected = viewport.project(new Vector3(worldX, worldY, 0f));
        return Math.round(projected.y);
    }

    private static void tap(PaddleTouchInput input, int x, int y, int pointer) {
        input.touchDown(x, y, pointer, Input.Buttons.LEFT);
        input.touchUp(x, y, pointer, Input.Buttons.LEFT);
    }

    private static void exerciseMenu(TouchPongWorld world, StringBuilder log) {
        MenuProbe probe = new MenuProbe(world);
        MobileMenu menu = new MobileMenu(probe);
        menu.showMain();
        check(menu.getItemCount() == 7, "menu principal completo");
        menu.tapRow(1);
        check(menu.getPage() == MobileMenu.Page.MODES, "pagina de modos");
        menu.tapRow(MobileGameMode.MUTANT.ordinal());
        check(probe.starts == 1 && world.getMode() == MobileGameMode.MUTANT, "modo mutant selecionado");
        menu.showMain();
        menu.tapRow(2);
        check(menu.getPage() == MobileMenu.Page.HELP, "pagina de ajuda");
        menu.tapRow(0);
        check(menu.getPage() == MobileMenu.Page.MAIN, "volta da ajuda");
        menu.showSettings();
        menu.tapRow(0);
        check(probe.qualityChanges == 1, "qualidade alterada");
        menu.showPause();
        menu.tapRow(0);
        check(probe.resumes == 1, "pausa retomada");
        log.append("menu main/modes/help/settings/pause=OK\n");
    }

    private static void exerciseCampaignAndSurvival(StringBuilder log) {
        TouchPongWorld survival = new TouchPongWorld();
        survival.setMode(MobileGameMode.SURVIVAL);
        survival.ballY = -1f;
        survival.ballDy = -1f;
        survival.update(0.01f);
        check(survival.getSurvivalLives() == 2, "survival perde uma vida");
        log.append("survival lives=" + survival.getSurvivalLives() + "\n");

        TouchPongWorld campaign = new TouchPongWorld();
        campaign.setMode(MobileGameMode.CAMPAIGN);
        for (int point = 0; point < 12; point++) {
            campaign.ballY = TouchPongWorld.HEIGHT + 1f;
            campaign.ballDy = 1f;
            campaign.update(0.01f);
        }
        check(campaign.getBossIndex() == 4, "campanha derrota quatro bosses");
        check(campaign.isMatchOver(), "campanha concluida");
        log.append("campaign bosses=4 complete=true\n");
    }

    public static void main(String[] args) throws Exception {
        TouchPongWorld world = new TouchPongWorld();
        HeadlessViewport viewport = new HeadlessViewport();
        PaddleTouchInput input = new PaddleTouchInput(viewport, world);
        StringBuilder log = new StringBuilder();

        exerciseMenu(world, log);
        exerciseCampaignAndSurvival(log);
        world.setMode(MobileGameMode.MUTANT);
        world.spawnPowerUpAt(world.playerX, world.playerY, PowerUpType.SPLIT);
        world.update(0.01f);
        check(world.powerUpsCollected == 1, "power-up split coletado");
        check(world.getSplitTicks() > 0f, "split ativo");

        world.activateAbility(PaddleSide.BOTTOM);
        check(world.getAbility(PaddleSide.BOTTOM) == AbilityType.OVERDRIVE,
                "overdrive selecionado inicialmente");
        check(world.isOverdriveActive(PaddleSide.BOTTOM), "overdrive ativo");
        world.cycleAbility(PaddleSide.BOTTOM);
        world.activateAbility(PaddleSide.BOTTOM);
        check(world.isShieldActive(PaddleSide.BOTTOM), "shield ativo");
        world.grantRewardEnergy(100);
        world.cycleAbility(PaddleSide.BOTTOM);
        world.activateAbility(PaddleSide.BOTTOM);
        check(world.isWideActive(PaddleSide.BOTTOM), "wide ativo");
        log.append("powers overdrive/shield/wide=OK\n");

        world.setMode(MobileGameMode.CLASSIC);
        world.spawnPowerUpAt(world.playerX, world.playerY, PowerUpType.ENERGY);
        world.update(0.01f);
        check(world.powerUpsCollected == 1, "power-up energy coletado na partida");
        log.append("classic energy power-up collected=1\n");
        int bottomX = screenX(viewport, world.playerX, world.playerY);
        int bottomY = screenY(viewport, world.playerX, world.playerY);
        int topX = screenX(viewport, world.enemyX, world.enemyY);
        int topY = screenY(viewport, world.enemyX, world.enemyY);
        input.touchDown(bottomX, bottomY, 0, Input.Buttons.LEFT);
        input.touchDown(topX, topY, 1, Input.Buttons.LEFT);
        int edgeX = screenX(viewport, 0.5f, world.playerY);
        int edgeY = screenY(viewport, 0.5f, world.playerY);
        input.touchDown(edgeX, edgeY, 2, Input.Buttons.LEFT);
        input.touchUp(edgeX, edgeY, 2, Input.Buttons.LEFT);
        check(world.getAbility(PaddleSide.BOTTOM) == AbilityType.SHIELD,
                "borda troca poder bottom");
        log.append("edge ability cycle=SHIELD\n");

        for (int frame = 0; frame < 900; frame++) {
            float time = frame / 60f;
            input.update(1f / 60f);

            if (frame == 20) {
                int x = screenX(viewport, world.playerX, world.playerY);
                int y = screenY(viewport, world.playerX, world.playerY);
                tap(input, x, y, 2);
                input.update(0.08f);
                tap(input, x, y, 2);
                log.append(String.format("t=%.2f double-tap bottom ability=%d%n",
                        time, world.abilityActivations));
            }
            if (frame == 40) {
                int x = screenX(viewport, world.enemyX, world.enemyY);
                int y = screenY(viewport, world.enemyX, world.enemyY);
                tap(input, x, y, 3);
                input.update(0.08f);
                tap(input, x, y, 3);
                log.append(String.format("t=%.2f double-tap top ability=%d%n",
                        time, world.abilityActivations));
            }

            float desiredBottom = world.ballX;
            float desiredTop = (frame / 180) % 2 == 0 ? world.ballX : 1.1f;
            input.touchDragged(screenX(viewport, desiredBottom, world.playerY),
                    screenY(viewport, desiredBottom, world.playerY), 0);
            input.touchDragged(screenX(viewport, desiredTop, world.enemyY),
                    screenY(viewport, desiredTop, world.enemyY), 1);
            world.update(1f / 60f);
            if (frame % 120 == 0) {
                log.append(String.format("t=%.2f score=%d:%d ball=(%.2f,%.2f) event=%s%n",
                        time, world.playerScore, world.enemyScore, world.ballX, world.ballY,
                        world.getLastEvent()));
            }
        }

        input.touchUp(screenX(viewport, world.playerX, world.playerY),
                screenY(viewport, world.playerX, world.playerY), 0, Input.Buttons.LEFT);
        input.touchUp(screenX(viewport, world.enemyX, world.enemyY),
                screenY(viewport, world.enemyX, world.enemyY), 1, Input.Buttons.LEFT);

        float beforePauseX = world.ballX;
        float beforePauseY = world.ballY;
        world.togglePause();
        world.update(1f);
        check(world.isPaused(), "pause ativa");
        check(Math.abs(world.ballX - beforePauseX) < 0.0001f
                && Math.abs(world.ballY - beforePauseY) < 0.0001f,
                "bola permanece parada na pausa");
        world.togglePause();
        check(!world.isPaused(), "retomada ativa");

        check(world.playerDragEvents > 100, "arraste do jogador processado");
        check(world.enemyDragEvents > 100, "arraste do topo processado");
        check(world.abilityActivations >= 2, "toques duplos ativam habilidades");
        check(world.getBallEffects().getActiveTrailCount() > 0, "trilha da bola emitida");
        check(world.getBallEffects().getEmittedParticles() > 0, "particulas da bola emitidas");
        check(world.playerScore + world.enemyScore > 0, "playthrough gerou pontuacao");
        check(!Float.isNaN(world.ballX) && !Float.isNaN(world.ballY),
                "bola permanece numericamente valida");

        File output = new File("libgdx-touch/playthrough.log");
        try (PrintWriter writer = new PrintWriter(new FileWriter(output))) {
            writer.print(log);
            writer.printf("final score=%d:%d drags=%d/%d doubleTapAbilities=%d pauseToggles=%d powerUps=%d/%d event=%s%n",
                    world.playerScore, world.enemyScore, world.playerDragEvents, world.enemyDragEvents,
                    world.abilityActivations, world.pauseToggles, world.powerUpsSpawned,
                    world.powerUpsCollected, world.getLastEvent());
        }

        System.out.println("touch playthrough: OK");
        System.out.println("score=" + world.playerScore + ":" + world.enemyScore);
        System.out.println("abilityActivations=" + world.abilityActivations);
        System.out.println("powerUps=" + world.powerUpsSpawned + "/" + world.powerUpsCollected);
        System.out.println("pauseToggles=" + world.pauseToggles);
        System.out.println("activeTrail=" + world.getBallEffects().getActiveTrailCount());
        System.out.println("emittedParticles=" + world.getBallEffects().getEmittedParticles());
    }
}
