package com.rscgl.assets.def;

import com.rscgl.assets.util.BufferUtil;

import java.nio.ByteBuffer;

public class ItemDef extends EntityDef {

	public int pictureMask2;
	public String command;
	public int basePrice;
	public int sprite;
	public boolean stackable;
	public boolean wieldable;
	public int colour;
	public boolean tradable;
	public boolean members;
	public boolean unknown;
	public int wearable;

	private int isNotedFormOf = -1;
	private int notedFormID = -1;

	public ItemDef() {
	}

	public String getCommand() {
		return command;
	}

	public int getSprite() {
		return sprite;
	}

	public int getBasePrice() {
		return basePrice;
	}

	public boolean isStackable() {
		return stackable;
	}

	public boolean isWieldable() {
		return wieldable;
	}

	public int getColour() {
		return colour;
	}

	public int getNoteItem() {
		if (id == 1298)
			return 12;
		return -1;
	}

	public int getNotedFormOf() {
		return isNotedFormOf;
	}

	public void setNotedFormOf(int notedFormOf) {
		this.isNotedFormOf = notedFormOf;
	}

	public void setNotedForm(int id) {
		this.notedFormID = id;
	}

	public int getNotedForm() {
		return notedFormID;
	}
	
	@Override
    public ByteBuffer pack() {
        ByteBuffer buffer = ByteBuffer.allocate((getCommand().getBytes().length + 1) + (getName().getBytes().length + 1) + (getDescription().getBytes().length + 1) + 32);
        buffer.putInt(pictureMask2);
        buffer.putInt(getBasePrice());
        buffer.putInt(getSprite());
        buffer.put((byte)(isStackable() ? 1 : 0));
        buffer.put((byte)(isWieldable() ? 1 : 0));
        buffer.putInt(getColour());
        buffer.put((byte)(tradable ? 1 : 0));
        buffer.put((byte)(members ? 1 : 0));
        buffer.putInt(getNotedFormOf());
        buffer.putInt(getNotedForm());
        buffer.putInt(id);
        BufferUtil.putString(getName(), buffer);
        BufferUtil.putString(getDescription(), buffer);
        BufferUtil.putString(getCommand(), buffer);
        return buffer;
    }

    @Override
    public ItemDef unpack(ByteBuffer buffer) {
        pictureMask2 = buffer.getInt();
        basePrice = buffer.getInt();
        sprite = buffer.getInt();
        stackable = buffer.get() == 1;
        wieldable = buffer.get() == 1;
		colour = buffer.getInt();
		tradable = buffer.get() == 1;
		members = buffer.get() == 1;
		setNotedFormOf(buffer.getInt());
		setNotedForm(buffer.getInt());
		
		id = buffer.getInt();
		name = BufferUtil.getString(buffer);
		description = BufferUtil.getString(buffer);
		command = BufferUtil.getString(buffer);
        return this;
    }
}
