package com.rscgl.actions.player;

import com.rscgl.Game;
import com.rscgl.actions.WalkToMobAction;

public class AttackPlayerAction extends WalkToMobAction {

    private final int serverIndex;

    public AttackPlayerAction(int theirX, int theirY, int serverIndex) {
        super(theirX, theirY);
        this.serverIndex = serverIndex;
    }

    @Override
    public void action() {
        Game.outBuffer().newPacket(171);
        Game.outBuffer().putShort(serverIndex);
        Game.outBuffer().finishPacket();

    }
}
