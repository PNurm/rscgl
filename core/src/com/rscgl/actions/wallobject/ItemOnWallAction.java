package com.rscgl.actions.wallobject;

import com.rscgl.Game;
import com.rscgl.actions.WalkToWallObjectAction;
import com.rscgl.model.entity.WallEntity;

public class ItemOnWallAction extends WalkToWallObjectAction {

    private final int slot;
    private final int x;
    private final int y;
    private final int dir;

    public ItemOnWallAction(WallEntity object, int slot) {
        super(object);
        this.slot = slot;
        this.x = object.getTileX();
        this.dir = object.getDirection();
        this.y = object.getTileY();
    }

    @Override
    public void action() {
        Game.outBuffer().newPacket(161);
        Game.outBuffer().putShort(x);
        Game.outBuffer().putShort(y);
        Game.outBuffer().putByte(dir);
        Game.outBuffer().putShort(slot);
        Game.outBuffer().finishPacket();

       ui.getInventory().setSelectedSlot(-1);
    }
}
