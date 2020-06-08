package com.rscgl.actions.grounditem;

import com.rscgl.Game;
import com.rscgl.actions.WalkAction;

public class CastOnGroundItemAction extends WalkAction {

    private final int spell;
    private final int x;
    private final int y;
    private final int id;

    @Override
    public void action() {

        Game.outBuffer().newPacket(249);
        Game.outBuffer().putShort(spell);
        Game.outBuffer().putShort(x);
        Game.outBuffer().putShort(y);
        Game.outBuffer().putShort(id);
        Game.outBuffer().finishPacket();

        ui.getSpellMenu().setSelectedSpell(-1);
    }

    public CastOnGroundItemAction(int id, int x, int y, int spell) {
        super(Type.WALK_TO_ENTITY, x, y, true);
        this.id = id;
        this.spell = spell;
        this.x = x;
        this.y = y;
    }
}
