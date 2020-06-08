package com.rscgl.assets.def;

import com.rscgl.assets.util.BufferUtil;

import java.nio.ByteBuffer;

public class WallObjectDef extends EntityDef {

    public int adjacent;

    public WallObjectDef() {

    }

    public String option1;
    public String option2;
    public int doorType;
    public int invisible;
    public int height;
    public int frontTexture;
    public int backTexture;

    public String getOption1() {
        return option1.toLowerCase();
    }

    public String getOption2() {
        return option2.toLowerCase();
    }

    public int getType() {
        return doorType;
    }

    public int getInvisible() {
        return invisible;
    }

    public int getHeight() {
        return height;
    }

    public int getFrontTex() {
        return frontTexture;
    }

    public int getBackTex() {
        return backTexture;
    }
	
	@Override
    public ByteBuffer pack() {
        ByteBuffer buffer = ByteBuffer.allocate((getOption1().getBytes().length + 1) + (getOption2().getBytes().length + 1) + (getName().getBytes().length + 1) + (getDescription().getBytes().length + 1) + 24);
        buffer.putInt(getType());
        buffer.putInt(getInvisible());
        buffer.putInt(getHeight());
        buffer.putInt(getFrontTex());
        buffer.putInt(getBackTex());
        buffer.putInt(id);
        BufferUtil.putString(getName(), buffer);
        BufferUtil.putString(getDescription(), buffer);
        BufferUtil.putString(getOption1(), buffer);
        BufferUtil.putString(getOption2(), buffer);
        return buffer;
    }

    @Override
    public WallObjectDef unpack(ByteBuffer buffer) {
        doorType = buffer.getInt();
        invisible = buffer.getInt();
        height = buffer.getInt();
        frontTexture = buffer.getInt();
        backTexture = buffer.getInt();
		id = buffer.getInt();
		name = BufferUtil.getString(buffer);
		description = BufferUtil.getString(buffer);
        option1 = BufferUtil.getString(buffer);
        option2 = BufferUtil.getString(buffer);
        return this;
    }
}
