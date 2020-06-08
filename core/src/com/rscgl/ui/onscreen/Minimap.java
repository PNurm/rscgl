package com.rscgl.ui.onscreen;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.rscgl.Game;
import com.rscgl.actions.WalkToPointAction;
import com.rscgl.GameWorld;
import com.rscgl.model.entity.*;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.model.entity.ObjectEntity;

import static com.rscgl.GameWorld.TSIZE;

public class Minimap extends Widget {

    private final TextureRegion PLAYER = new TextureRegion(Backgrounds.createSolidBackgroundT(Color.WHITE, 3, 3));
    private final TextureRegion NPC = new TextureRegion(Backgrounds.createSolidBackgroundT(Color.YELLOW, 3, 3));
    private final TextureRegion ITEM = new TextureRegion(Backgrounds.createSolidBackgroundT(Color.RED, 3, 3));
    private final TextureRegion OBJECT = new TextureRegion(Backgrounds.createSolidBackgroundT(Color.CYAN, 3, 3));
    private final TextureRegion WAYPOINT = new TextureRegion(Backgrounds.createSolidBackgroundT(new Color(), Color.RED, 6, 6));
    //private final TextureRegion compass = new TextureRegion(Assets.inst.getSprite("interfaces", 22));

    private final GameWorld world;
    private final Camera camera;

    private float waypointX = -1;
    private float waypointY = -1;

    private final float radius = 16 * 3;

    private Texture minimapTexture;
    private TextureRegion viewRegion;

    private final Rectangle scissors;
    private final Rectangle clipBounds;

    /**
     * Map position offset relative to getX(), getY()
     */
    private float mapImageOffsetX = 0;
    private float mapImageOffsetY = 0;

    private final float scale = 1.5f;
    private float imageWidth;
    private float imageHeight;
    private MinimapImage map;

    public  Minimap() {
        this.world = Game.world();
        this.camera = Game.cam();
        scissors = new Rectangle();
        clipBounds = new Rectangle();
        setSize(156, 152);
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                float offsetX = (getWidth() / 2) - x;
                float offsetY = (getHeight() / 2) - y;

                offsetX /= scale;
                offsetY /= scale;
                offsetX += getWorldX(Game.inst.localPlayer);
                offsetY += getWorldZ(Game.inst.localPlayer);
                waypointX = offsetX;
                waypointY = offsetY;

                int walkX = (int) ((waypointX + map.regionBaseX * TSIZE) / TSIZE);
                int walkY = (int) ((waypointY + map.regionBaseZ * TSIZE) / TSIZE);
                new WalkToPointAction(walkX, walkY).execute();
                return true;
            }
        });
    }



    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        if (minimapTexture == null) {
            return;
        }
        /**
         * Set clip to the image area
         */
        clipBounds.set(getX(), getY(), getWidth(), getHeight());


        /**
         * then draw huge ass map, offset to our player position.
         * */
        PlayerEntity us = Game.inst.localPlayer;

        float ourX = getWorldX(us) * scale;
        float ourY = getWorldZ(us) * scale;
        imageWidth = viewRegion.getRegionWidth() * scale;
        imageHeight = viewRegion.getRegionHeight() * scale;

        //player position + dimensions/2 to center the view
        mapImageOffsetX = ourX - imageWidth + (getWidth() / 2);
        mapImageOffsetY = ourY - imageHeight + (getHeight() / 2);
        batch.flush();
        setOrigin(getWidth()/2, getHeight()/2);
        ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), clipBounds, scissors);
        ScissorStack.pushScissors(scissors);
        updateRotation();
        drawMinimapImage(batch);
        drawMinimapEntities(batch);
        ScissorStack.popScissors();
    }

    public float getWorldX(MobEntity m) {
        return -m.getPosition().x - (map.regionBaseX * TSIZE);
    }

    public float getWorldZ(MobEntity m) {
        return m.getPosition().z - (map.regionBaseZ * TSIZE);
    }


    private void drawMinimapImage(Batch batch) {
        float x = getX();
        float y = getY();

        PlayerEntity us = Game.inst.localPlayer;

        float ourX = getWorldX(us) * scale;
        float ourY = getWorldZ(us) * scale;
        batch.draw (viewRegion,
                x + mapImageOffsetX, y + mapImageOffsetY,
                imageWidth - ourX, imageHeight - ourY,
                imageWidth, imageHeight,
                1, 1, getRotation());
        batch.flush();
    }

    private void updateRotation() {
        float camAngle = (float) Math.atan2(camera.up.x, -camera.up.z) * MathUtils.radiansToDegrees;
        setRotation(camAngle);
    }

    private void drawMinimapEntities(Batch batch) {
        PlayerEntity us = Game.inst.localPlayer;
        float mapCenterX = getX() + getWidth()/2;
        float mapCenterY = getY() + getHeight()/2;

        float wx = (waypointX - getWorldX(us)) * scale;
        float wy = (waypointY - getWorldZ(us)) * scale;
        batch.draw (WAYPOINT,
                mapCenterX - wx,  mapCenterY - wy, wx, wy,
                6, 6,
                1, 1, getRotation());

        for (ObjectEntity objectEntity : world.getObjects()) {
            float offsetX = (getWorldX(objectEntity) - getWorldX(us)) * scale;
            float offsetY = (getWorldZ(objectEntity) - getWorldZ(us)) * scale;
            batch.draw (OBJECT,
                    mapCenterX - offsetX,  mapCenterY - offsetY, offsetX, offsetY,
                    2, 2,
                    1, 1, getRotation());
        }

        for (ItemEntity g : world.getItems()) {
            float offsetX = (getWorldX(g) - getWorldX(us)) * scale;
            float offsetY = (getWorldZ(g) - getWorldZ(us)) * scale;
            batch.draw (ITEM,
                    mapCenterX - offsetX,  mapCenterY - offsetY, offsetX, offsetY,
                    2, 2,
                    1, 1, getRotation());
        }
        for (NpcEntity npc : world.getNpcs()) {
            float offsetX = (getWorldX(npc) - getWorldX(us)) * scale;
            float offsetY = (getWorldZ(npc) - getWorldZ(us)) * scale;

            batch.draw (NPC,
                    mapCenterX - offsetX,  mapCenterY - offsetY, offsetX, offsetY,
                    2, 2,
                    1, 1, getRotation());
        }
        for (PlayerEntity player : world.getPlayers()) {
            float offsetX = (getWorldX(player) - getWorldX(us)) * scale;
            float offsetY = (getWorldZ(player) - getWorldZ(us)) * scale;

            batch.draw (PLAYER,
                    mapCenterX - offsetX,  mapCenterY - offsetY, offsetX, offsetY,
                    2, 2,
                    1, 1, getRotation());
        }

        batch.flush();
    }

    private float getWorldZ(ItemEntity item) {
        return item.getPosition().z - (map.regionBaseZ * TSIZE);
    }

    private float getWorldX(ItemEntity item) {
        return -item.getPosition().x - (map.regionBaseX * TSIZE);
    }


    private float getWorldX(ObjectEntity object) {
        return object.getWorldX() - (map.regionBaseX * TSIZE);
    }

    private float getWorldZ(ObjectEntity object) {
        return object.getWorldZ() - (map.regionBaseZ * TSIZE);
    }


    public void set(MinimapImage map) {
        this.map = map;
        if(minimapTexture != null) {
            minimapTexture.dispose();
        }
        minimapTexture = new Texture(map.image);
        viewRegion = new TextureRegion(minimapTexture);
    }

    public boolean hit2(float x, float y) {
        return clipBounds.contains(x,y) && isVisible();
    }

}
