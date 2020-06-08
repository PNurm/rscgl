package com.rscgl.actions.player;

import com.rscgl.Game;
import com.rscgl.ui.menu.GameAction;

public class TradePlayerAction extends GameAction {

    private final int playerIndex;

    public TradePlayerAction(int id) {
        this.playerIndex = id;
    }

    @Override
    public void execute() {
        Game.outBuffer().newPacket(142);
        Game.outBuffer().putShort(playerIndex);
        Game.outBuffer().finishPacket();


    }
}
