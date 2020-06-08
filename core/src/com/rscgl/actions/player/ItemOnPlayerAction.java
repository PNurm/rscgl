package com.rscgl.actions.player;

import com.rscgl.Game;
import com.rscgl.actions.WalkToMobAction;

public class ItemOnPlayerAction extends WalkToMobAction {

    private final int serverIndex;
    private final int inventorySlot;

    public ItemOnPlayerAction(int theirX, int theirY, int serverIndex, int inventorySlot) {
        super(theirX, theirY);
        this.serverIndex = serverIndex;
        this.inventorySlot = inventorySlot;
    }

    @Override
    public void action() {
        Game.outBuffer().newPacket(171);
        Game.outBuffer().putShort(serverIndex);
        Game.outBuffer().putShort(inventorySlot);
        Game.outBuffer().finishPacket();

        ui.getInventory().setSelectedSlot(-1);
    }

}

