package com.rscgl.net;

import com.rscgl.util.StringEncryption;

import java.math.BigInteger;

public class Buffer {

    public static final int[] bitwiseMaskForShift = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095,
            8191, 16383, 32767, '\uffff', 131071, 262143, 524287, 1048575, 2097151, 4194303, 8388607, 16777215,
            33554431, 67108863, 134217727, 268435455, 536870911, 1073741823, Integer.MAX_VALUE, -1};

    public byte[] dataBuffer;
    public int offset;
    private int bitHead;

    public Buffer(int size) {
        this.dataBuffer = new byte[size];
        this.offset = 0;
    }

    public final int getBitHead() {
        return this.bitHead;
    }

    public final void startBitAccess() {
        this.bitHead = this.offset * 8;
    }

    public final int getBitMask(int count) {
        int bite = this.bitHead >> 3;
        int shift = 8 - (this.bitHead & 7);

        this.bitHead += count;
        int result = 0;
        while (count > shift) {
            result += (bitwiseMaskForShift[shift] & this.dataBuffer[bite++]) << count - shift;
            count -= shift;
            shift = 8;
        }

        if (count != shift) {
            result += this.dataBuffer[bite] >> shift - count & bitwiseMaskForShift[count];
        } else {
            result += this.dataBuffer[bite] & bitwiseMaskForShift[shift];
        }

        return result;
    }

    public final void endBitAccess() {
        this.offset = (7 + this.bitHead) / 8;
    }

    public final void putNullThenString(String str) {

        int var3 = str.indexOf(0);
        if (var3 < 0) {
            this.dataBuffer[this.offset++] = 0;
            this.offset += StringEncryption.putStringIntoBytes(str, 0, str.length(), this.dataBuffer, this.offset);
            this.dataBuffer[this.offset++] = 0;
        } else {
            throw new IllegalArgumentException("");
        }

    }

    public final void put24(int val) {
        this.dataBuffer[this.offset++] = (byte) (val >> 16);
        this.dataBuffer[this.offset++] = (byte) (val >> 8);
        this.dataBuffer[this.offset++] = (byte) val;
    }

    private final void readBytes(int offset, int count, byte[] out) {

        for (int i = offset; i < count + offset; ++i) {
            out[i] = this.dataBuffer[this.offset++];
        }
    }

    public final void writeBytes(byte[] src, int offset, int count) {

        for (int i = offset; i < count + offset; ++i) {
            this.dataBuffer[this.offset++] = src[i];
        }

    }

    public final void putByte(int var1) {

        this.dataBuffer[this.offset++] = (byte) var1;

    }

    public final void putString(String var2) {

        byte[] stringBytes = var2.getBytes();
        for (byte b : stringBytes)
            putByte(b);
        putByte(10);

    }

    public final String readString() {
        StringBuilder bldr = new StringBuilder();
        byte b;
        while ((b = dataBuffer[this.offset++]) != 10) {
            bldr.append((char) b);
        }
        return bldr.toString();
    }

    public final void putShort(int val) {

        this.dataBuffer[this.offset++] = (byte) (val >> 8);
        this.dataBuffer[this.offset++] = (byte) val;
    }

    public void putLong(long l) {
        putInt((int) (l >> 32));
        putInt((int) (l & -1L));
    }

    public final byte readByte() {
        return this.dataBuffer[this.offset++];
    }

    public final int readUnsignedByte() {

        return this.dataBuffer[this.offset++] & 255;
    }

    public final void a(int var2, int[] var3, int var4) {

        int var5 = this.offset;

        this.offset = var2;
        int var6 = (var4 - var2) / 8;

        for (int var7 = 0; var7 < var6; ++var7) {
            int var8 = this.readInt();
            int var9 = this.readInt();
            int var10 = 0;
            int var11 = -1640531527;

            for (int var12 = 32; var12-- > 0; var9 += var8 + (var8 >>> 5 ^ var8 << 4)
                    ^ var10 + var3[(7145 & var10) >>> 11]) {
                var8 += (var9 << 4 ^ var9 >>> 5) + var9 ^ var10 + var3[var10 & 3];
                var10 += var11;
            }

            this.offset -= 8;
            this.putInt(var8);
            this.putInt(var9);

            this.offset = var5;
        }
    }

    public final void put16_Offset(int offset) {
        this.dataBuffer[this.offset - offset - 2] = (byte) (offset >> 8);
        this.dataBuffer[this.offset - offset - 1] = (byte) offset;
    }

    public final int readSmart08_16() {
        int var2 = 255 & this.dataBuffer[this.offset];
        return var2 < 128 ? this.readUnsignedByte() : this.readShort() - '\u8000';
    }

    public final int readUnsignedShort() {
        this.offset += 2;
        int strBegin = (255 & this.dataBuffer[this.offset - 1])
                + (this.dataBuffer[this.offset - 2] << 8 & '\uff00');
        if (strBegin > 32767) {
            strBegin -= 65536;
        }
        return strBegin;

    }

    public final int readSmart16_32() {
        return (this.dataBuffer[this.offset] < 0 ? Integer.MAX_VALUE & this.readInt() : this.readShort());
    }

    public final void putSmart08_16(int val) {
        if (val >= 0 && val < 128) {
            this.putByte(val);
        } else if (val >= 0 && val < '\u8000') {
            this.putShort('\u8000' + val);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public final void putInt(int val) {
        this.dataBuffer[this.offset++] = (byte) (val >> 24);
        this.dataBuffer[this.offset++] = (byte) (val >> 16);
        this.dataBuffer[this.offset++] = (byte) (val >> 8);
        this.dataBuffer[this.offset++] = (byte) val;
    }

    public final int readShort() {
        this.offset += 2;
        return ((this.dataBuffer[this.offset - 2] & 255) << 8)
                + (255 & this.dataBuffer[this.offset - 1]);
    }

    public final int readInt() {
        this.offset += 4;
        return (this.dataBuffer[this.offset - 3] << 16 & 16711680)
                + (this.dataBuffer[this.offset - 4] << 24 & -16777216)
                + (0xFF00 & this.dataBuffer[this.offset - 2] << 8)
                + (this.dataBuffer[this.offset - 1] & 255);

    }

    public final long readLong() {
        long var2 = (long) this.readInt() & 4294967295L;
        long var4 = (long) this.readInt() & 4294967295L;
        return (var2 << 1382465952) - -var4;
    }

    public final byte[] toByteArray(int ignore) {
        byte[] bites = new byte[this.offset];
        for (int i = ignore; this.offset > i; ++i) {
            bites[i] = this.dataBuffer[i];
        }
        return bites;
    }


    public final void encodeWithRSA(BigInteger var1, BigInteger var3) {
        int pointerPosition = this.offset;
        this.offset = 0;

        byte[] encodedBuffer = new byte[pointerPosition];

        this.readBytes(0, pointerPosition, encodedBuffer);
        BigInteger var7 = new BigInteger(encodedBuffer);
        BigInteger var8 = var7.modPow(var3, var1);
        byte[] var9 = var8.toByteArray();
        this.offset = 0;
        this.putShort(var9.length);
        this.writeBytes(var9, 0, var9.length);
    }
}
