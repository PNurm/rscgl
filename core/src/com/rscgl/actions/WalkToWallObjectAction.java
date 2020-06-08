package com.rscgl.actions;

import com.rscgl.model.entity.WallEntity;

public abstract class WalkToWallObjectAction extends WalkAction {

    public WalkToWallObjectAction(WallEntity object) {
        super(Type.WALK_TO_ENTITY,
                object.getTileX(),
                 object.getTileY(),
                object.getDirection() != 1);
    }

}
