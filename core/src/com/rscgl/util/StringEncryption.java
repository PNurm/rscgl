package com.rscgl.util;

import com.rscgl.net.Buffer;

public final class StringEncryption {

    public static char[] specialCharLookup = new char[]{'\u20ac', '\u0000', '\u201a', '\u0192', '\u201e', '\u2026',
            '\u2020', '\u2021', '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\u0000', '\u017d', '\u0000',
            '\u0000', '\u2018', '\u2019', '\u201c', '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122',
            '\u0161', '\u203a', '\u0153', '\u0000', '\u017e', '\u0178'};
    private static int[] stringEncodeTable;
    private static byte[] characterTable;
    private static int[] characterMasks;

    static {
        byte[] table = StringEncryption.asByte(22, 22, 22, 22, 22, 22, 21,
                22, 22, 20, 22, 22, 22, 21, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 3, 8,
                22, 16, 22, 16, 17, 7, 13, 13, 13, 16, 7, 10, 6, 16, 10, 11, 12, 12, 12, 12, 13, 13, 14, 14, 11, 14, 19, 15,
                17, 8, 11, 9, 10, 10, 10, 10, 11, 10, 9, 7, 12, 11, 10, 10, 9, 10, 10, 12, 10, 9, 8, 12, 12, 9, 14, 8, 12,
                17, 16, 17, 22, 13, 21, 4, 7, 6, 5, 3, 6, 6, 5, 4, 10, 7, 5, 6, 4, 4, 6, 10, 5, 4, 4, 5, 7, 6, 10, 6, 10,
                22, 19, 22, 14, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
                22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
                22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
                22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
                22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 21, 22, 21, 22, 22, 22, 21, 22, 22);

        int tableLength = table.length;
        stringEncodeTable = new int[tableLength];
        characterTable = table;
        characterMasks = new int[8];
        int[] var3 = new int[33];
        int var4 = 0;

        for (int var5 = 0; tableLength > var5; ++var5) {
            byte var6 = table[var5];
            if (var6 != 0) {
                int var7 = 1 << 32 - var6;
                int var8 = var3[var6];
                stringEncodeTable[var5] = var8;
                int var9;
                int var10;
                int var11;
                int var12;
                if ((var7 & var8) != 0) {
                    var9 = var3[var6 - 1];
                } else {
                    for (var10 = var6 - 1; var10 >= 1; --var10) {
                        var11 = var3[var10];
                        if (var8 != var11) {
                            break;
                        }

                        var12 = 1 << 32 - var10;
                        if ((var11 & var12) != 0) {
                            var3[var10] = var3[var10 - 1];
                            break;
                        }

                        var3[var10] = var12 | var11; //bitwiseOr(var12, var11);
                    }

                    var9 = var8 | var7;
                }

                var3[var6] = var9;

                for (var10 = var6 + 1; var10 <= 32; ++var10) {
                    if (var3[var10] == var8) {
                        var3[var10] = var9;
                    }
                }

                var10 = 0;

                for (var11 = 0; var6 > var11; ++var11) {
                    var12 = Integer.MIN_VALUE >>> var11;
                    if ((var12 & var8) == 0) {
                        ++var10;
                    } else {
                        if (characterMasks[var10] == 0) {
                            characterMasks[var10] = var4;
                        }

                        var10 = characterMasks[var10];
                    }

                    if (characterMasks.length <= var10) {
                        int[] var13 = new int[characterMasks.length * 2];

                        for (int var14 = 0; var14 < characterMasks.length; ++var14) {
                            var13[var14] = characterMasks[var14];
                        }

                        characterMasks = var13;
                    }

                    var12 >>>= 1;
                }

                if (var10 >= var4) {
                    var4 = var10 + 1;
                }

                characterMasks[var10] = ~var5;
            }
        }
    }

    public static final String getEncryptedString(Buffer src) {
        final int limit = 32767;
        try {
            int count = src.readSmart08_16();
            if (count > limit) {
                count = limit;
            }
            byte[] dest = new byte[count];
            src.offset += decryptString(src.dataBuffer, dest, 0, src.offset, -1, count);
            return getStringFromBytes(dest, 0, count);
        } catch (Exception var6) {
            return "Cabbage";
        }
    }

    public static final int putEncryptedString(Buffer dest, String src) {
        int oldHead = dest.offset;
        byte[] data = stringToBytes(src);
        dest.putSmart08_16(data.length);
        dest.offset += encryptString(data.length, dest.dataBuffer, dest.offset, data);
        return dest.offset - oldHead;

    }

    public static int putStringIntoBytes(CharSequence charseq, int seqoff, int seqlen, byte[] buf, int bufoff) {
        int j1 = seqlen - seqoff;
        for (int k1 = 0; k1 < j1; k1++) {
            char c = charseq.charAt(seqoff + k1);
            if (c > 0 && c < '\200' || c >= '\240' && c <= '\377') {
                buf[bufoff + k1] = (byte) c;
                continue;
            }
            if (c == '\u20AC') {
                buf[bufoff + k1] = -128;
                continue;
            }
            if (c == '\u201A') {
                buf[bufoff + k1] = -126;
                continue;
            }
            if (c == '\u0192') {
                buf[bufoff + k1] = -125;
                continue;
            }
            if (c == '\u201E') {
                buf[bufoff + k1] = -124;
                continue;
            }
            if (c == '\u2026') {
                buf[bufoff + k1] = -123;
                continue;
            }
            if (c == '\u2020') {
                buf[bufoff + k1] = -122;
                continue;
            }
            if (c == '\u2021') {
                buf[bufoff + k1] = -121;
                continue;
            }
            if (c == '\u02C6') {
                buf[bufoff + k1] = -120;
                continue;
            }
            if (c == '\u2030') {
                buf[bufoff + k1] = -119;
                continue;
            }
            if (c == '\u0160') {
                buf[bufoff + k1] = -118;
                continue;
            }
            if (c == '\u2039') {
                buf[bufoff + k1] = -117;
                continue;
            }
            if (c == '\u0152') {
                buf[bufoff + k1] = -116;
                continue;
            }
            if (c == '\u017D') {
                buf[bufoff + k1] = -114;
                continue;
            }
            if (c == '\u2018') {
                buf[bufoff + k1] = -111;
                continue;
            }
            if (c == '\u2019') {
                buf[bufoff + k1] = -110;
                continue;
            }
            if (c == '\u201C') {
                buf[bufoff + k1] = -109;
                continue;
            }
            if (c == '\u201D') {
                buf[bufoff + k1] = -108;
                continue;
            }
            if (c == '\u2022') {
                buf[bufoff + k1] = -107;
                continue;
            }
            if (c == '\u2013') {
                buf[bufoff + k1] = -106;
                continue;
            }
            if (c == '\u2014') {
                buf[bufoff + k1] = -105;
                continue;
            }
            if (c == '\u02DC') {
                buf[bufoff + k1] = -104;
                continue;
            }
            if (c == '\u2122') {
                buf[bufoff + k1] = -103;
                continue;
            }
            if (c == '\u0161') {
                buf[bufoff + k1] = -102;
                continue;
            }
            if (c == '\u203A') {
                buf[bufoff + k1] = -101;
                continue;
            }
            if (c == '\u0153') {
                buf[bufoff + k1] = -100;
                continue;
            }
            if (c == '\u017E') {
                buf[bufoff + k1] = -98;
                continue;
            }
            if (c == '\u0178')
                buf[bufoff + k1] = -97;
            else
                buf[bufoff + k1] = '?';
        }
        return j1;
    }

    public static byte[] stringToBytes(String str) {
        int strlen = str.length();
        byte[] buf = new byte[strlen];
        for (int i = 0; i < strlen; i++) {
            char c = str.charAt(i);
            if (c > 0 && c < '\200' || c >= '\240' && c <= '\377') {
                buf[i] = (byte) c;
                continue;
            }
            if (c == '\u20AC') {
                buf[i] = -128;
                continue;
            }
            if (c == '\u201A') {
                buf[i] = -126;
                continue;
            }
            if (c == '\u0192') {
                buf[i] = -125;
                continue;
            }
            if (c == '\u201E') {
                buf[i] = -124;
                continue;
            }
            if (c == '\u2026') {
                buf[i] = -123;
                continue;
            }
            if (c == '\u2020') {
                buf[i] = -122;
                continue;
            }
            if (c == '\u2021') {
                buf[i] = -121;
                continue;
            }
            if (c == '\u02C6') {
                buf[i] = -120;
                continue;
            }
            if (c == '\u2030') {
                buf[i] = -119;
                continue;
            }
            if (c == '\u0160') {
                buf[i] = -118;
                continue;
            }
            if (c == '\u2039') {
                buf[i] = -117;
                continue;
            }
            if (c == '\u0152') {
                buf[i] = -116;
                continue;
            }
            if (c == '\u017D') {
                buf[i] = -114;
                continue;
            }
            if (c == '\u2018') {
                buf[i] = -111;
                continue;
            }
            if (c == '\u2019') {
                buf[i] = -110;
                continue;
            }
            if (c == '\u201C') {
                buf[i] = -109;
                continue;
            }
            if (c == '\u201D') {
                buf[i] = -108;
                continue;
            }
            if (c == '\u2022') {
                buf[i] = -107;
                continue;
            }
            if (c == '\u2013') {
                buf[i] = -106;
                continue;
            }
            if (c == '\u2014') {
                buf[i] = -105;
                continue;
            }
            if (c == '\u02DC') {
                buf[i] = -104;
                continue;
            }
            if (c == '\u2122') {
                buf[+i] = -103;
                continue;
            }
            if (c == '\u0161') {
                buf[i] = -102;
                continue;
            }
            if (c == '\u203A') {
                buf[i] = -101;
                continue;
            }
            if (c == '\u0153') {
                buf[i] = -100;
                continue;
            }
            if (c == '\u017E') {
                buf[i] = -98;
                continue;
            }
            if (c == '\u0178')
                buf[i] = -97;
            else
                buf[i] = '?';
        }

        return buf;
    }

    public static final String getStringFromBytes(byte[] src, int offset, int count) {
        char[] dest = new char[count];
        int dh = 0;

        for (int i = 0; i < count; ++i) {
            int codepoint = 255 & src[offset + i];
            if (codepoint != 0) {
                if (codepoint >= 128 && codepoint < 160) {
                    char c = specialCharLookup[codepoint - 128];
                    if (c == 0) {
                        c = '?';
                    }
                    codepoint = c;
                }
                dest[dh++] = (char) codepoint;
            }
        }
        return new String(dest, 0, dh);
    }


    public static final int encryptString(byte[] stringData, int stringArrayLen) {
        int writerPos = 0;
        int value = 0;
        int left = 0;
        stringArrayLen += left;

        byte[] destBuffer = new byte[1555];
        int curWriterPos;
        for (curWriterPos = writerPos << 3; left < stringArrayLen; ++left) {

            int strUnicode = stringData[left] & 255;
            int encodeInt = stringEncodeTable[strUnicode];
            byte encodeLength = characterTable[strUnicode];
            if (encodeLength == 0) {
                throw new RuntimeException("" + strUnicode);
            }

            int destOffset = curWriterPos >> 3;
            int offset = curWriterPos & 7;
            value &= -offset >> 31;

            int totalLength = destOffset + (encodeLength + offset - 1 >> 3);

            curWriterPos += encodeLength;
            offset += 24;
            destBuffer[destOffset] = (byte) (value = (value | (encodeInt >>> offset))); //bitwiseOr(value, encodeInt >>> offset));
            if (destOffset < totalLength) {
                ++destOffset;
                offset -= 8;
                destBuffer[destOffset] = (byte) (value = encodeInt >>> offset);

                if (destOffset < totalLength) {
                    ++destOffset;
                    offset -= 8;
                    destBuffer[destOffset] = (byte) (value = encodeInt >>> offset);

                    if (destOffset < totalLength) {
                        ++destOffset;
                        offset -= 8;
                        destBuffer[destOffset] = (byte) (value = encodeInt >>> offset);

                        if (destOffset < totalLength) {
                            ++destOffset;
                            offset -= 8;
                            destBuffer[destOffset] = (byte) (value = encodeInt << -offset);
                        }
                    }
                }
            }
        }

        return (curWriterPos + 7 >> 3) - writerPos;
    }

    public static final int encryptString(int length, byte[] data, int destWriterPos, byte[] stringData) {
        int value = 0;
        int left = 0;
        length += left;

        int curWriterPos;
        for (curWriterPos = destWriterPos << 3; left < length; ++left) {
            int strUnicode = stringData[left] & 255;
            int encodeInt = stringEncodeTable[strUnicode];
            byte encodeLength = characterTable[strUnicode];
            if (encodeLength == 0) {
                throw new RuntimeException("" + strUnicode);
            }

            int destOffset = curWriterPos >> 3;
            int offset = curWriterPos & 7;
            value &= -offset >> 31;

            int totalLength = destOffset + (encodeLength + offset - 1 >> 3);

            curWriterPos += encodeLength;
            offset += 24;
            data[destOffset] = (byte) (value = (value | (encodeInt >>> offset))); //bitwiseOr(value, encodeInt >>> offset));
            if (destOffset < totalLength) {
                ++destOffset;
                offset -= 8;
                data[destOffset] = (byte) (value = encodeInt >>> offset);

                if (destOffset < totalLength) {
                    ++destOffset;
                    offset -= 8;
                    data[destOffset] = (byte) (value = encodeInt >>> offset);

                    if (destOffset < totalLength) {
                        ++destOffset;
                        offset -= 8;
                        data[destOffset] = (byte) (value = encodeInt >>> offset);

                        if (destOffset < totalLength) {
                            ++destOffset;
                            offset -= 8;
                            data[destOffset] = (byte) (value = encodeInt << -offset);
                        }
                    }
                }
            }
        }

        return (curWriterPos + 7 >> 3) - destWriterPos;
    }

    public static int bitwiseOr(int var0, int var1) {
        return var0 | var1;
    }

    final static int decryptString(byte[] src, byte[] dest, int destOffset, int srcOffset, int dummy, int count) {
        if (count == 0) {
            return 0;
        } else {
            int pos = 0;
            count += destOffset;

            int readerIndex = srcOffset;

            while (true) {
                byte srcValue = src[readerIndex];
                if (srcValue >= 0) {
                    ++pos;
                } else {
                    pos = characterMasks[pos];
                }

                int value;
                if ((value = characterMasks[pos]) < 0) {
                    dest[destOffset++] = (byte) (~value);
                    if (destOffset >= count) {
                        break;
                    }

                    pos = 0;
                }

                if ((64 & srcValue) != 0) {
                    pos = characterMasks[pos];
                } else {
                    ++pos;
                }

                if ((value = characterMasks[pos]) < 0) {
                    dest[destOffset++] = (byte) (~value);
                    if (count <= destOffset) {
                        break;
                    }

                    pos = 0;
                }

                if ((srcValue & 32) == 0) {
                    ++pos;
                } else {
                    pos = characterMasks[pos];
                }

                if ((value = characterMasks[pos]) < 0) {
                    dest[destOffset++] = (byte) (~value);
                    if (count <= destOffset) {
                        break;
                    }

                    pos = 0;
                }

                if ((16 & srcValue) != 0) {
                    pos = characterMasks[pos];
                } else {
                    ++pos;
                }

                if ((value = characterMasks[pos]) < 0) {
                    dest[destOffset++] = (byte) (~value);
                    if (destOffset >= count) {
                        break;
                    }

                    pos = 0;
                }

                if ((srcValue & 8) != 0) {
                    pos = characterMasks[pos];
                } else {
                    ++pos;
                }

                if ((value = characterMasks[pos]) < 0) {
                    dest[destOffset++] = (byte) (~value);
                    if (destOffset >= count) {
                        break;
                    }

                    pos = 0;
                }

                if ((4 & srcValue) != 0) {
                    pos = characterMasks[pos];
                } else {
                    ++pos;
                }

                if ((value = characterMasks[pos]) < 0) {
                    dest[destOffset++] = (byte) (~value);
                    if (destOffset >= count) {
                        break;
                    }

                    pos = 0;
                }

                if ((2 & srcValue) == 0) {
                    ++pos;
                } else {
                    pos = characterMasks[pos];
                }

                if ((value = characterMasks[pos]) < 0) {
                    dest[destOffset++] = (byte) (~value);
                    if (destOffset >= count) {
                        break;
                    }

                    pos = 0;
                }

                if ((1 & srcValue) != 0) {
                    pos = characterMasks[pos];
                } else {
                    ++pos;
                }

                if ((value = characterMasks[pos]) < 0) {
                    dest[destOffset++] = (byte) (~value);
                    if (destOffset >= count) {
                        break;
                    }

                    pos = 0;
                }

                ++readerIndex;
            }

            return 1 - srcOffset + readerIndex;
        }
    }

    public static byte[] asByte(int... is) {
        byte[] res = new byte[is.length];
        for (int i = 0; i < res.length; i++)
            res[i] = (byte) is[i];
        return res;
    }
}
