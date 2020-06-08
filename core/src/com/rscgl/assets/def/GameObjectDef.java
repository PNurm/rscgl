package com.rscgl.assets.def;

import com.rscgl.assets.util.BufferUtil;

import java.nio.ByteBuffer;

public class GameObjectDef extends EntityDef {
    public String option1;
    public String option2;
    public int type;
    public int width;
    public int height;
    public int itemHeight;
    public String objectModel;
    public int modelID;

	public GameObjectDef() {}

	
    public String getObjectModel() {
        return objectModel;
    }

    public String getOption1() {
        return option1.toLowerCase();
    }

    public String getOption2() {
        return option2.toLowerCase();
    }

    public int getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getItemHeight() {
        return itemHeight;
    }
	
	@Override
    public ByteBuffer pack() {
        ByteBuffer buffer = ByteBuffer.allocate((getOption1().getBytes().length + 1) + (getOption2().getBytes().length + 1) + (objectModel.getBytes().length + 1) + (getName().getBytes().length + 1) + (getDescription().getBytes().length + 1) + 24);
        buffer.putInt(getType());
        buffer.putInt(getWidth());
        buffer.putInt(getHeight());
        buffer.putInt(getItemHeight());
        buffer.putInt(modelID);
        buffer.putInt(id);
        BufferUtil.putString(getName(), buffer);
        BufferUtil.putString(getDescription(), buffer);		
        BufferUtil.putString(getOption1(), buffer);
        BufferUtil.putString(getOption2(), buffer);
        BufferUtil.putString(objectModel, buffer);
        return buffer;
    }

    @Override
    public GameObjectDef unpack(ByteBuffer buffer) {
        type = buffer.getInt();
        width = buffer.getInt();
        height = buffer.getInt();
        itemHeight = buffer.getInt();
        modelID = buffer.getInt();
		id = buffer.getInt();
		name = BufferUtil.getString(buffer);
		description = BufferUtil.getString(buffer);			
        option1 = BufferUtil.getString(buffer);
        option2 = BufferUtil.getString(buffer);
        objectModel = BufferUtil.getString(buffer);
        return this;
    }
}
