package com.rscgl.actions.wallobject;

import com.rscgl.Game;
import com.rscgl.actions.WalkToWallObjectAction;
import com.rscgl.model.entity.WallEntity;

public class WallAction extends WalkToWallObjectAction {

    private final int x;
    private final int y;
    private final int option;
    private final int dir;

    public WallAction(WallEntity object, int option) {
        super(object);
        this.x = object.getTileX();
        this.y = object.getTileY();
        this.option = option;
        this.dir = object.getDirection();
    }

    @Override
    public void action() {
        Game.outBuffer().newPacket(option == 0 ? 14 : 127);
        Game.outBuffer().putShort(x);
        Game.outBuffer().putShort(y);
        Game.outBuffer().putShort(dir);
        Game.outBuffer().finishPacket();
    }
}
