package com.rscgl.model.entity;


import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.rscgl.render.EntityRenderContext;

public abstract class Entity {
    /**
     * The index in the local array
     */
    protected int index;

    /**
     * The index in definitions
     */
    protected int type;

    /**
     * The index in server array
     */
    protected int serverIndex;

    /**
     * Coordinates in the 2D tile-system
     */
    protected int localTileX;
    protected int localTileY;

    /**
     * Entity type
     */
    protected EntityType entityType;

    protected EntityRenderContext<? extends Entity> renderContext;

    public EntityRenderContext<? extends Entity> getRenderContext() {
        return renderContext;
    }

    public void setRenderContext(EntityRenderContext<? extends Entity> renderContext) {
        this.renderContext = renderContext;
    }


    public Vector3 getPosition() {
        return position;
    }

    /**
     * Coordinates in 3D world
     */
    public Vector3 position = new Vector3();

    private boolean picked;

    public Entity(EntityType entityType) {
        this.entityType = entityType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setServerIndex(int serverIndex) {
        this.serverIndex = serverIndex;
    }

    public int getServerIndex() {
        return serverIndex;
    }

    public int getTileX() {
        return localTileX;
    }

    public int getTileY() {
        return localTileY;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public void setTile(int x, int y) {
        this.localTileX = x;
        this.localTileY = y;
    }

    public EntityType getEntityType() {
        return entityType;
    }


    public boolean isPicked() {
        return picked;
    }

    public void setPicked(boolean b) {
        this.picked = b;
    }
}

