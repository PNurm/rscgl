package com.rscgl.actions.player;

import com.rscgl.Game;
import com.rscgl.ui.menu.GameAction;

public class DuelPlayerAction extends GameAction {

    private final int playerIndex;

    public DuelPlayerAction(int id) {
        this.playerIndex = id;
    }

    @Override
    public void execute() {
        Game.outBuffer().newPacket(8);
        Game.outBuffer().putShort(playerIndex);
        Game.outBuffer().finishPacket();
    }
}
