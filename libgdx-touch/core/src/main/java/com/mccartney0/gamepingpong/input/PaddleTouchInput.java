package com.mccartney0.gamepingpong.input;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PaddleTouchInput extends InputAdapter {

    private static final class PointerState {
        private final PaddleSide side;
        private final float startX;
        private final float startY;
        private float lastX;
        private float lastY;
        private boolean dragging;

        private PointerState(PaddleSide side, float startX, float startY) {
            this.side = side;
            this.startX = startX;
            this.startY = startY;
            this.lastX = startX;
            this.lastY = startY;
        }
    }

    private final Viewport viewport;
    private final PaddleTouchTarget target;
    private final TouchGestureConfig config;
    private final Vector3 worldPoint = new Vector3();
    private final Map<Integer, PointerState> pointers = new HashMap<Integer, PointerState>();
    private final Map<PaddleSide, Float> lastTapTime = new HashMap<PaddleSide, Float>();
    private final Map<PaddleSide, Vector3> lastTapPoint = new HashMap<PaddleSide, Vector3>();
    private float elapsedSeconds;

    public PaddleTouchInput(Viewport viewport, PaddleTouchTarget target) {
        this(viewport, target, new TouchGestureConfig());
    }

    public PaddleTouchInput(Viewport viewport, PaddleTouchTarget target, TouchGestureConfig config) {
        if (viewport == null || target == null || config == null) {
            throw new IllegalArgumentException("viewport, target e config sao obrigatorios");
        }
        this.viewport = viewport;
        this.target = target;
        this.config = config;
    }

    public void update(float deltaSeconds) {
        elapsedSeconds += Math.max(0f, deltaSeconds);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || pointer < 0 || pointer >= 4) {
            return false;
        }

        PaddleSide side = sideForScreenPoint(screenX, screenY);
        if (side == PaddleSide.TOP && !config.allowTopPlayer) {
            return false;
        }
        Vector3 point = unproject(screenX, screenY);
        PointerState state = new PointerState(side, point.x, point.y);
        pointers.put(pointer, state);

        if (isDoubleTap(side, point.x, point.y)) {
            target.activateAbility(side);
            lastTapTime.remove(side);
            lastTapPoint.remove(side);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        PointerState state = pointers.get(pointer);
        if (state == null) {
            return false;
        }
        Vector3 point = unproject(screenX, screenY);
        state.lastX = point.x;
        state.lastY = point.y;
        float dx = point.x - state.startX;
        float dy = point.y - state.startY;
        if (!state.dragging && Math.sqrt(dx * dx + dy * dy) >= config.dragStartSlopWorld) {
            state.dragging = true;
        }
        if (state.dragging) {
            target.movePaddleTo(state.side, point.x);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        PointerState state = pointers.remove(pointer);
        if (state == null || button != Input.Buttons.LEFT) {
            return false;
        }
        Vector3 point = unproject(screenX, screenY);
        if (!state.dragging) {
            lastTapTime.put(state.side, elapsedSeconds);
            lastTapPoint.put(state.side, new Vector3(point.x, point.y, 0f));
        }
        return true;
    }

    public boolean isDragging(PaddleSide side) {
        for (PointerState state : pointers.values()) {
            if (state.side == side && state.dragging) {
                return true;
            }
        }
        return false;
    }

    public void cancelAll() {
        pointers.clear();
        lastTapTime.clear();
        lastTapPoint.clear();
    }

    private boolean isDoubleTap(PaddleSide side, float x, float y) {
        Float previousTime = lastTapTime.get(side);
        Vector3 previousPoint = lastTapPoint.get(side);
        if (previousTime == null || previousPoint == null) {
            return false;
        }
        float dx = x - previousPoint.x;
        float dy = y - previousPoint.y;
        return elapsedSeconds - previousTime <= config.doubleTapWindowSeconds
                && dx * dx + dy * dy <= config.doubleTapSlopWorld * config.doubleTapSlopWorld;
    }

    private PaddleSide sideForScreenPoint(int screenX, int screenY) {
        Vector3 point = unproject(screenX, screenY);
        return point.y < config.topHalfThresholdWorld ? PaddleSide.BOTTOM : PaddleSide.TOP;
    }

    private Vector3 unproject(int screenX, int screenY) {
        return viewport.unproject(worldPoint.set(screenX, screenY, 0f));
    }
}
