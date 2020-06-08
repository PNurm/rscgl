package com.rscgl.assets.def;

import java.nio.ByteBuffer;

public class RoofDef extends GameDefinition {
    public int height;
    public int numVertices;

    public RoofDef() {}
	
    public int getHeight() {
        return height;
    }

    public int getNumVertices() {
        return numVertices;
    }
	
	@Override
    public ByteBuffer pack() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(getHeight());
        buffer.putInt(getNumVertices());
        return buffer;
    }

    @Override
    public RoofDef unpack(ByteBuffer buffer) {
        height = buffer.getInt();
        numVertices = buffer.getInt();
        return this;
    }	
}
