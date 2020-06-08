package com.rscgl.model.entity;

import com.badlogic.gdx.math.collision.Ray;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.GameObjectDef;

public class ObjectEntity extends Entity {

    private int direction;
    private GameObjectDef objectDef;

    public ObjectEntity() {
        super(EntityType.OBJECT);
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getWidth() {
        if (direction != 0 && direction != 4) {
            return getDef().getHeight();
        } else {
            return getDef().getWidth();
        }
    }

    public int getHeight() {
        if (direction != 0 && direction != 4) {
            return getDef().getWidth();
        } else {
            return getDef().getHeight();
        }
    }

    public GameObjectDef getDef() {
        if(objectDef == null) {
            objectDef = RSCache.OBJECTS[getType()];
        }
        return objectDef;
    }

    public int getDirection() {
        return direction;
    }

    public float getWorldX() {
        return -position.x;
    }

    public float getWorldZ() {
        return position.z;
    }


}
