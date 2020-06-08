package com.rscgl.actions.grounditem;

import com.rscgl.Game;
import com.rscgl.actions.WalkAction;

public class TakeGroundItemAction extends WalkAction {

    private final int x;
    private final int y;
    private final int groundItemID;

    @Override
    public void action() {

        Game.outBuffer().newPacket(247);
        Game.outBuffer().putShort(x);
        Game.outBuffer().putShort(y);
        Game.outBuffer().putShort(groundItemID);
        Game.outBuffer().finishPacket();

        ui.getSpellMenu().setSelectedSpell(-1);
    }

    public TakeGroundItemAction(int x, int y, int groundItemID) {
        super(Type.WALK_TO_ENTITY, x, y, false);
        this.groundItemID = groundItemID;
        this.x = x;
        this.y = y;
    }
}
