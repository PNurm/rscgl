package com.rscgl.actions;

import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.GameObjectDef;
import com.rscgl.model.entity.ObjectEntity;

public abstract class WalkToGameObject extends WalkAction {

    public WalkToGameObject(ObjectEntity objectEntity) {
        super(Type.WALK_TO_ENTITY,  objectEntity.getTileX(), objectEntity.getTileY(), true);
        GameObjectDef def = RSCache.OBJECTS[objectEntity.getType()];
        int width = def.getWidth();
        int height = def.getHeight();
        if (objectEntity.getDirection() != 0 && objectEntity.getDirection() != 4) {
            width = def.getHeight();
            height = def.getWidth();
        }
        if (def.getType() != 2 && def.getType() != 3) {
            this.maxX = minX + width - 1;
            this.maxY = minY + height - 1;
            this.reachBorder = true;
        } else {
            if (objectEntity.getDirection() == 0) {
                ++width;
                --minX;
            }
            if (objectEntity.getDirection() == 2) {
                ++height;
            }
            if (objectEntity.getDirection() == 6) {
                --minY;
                ++height;
            }
            if (objectEntity.getDirection() == 4) {
                ++width;
            }
            this.maxX = width + (minX - 1);
            this.maxY =  height + minY - 1;
            this.reachBorder = false;
        }
    }

}
