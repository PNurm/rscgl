package com.rscgl.actions.npc;

import com.rscgl.Game;

import com.rscgl.actions.WalkToMobAction;

public class TalkToNpcAction extends WalkToMobAction {

    private final int serverIndex;

    public TalkToNpcAction(int theirX, int theirY, int serverIndex) {
        super(theirX, theirY);
        this.serverIndex = serverIndex;
    }

    @Override
    public void action() {
        Game.outBuffer().newPacket(153);
        Game.outBuffer().putShort(serverIndex);
        Game.outBuffer().finishPacket();
    }
}
