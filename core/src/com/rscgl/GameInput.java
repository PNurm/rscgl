package com.rscgl;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;

public class GameInput extends GestureDetector {

    private static UI ui;
    private static GameWorld world;
    private static GameCamera camera;

    public GameInput(UI ui, GameCamera camera, GameWorld world) {
        super(new GameGestureHandler());
        this.camera = camera;
        this.ui = ui;
        this.world = world;
    }

    @Override
    public boolean keyTyped(char character) {
        ui.getStage().keyTyped(character);
        return super.keyTyped(character);
    }

    @Override
    public boolean scrolled(int amount) {
        ui.getStage().scrolled(amount);
        return super.scrolled(amount);
    }

    @Override
    public boolean keyDown(int keycode) {
        ui.getStage().keyDown(keycode);
        return camera.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        ui.getStage().keyUp(keycode);
        return camera.keyUp(keycode);
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (ui.getStage().touchDragged(x, y, pointer)) {
            return true;
        }
        return super.touchDragged(x, y, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        pick(screenX, screenY);
        if (ui.getStage().mouseMoved(screenX, screenY)) {
            return true;
        }
        return super.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (ui.getStage().touchDown(x, y, pointer, button)) {
            return true;
        }
        if (button == 0) {
            ui.getActionMenu().executeFirstAction();
        } else if (button == 1)
            ui.getActionMenu().showActionMenu((int) x, (int) y);

        return super.touchDown(x, y, pointer, button);
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (ui.getStage().touchUp(x, y, pointer, button)) {
            return true;
        }
        return super.touchUp(x, y, pointer, button);
    }

    @Override
    public boolean touchDragged(float x, float y, int pointer) {
        return mouseMoved((int) x, (int) y);
    }

    public static void pick(int x, int y) {
        ui.getActionMenu().checkBounds(x, y);

        Ray ray = camera.getPickRay(x, y);
        Game.inst.processPicking(ray);

        ui.refreshActionMenu();
    }

    private static class GameGestureHandler implements GestureListener {
        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            //pick((int) x, (int) y);

            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            ui.getActionMenu().showActionMenu((int) x, (int) y);
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {

            camera.cameraAngle += (deltaX) * 0.50F;
            camera.pitch += (deltaY) * 0.50F;
            return false;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            return false;
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return false;
        }

        @Override
        public void pinchStop() {

        }
    }
}
