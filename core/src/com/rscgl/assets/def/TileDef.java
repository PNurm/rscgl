package com.rscgl.assets.def;

import java.nio.ByteBuffer;

public class TileDef extends GameDefinition {

    public TileDef() {}

    @Override
    public ByteBuffer pack() {
        ByteBuffer b = ByteBuffer.allocate(12);
        b.putInt(colour);
        b.putInt(type);
        b.putInt(adjacent);
        return b;
    }

    @Override
    public TileDef unpack(ByteBuffer buffer) {
        colour = buffer.getInt();
        type = buffer.getInt();
        adjacent = buffer.getInt();
        return this;
    }

    public int colour;
    public int type;
    public int adjacent;

	public TileDef(int colour, int type, int objectType) {
		this.colour = colour;
		this.type = type;
		this.adjacent = objectType;
	}
	
    public int getTexture() {
        return colour;
    }

    public int getType() {
        return type;
    }

    public int getAdjacent() {
        return adjacent;
    }
}
