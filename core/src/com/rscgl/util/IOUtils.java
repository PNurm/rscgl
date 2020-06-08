package com.rscgl.util;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class IOUtils {

    public static int sizeof(String s) {
        if (s == null)
            return 4;
        return 4 + stringToByteArray(s).length;
    }

    public static String byteToString(byte[] data, int offset, int length) {
        return new String(data, offset, length, StandardCharsets.UTF_8);
    }

    public static byte[] stringToByteArray(String message) {
        return message.getBytes(StandardCharsets.UTF_8);
    }

    public static String unpackString(ByteBuffer b) {
        int l = b.getInt();
        if (l < 0)
            return null;
        byte[] data = new byte[l];
        b.get(data);
        return byteToString(data, 0, data.length);
    }


    public static void pack(String s, ByteBuffer b) {
        if (s == null) {
            b.putInt(-1);
            return;
        }
        byte[] data = stringToByteArray(s);
        b.putInt(data.length);
        b.put(data);
    }

}
