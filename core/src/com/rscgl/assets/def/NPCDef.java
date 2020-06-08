package com.rscgl.assets.def;

import com.rscgl.assets.util.BufferUtil;

import java.nio.ByteBuffer;

public class NPCDef extends EntityDef {
    public String command;
    public int[] sprites = new int[12];
    public int hairColour;
    public int topColour;
    public int bottomColour;
    public int skinColour;
    public int width, height;
    public int walkModel, combatModel, combatSprite;
    public int hits;
    public int attack;
    public int defense;
    public int strength;
    public boolean attackable;
    public boolean requested;
    public boolean placeholder;

    public NPCDef(){ }

    public NPCDef(String name, String description, String command, int attack, int strength, int hits, int defense, boolean attackable, int[] sprites, int hairColour, int topColour, int bottomColour, int skinColour, int width, int height, int walkModel, int combatModel, int combatSprite, int id) {
        super.name = name;
        super.description = description;
        super.id = id;
        this.command = command;
        this.attack = attack;
        this.strength = strength;
        this.hits = hits;
        this.defense = defense;
        this.attackable = attackable;
        this.sprites = sprites;
        this.hairColour = hairColour;
        this.topColour = topColour;
        this.bottomColour = bottomColour;
        this.skinColour = skinColour;
        this.width = width;
        this.height = height;
        this.walkModel = walkModel;
        this.combatModel = combatModel;
        this.combatSprite = combatSprite;
    }
    public String getCommand() {
        return command;
    }

    public int getSprite(int index) {
        return sprites[index];
    }

    public int getHairColour() {
        return hairColour;
    }

    public int getTopColour() {
        return topColour;
    }

    public int getBottomColour() {
        return bottomColour;
    }

    public int getSkinColour() {
        return skinColour;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getWalkModel() {
        return walkModel;
    }

    public int getCombatModel() {
        return combatModel;
    }

    public int getCombatSprite() {
        return combatSprite;
    }

    public int getHits() {
        return hits;
    }

    public int getAtt() {
        return attack;
    }

    public int getDef() {
        return defense;
    }

    public int getStr() {
        return strength;
    }

    public int[] getStats() {
        return new int[]{attack, defense, strength};
    }

    public boolean isAttackable() {
        return attackable;
    }


    @Override
    public ByteBuffer pack() {
        ByteBuffer buffer = ByteBuffer.allocate((getCommand().getBytes().length + 1) + (getName().getBytes().length + 1) + (getDescription().getBytes().length + 1) + 109);
        buffer.putInt(sprites.length);
        for (int i = 0; i < sprites.length; i++) {
            buffer.putInt(getSprite(i));
        }
        buffer.putInt(getHairColour());
        buffer.putInt(getTopColour());
        buffer.putInt(getBottomColour());
        buffer.putInt(getSkinColour());
        buffer.putInt(getWidth());
        buffer.putInt(getHeight());
        buffer.putInt(getWalkModel());
        buffer.putInt(getCombatModel());
        buffer.putInt(getCombatSprite());
        buffer.putInt(getHits());
        buffer.putInt(getAtt());
        buffer.putInt(getDef());
        buffer.putInt(getStr());
        buffer.put((byte) (isAttackable() ? 1 : 0));
        buffer.putInt(id);
        BufferUtil.putString(getName(), buffer);
        BufferUtil.putString(getDescription(), buffer);
        BufferUtil.putString(getCommand(), buffer);
        return buffer;
    }

    @Override
    public NPCDef unpack(ByteBuffer buffer) {
        int totalSprites = buffer.getInt();

        for (int i = 0; i < totalSprites; i++) {
            sprites[i] = buffer.getInt();
        }
        hairColour = buffer.getInt();
        topColour = buffer.getInt();
        bottomColour = buffer.getInt();
        skinColour = buffer.getInt();
        width = buffer.getInt();
        height = buffer.getInt();
        walkModel = buffer.getInt();
        combatModel = buffer.getInt();
        combatSprite = buffer.getInt();
        hits = buffer.getInt();
        attack = buffer.getInt();
        defense = buffer.getInt();
        strength = buffer.getInt();
        attackable = buffer.get() == 1;

        id = buffer.getInt();
        name = BufferUtil.getString(buffer);
        description = BufferUtil.getString(buffer);
        command = BufferUtil.getString(buffer);
        return this;
    }
}
