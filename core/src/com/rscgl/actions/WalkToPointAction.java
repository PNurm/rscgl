package com.rscgl.actions;

public class WalkToPointAction extends WalkAction {

    public WalkToPointAction(int destX, int destY) {
        super(Type.WALK_TO_POINT, destX, destY, destX, destY, false);
    }

    @Override
    public void action() {}
}
