package com.rscgl.assets.def;

import com.rscgl.assets.util.BufferUtil;

import java.nio.ByteBuffer;

public class PrayerDef extends EntityDef {

    public int reqLevel;
    public int drainRate;

    public PrayerDef() {}
    public int getReqLevel() {
        return reqLevel;
    }

    public int getDrainRate() {
        return drainRate;
    }


    @Override
    public ByteBuffer pack() {
        ByteBuffer buffer = ByteBuffer.allocate((getName().getBytes().length + 1) + (getDescription().getBytes().length + 1) + 12);
        buffer.putInt(getReqLevel());
        buffer.putInt(getDrainRate());
        buffer.putInt(id);
        BufferUtil.putString(getName(), buffer);
        BufferUtil.putString(getDescription(), buffer);
        return buffer;
    }

    @Override
    public PrayerDef unpack(ByteBuffer buffer) {
        reqLevel = buffer.getInt();
        drainRate = buffer.getInt();
        id = buffer.getInt();
        name = BufferUtil.getString(buffer);
        description = BufferUtil.getString(buffer);
        return this;
    }
}
