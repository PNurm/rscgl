package com.rscgl.assets.def;

import com.rscgl.assets.util.BufferUtil;

import java.nio.ByteBuffer;

public class MapLabel extends GameDefinition {

    public MapLabel() {}

    private String label = "";
    private int icon;
    private int fontSize;
    private int x;
    private int y;
    public byte type;

    public MapLabel(int icon, String label, int x, int y) {
        this.label = label;
        this.icon = icon;
        this.x = x;
        this.y = y;
    }

    public MapLabel(String label, int x, int y, int fontSize) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.fontSize = fontSize;
    }

    public MapLabel(int icon, int font, int x, int y, String string) {
        this.icon = icon;
        this.fontSize = font;
        this.x = x;
        this.y = y;
        this.label = string;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getLabel() {
        return label;
    }

    public int getIcon() {
        return icon;
    }

    @Override
    public ByteBuffer pack() {
        return null;
    }

    @Override
    public MapLabel unpack(ByteBuffer buffer) {
        icon = buffer.getShort();
        fontSize = buffer.getShort();
        x = buffer.getShort();
        y = buffer.getShort();
        label = BufferUtil.getString(buffer);
        return this;
    }
}
