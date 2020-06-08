package com.rscgl.net;

public interface PacketHandler {

    void handlePacket(int opcode, int length, Buffer packet);

    int[] opcodes();
}
