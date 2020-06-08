package com.rscgl.actions.gameobject;

import com.rscgl.Game;
import com.rscgl.actions.WalkToGameObject;
import com.rscgl.model.entity.ObjectEntity;

public class ObjectAction extends WalkToGameObject {

    private final int x;
    private final int y;
    private final int option;

    public ObjectAction(ObjectEntity objectEntity, int option) {
        super(objectEntity);
        this.x = objectEntity.getTileX();
        this.y = objectEntity.getTileY();
        this.option = option;
    }

    @Override
    public void action() {
        Game.outBuffer().newPacket(option == 0 ? 136 : 79);
        Game.outBuffer().putShort(x);
        Game.outBuffer().putShort(y);
        Game.outBuffer().finishPacket();
    }
}
