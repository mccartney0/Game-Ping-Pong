import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
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
            screenCoords.y = (getScreenHeight() - screenCoords.y + getScreenY()) * getWorldHeight() / getScreenHeight();
            return screenCoords;
        }

        @Override
        public Vector3 project(Vector3 worldCoords) {
            worldCoords.x = getScreenX() + worldCoords.x * getScreenWidth() / getWorldWidth();
            worldCoords.y = getScreenHeight() - worldCoords.y * getScreenHeight() / getWorldHeight() + getScreenY();
            return worldCoords;
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

    public static void main(String[] args) throws Exception {
        TouchPongWorld world = new TouchPongWorld();
        HeadlessViewport viewport = new HeadlessViewport();
        PaddleTouchInput input = new PaddleTouchInput(viewport, world);
        StringBuilder log = new StringBuilder();

        int bottomX = screenX(viewport, world.playerX, world.playerY);
        int bottomY = screenY(viewport, world.playerX, world.playerY);
        int topX = screenX(viewport, world.enemyX, world.enemyY);
        int topY = screenY(viewport, world.enemyX, world.enemyY);
        input.touchDown(bottomX, bottomY, 0, Input.Buttons.LEFT);
        input.touchDown(topX, topY, 1, Input.Buttons.LEFT);

        for (int frame = 0; frame < 900; frame++) {
            float time = frame / 60f;
            input.update(1f / 60f);

            if (frame == 20) {
                int x = screenX(viewport, world.playerX, world.playerY);
                int y = screenY(viewport, world.playerX, world.playerY);
                tap(input, x, y, 2);
                input.update(0.08f);
                tap(input, x, y, 2);
                log.append(String.format("t=%.2f double-tap bottom ability=%d%n", time, world.abilityActivations));
            }
            if (frame == 40) {
                int x = screenX(viewport, world.enemyX, world.enemyY);
                int y = screenY(viewport, world.enemyX, world.enemyY);
                tap(input, x, y, 3);
                input.update(0.08f);
                tap(input, x, y, 3);
                log.append(String.format("t=%.2f double-tap top ability=%d%n", time, world.abilityActivations));
            }

            // O jogador acompanha a bola; o topo erra deliberadamente em alguns ciclos
            // para validar o fluxo de pontuação do playthrough.
            float desiredBottom = world.ballX;
            float desiredTop = (frame / 180) % 2 == 0 ? world.ballX : 1.1f;
            input.touchDragged(screenX(viewport, desiredBottom, world.playerY),
                    screenY(viewport, desiredBottom, world.playerY), 0);
            input.touchDragged(screenX(viewport, desiredTop, world.enemyY),
                    screenY(viewport, desiredTop, world.enemyY), 1);

            world.update(1f / 60f);
            if (frame % 120 == 0) {
                log.append(String.format("t=%.2f score=%d:%d ball=(%.2f,%.2f) event=%s%n",
                        time, world.playerScore, world.enemyScore, world.ballX, world.ballY, world.getLastEvent()));
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
        check(Math.abs(world.ballX - beforePauseX) < 0.0001f && Math.abs(world.ballY - beforePauseY) < 0.0001f,
                "bola permanece parada na pausa");
        world.togglePause();
        check(!world.isPaused(), "retomada ativa");

        check(world.playerDragEvents > 100, "arraste do jogador processado");
        check(world.enemyDragEvents > 100, "arraste do topo processado");
        check(world.abilityActivations == 2, "toque duplo ativa as duas habilidades");
        check(world.playerScore + world.enemyScore > 0, "playthrough gerou pontuacao");
        check(!Float.isNaN(world.ballX) && !Float.isNaN(world.ballY), "bola permanece numericamente valida");

        File output = new File("libgdx-touch/playthrough.log");
        try (PrintWriter writer = new PrintWriter(new FileWriter(output))) {
            writer.print(log);
            writer.printf("final score=%d:%d drags=%d/%d doubleTapAbilities=%d pauseToggles=%d event=%s%n",
                    world.playerScore, world.enemyScore, world.playerDragEvents, world.enemyDragEvents,
                    world.abilityActivations, world.pauseToggles, world.getLastEvent());
        }

        // Artefato mínimo para deixar verificável que o playthrough terminou sem depender de uma janela GL.
        BufferedImage marker = new BufferedImage(320, 90, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(marker, "png", new File("libgdx-touch/playthrough-ok.png"));
        System.out.println("touch playthrough: OK");
        System.out.println("score=" + world.playerScore + ":" + world.enemyScore);
        System.out.println("abilityActivations=" + world.abilityActivations);
        System.out.println("pauseToggles=" + world.pauseToggles);
    }
}
