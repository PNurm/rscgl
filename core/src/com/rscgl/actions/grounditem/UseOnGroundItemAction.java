package com.rscgl.actions.grounditem;

import com.rscgl.Game;
import com.rscgl.actions.WalkAction;

public class UseOnGroundItemAction extends WalkAction {

    private final int slot;
    private final int x;
    private final int y;
    private int groundItemID;

    @Override
    public void action() {

        Game.outBuffer().newPacket(53);
        Game.outBuffer().putShort(x);
        Game.outBuffer().putShort(y);
        Game.outBuffer().putShort(groundItemID);
        Game.outBuffer().putShort(slot);
        Game.outBuffer().finishPacket();

        ui.getSpellMenu().setSelectedSpell(-1);
    }

    public UseOnGroundItemAction(int x, int y, int groundItemID, int slot) {
        super(Type.WALK_TO_ENTITY, x, y, false);
        this.slot = slot;
        this.groundItemID = groundItemID;
        this.x = x;
        this.y = y;
    }
}