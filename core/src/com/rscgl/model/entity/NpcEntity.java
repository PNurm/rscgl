package com.rscgl.model.entity;

import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.NPCDef;

public class NpcEntity extends MobEntity {

    public NpcEntity(int type) {
        super(EntityType.NPC);
        setType(type);

        this.def = RSCache.NPCS[getType()];
        if (def != null) {
            int[] sprites = def.sprites;
            for (int i = 0; i < sprites.length; i++) {
                int animationID = sprites[i];
                if (animationID > -1 && animationID < RSCache.ANIMATION_COUNT)
                    layerAnimation[i] = RSCache.ANIMATIONS[animationID];
            }
            colourHair = def.hairColour;
            colourTop = def.topColour;
            colourBottom = def.bottomColour;
            colourSkin = def.skinColour;
            combatLevel = (def.getStr()
                    + def.getAtt()
                    + def.getDef()
                    + def.getHits()) / 4;
        }
    }

    private NPCDef def;


    public NPCDef getDef() {
        return def;
    }

}
