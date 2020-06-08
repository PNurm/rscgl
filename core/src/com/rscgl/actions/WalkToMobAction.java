package com.rscgl.actions;

public abstract class WalkToMobAction extends WalkAction {

    public WalkToMobAction(int destX, int destY) {
        super(Type.WALK_TO_ENTITY, destX, destY, false);
    }

}
