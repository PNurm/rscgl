package com.rscgl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.rscgl.render.MobRenderContext;

public class GameCamera extends PerspectiveCamera {

    public int forwardKey = Input.Keys.UP;
    protected boolean forwardPressed;
    public int backwardKey = Input.Keys.DOWN;
    protected boolean backwardPressed;
    public int rotateRightKey = Input.Keys.LEFT;
    protected boolean rotateRightPressed;
    public int rotateLeftKey = Input.Keys.RIGHT;
    protected boolean rotateLeftPressed;

    protected final Vector3 cameraTarget = new Vector3();
    protected float pitch = 15f, yaw, offset = 20f;
    public float cameraAngle = 0;

    public GameCamera() {
        super(Config.FIELD_OF_VIEW, Config.RESOLUTION_WIDTH, Config.RESOLUTION_HEIGHT);
        near = 1f;
        far = 200f;
        update();
    }

    public void setCameraTarget(Vector3 v) {
        cameraTarget.set(v);
        updatePosition();
    }

    public void updatePosition() {
        if (rotateLeftPressed) {
            cameraAngle += Gdx.graphics.getDeltaTime() * 100F;
        }
        if (rotateRightPressed) {
            cameraAngle -= Gdx.graphics.getDeltaTime() * 100F;
        }
        if (forwardPressed) {
            pitch += Gdx.graphics.getDeltaTime() * 50F;
        }
        if (backwardPressed) {
            pitch -= Gdx.graphics.getDeltaTime() * 50F;
        }
        pitch = MathUtils.clamp(pitch, 5f, 30f);

        //cameraAngle = 25f;
        //gameScreen.view.setTranslation(0,0, -offset);
        MobRenderContext renderContext = ((MobRenderContext) Game.inst.localPlayer.getRenderContext());
        if(renderContext != null ) {
            Vector3 player = renderContext.decal.getPosition();

            if (cameraTarget.x - player.x < -8
                    || cameraTarget.x - player.x > 8
                    || cameraTarget.z - player.z < -8
                    || cameraTarget.z - player.z > 8) {
                cameraTarget.set(player);
            }
            cameraTarget.lerp(player, Gdx.graphics.getDeltaTime() * 2);
        }
        Vector3 camPosition = position;
        camPosition.set(-offset, pitch, 0); //Move gameScreen to default location on circle centered at origin
        camPosition.rotate(Vector3.Y, cameraAngle); //Rotate the position to the angle you want. Rotating this vector about the Y axis is like walking along the circle in a counter-clockwise lightDirection.
        camPosition.add(cameraTarget); //translate the circle from origin to tree center
        up.set(Vector3.Y); //Make sure gameScreen is still upright, in case a previous calculation caused it to roll or pitch
        lookAt(cameraTarget);
        super.update();
    }

    public boolean keyDown(int keycode) {
        if (keycode == forwardKey)
            forwardPressed = true;
        else if (keycode == backwardKey)
            backwardPressed = true;
        else if (keycode == rotateRightKey)
            rotateRightPressed = true;
        else if (keycode == rotateLeftKey)
            rotateLeftPressed = true;
        return false;
    }

    public boolean keyUp(int keycode) {
        if (keycode == forwardKey)
            forwardPressed = false;
        else if (keycode == backwardKey)
            backwardPressed = false;
        else if (keycode == rotateRightKey)
            rotateRightPressed = false;
        else if (keycode == rotateLeftKey)
            rotateLeftPressed = false;
        return false;
    }

    private Vector3 ps = new Vector3();

    public boolean inFrustum(final ModelInstance instance) {
        instance.transform.getTranslation(ps);
        return inFrustum(ps);
    }

    public boolean inFrustum(final Vector3 position) {
        return frustum.pointInFrustum(position);
    }
}
