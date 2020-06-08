package com.rscgl.assets.def;

public class AnimationDef {

    public int blueColorMask;
    public String name;
    public int charColour;
    public int genderModel;
    public boolean combatFrames;
    public boolean walkFrames;
    public int spriteIndex;

    public AnimationDef(){}

    public String getName() {
        return name;
    }

    public int getCharColour() {
        return charColour;
    }

    public boolean hasCombatSprites() {
        return combatFrames;
    }

    public boolean hasF() {
        return walkFrames;
    }

}
