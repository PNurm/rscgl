package com.rscgl.actions.player;

import com.rscgl.Game;
import com.rscgl.ui.menu.GameAction;

public class FollowPlayerAction extends GameAction {

    private final int playerIndex;

    public FollowPlayerAction(int id) {
        this.playerIndex = id;
    }

    @Override
    public void execute() {
        Game.outBuffer().newPacket(165);
        Game.outBuffer().putShort(playerIndex);
        Game.outBuffer().finishPacket();
    }
}
