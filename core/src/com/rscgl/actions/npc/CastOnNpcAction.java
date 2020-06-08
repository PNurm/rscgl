package com.rscgl.actions.npc;

import com.rscgl.Game;
import com.rscgl.actions.WalkToMobAction;

public class CastOnNpcAction extends WalkToMobAction {

    private final int serverIndex;
    private final int spellIndex;

    public CastOnNpcAction(int theirX, int theirY, int serverIndex, int spellIndex) {
        super(theirX, theirY);
        this.serverIndex = serverIndex;
        this.spellIndex = spellIndex;
    }

    @Override
    public void action() {
        Game.outBuffer().newPacket(50);
        Game.outBuffer().putShort(spellIndex);
        Game.outBuffer().putShort(serverIndex);
        Game.outBuffer().finishPacket();

        ui.getSpellMenu().setSelectedSpell(-1);
    }

}
