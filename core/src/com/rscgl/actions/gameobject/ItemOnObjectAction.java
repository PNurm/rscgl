package com.rscgl.actions.gameobject;

import com.rscgl.Game;
import com.rscgl.actions.WalkToGameObject;
import com.rscgl.model.entity.ObjectEntity;

public class ItemOnObjectAction extends WalkToGameObject {

    private final int slot;
    private final int x;
    private final int y;

    public ItemOnObjectAction(ObjectEntity objectEntity, int slot) {
        super(objectEntity);
        this.slot = slot;
        this.x = objectEntity.getTileX();
        this.y = objectEntity.getTileY();
    }

    @Override
    public void action() {
        Game.outBuffer().newPacket(161);
        Game.outBuffer().putShort(x);
        Game.outBuffer().putShort(y);
        Game.outBuffer().putShort(slot);
        Game.outBuffer().finishPacket();

       ui.getInventory().setSelectedSlot(-1);
    }
}
