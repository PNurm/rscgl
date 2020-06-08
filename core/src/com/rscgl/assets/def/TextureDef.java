package com.rscgl.assets.def;

import com.rscgl.assets.util.BufferUtil;

import java.nio.ByteBuffer;

public class TextureDef extends GameDefinition {
    @Override
    public ByteBuffer pack() {
        ByteBuffer buffer = ByteBuffer.allocate(overlayName.length() + textureName.length() + 2);
        BufferUtil.putString(overlayName, buffer);
        BufferUtil.putString(textureName, buffer);
        return buffer;
    }

    @Override
    public TextureDef unpack(ByteBuffer buffer) {
        overlayName = BufferUtil.getString(buffer);
        textureName = BufferUtil.getString(buffer);
        return this;
    }

    public String overlayName;
    public String textureName;

    public TextureDef() {}
	
    public String getOverlayName() {
        return overlayName;
    }

    public String getTextureName() {
        return textureName;
    }
}
