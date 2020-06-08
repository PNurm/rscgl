package com.rscgl.assets.def;

import com.rscgl.assets.util.BufferUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class SpellDef extends EntityDef {

    public int reqLevel;
    public int type;
    public int runeCount;
    public HashMap<Integer, Integer> requiredRunes = new HashMap<Integer, Integer>();

    public SpellDef() {}

    public int getReqLevel() {
        return reqLevel;
    }

    public int getSpellType() {
        return type;
    }

    public int getRuneCount() {
        return runeCount;
    }

    public Set<Entry<Integer, Integer>> getRunesRequired() {
        return requiredRunes.entrySet();
    }

    @Override
    public ByteBuffer pack() {

        int numberOfRequiredRunes = requiredRunes.size();

        ByteBuffer buffer = ByteBuffer.allocate((getName().getBytes().length + 1) + (getDescription().getBytes().length + 1) + 20 + (numberOfRequiredRunes * 8));
        buffer.putInt(getReqLevel());
        buffer.putInt(getSpellType());
        buffer.putInt(getRuneCount());
        buffer.putInt(numberOfRequiredRunes);
        for(Entry<Integer, Integer> entry : requiredRunes.entrySet()) {
            buffer.putInt(entry.getKey());
            buffer.putInt(entry.getValue());
        }
        buffer.putInt(id);
        BufferUtil.putString(getName(), buffer);
        BufferUtil.putString(getDescription(), buffer);
        return buffer;
    }

    @Override
    public SpellDef unpack(ByteBuffer buffer) {
        reqLevel = buffer.getInt();
        type = buffer.getInt();
        runeCount = buffer.getInt();

        int numberOfRequiredRunes = buffer.getInt();
        for(int i = 0; i < numberOfRequiredRunes; i++)
        {
            int key = buffer.getInt();
            int value = buffer.getInt();
            requiredRunes.put(key, value);
        }

        id = buffer.getInt();
        name = BufferUtil.getString(buffer);
        description = BufferUtil.getString(buffer);
        return this;
    }
}
