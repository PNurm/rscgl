package com.rscgl.assets.util;

import java.nio.ByteBuffer;

public class BufferUtil {
    public static final void putString(String var2, ByteBuffer buffer) {
        byte[] stringBytes = var2.getBytes();
        for (byte b : stringBytes)
            buffer.put(b);
        buffer.put((byte) 10);
    }

    public static String getString(ByteBuffer buffer) {
        StringBuilder bldr = new StringBuilder();
        byte b;
        while ((b = buffer.get()) != 10) {
            bldr.append((char) b);
        }
        return bldr.toString();
    }
}
