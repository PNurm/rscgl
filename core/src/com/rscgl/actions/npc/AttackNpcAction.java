package com.rscgl.actions.npc;

import com.rscgl.Game;
import com.rscgl.actions.WalkToMobAction;

public class AttackNpcAction extends WalkToMobAction {

    private final int serverIndex;

    public AttackNpcAction(int theirX, int theirY, int serverIndex) {
        super(theirX, theirY);
        this.serverIndex = serverIndex;
    }

    @Override
    public void action() {
        Game.outBuffer().newPacket(190);
        Game.outBuffer().putShort(serverIndex);
        Game.outBuffer().finishPacket();
    }
}
