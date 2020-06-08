package com.rscgl.net;

public class PacketBuffer extends Buffer {

    public int mouseMoved;
    private int packetStart;

    public boolean isConstructingPacket() {
        return constructingPacket;
    }

    private boolean constructingPacket;

    public PacketBuffer(int size) {
        super(size);
        resetOffset();
    }

    public void newPacket(int opcode) {
        if (constructingPacket)
            throw new IllegalStateException("Another packet construction in process!");

        offset = this.packetStart + 2;
        putByte(opcode);
        putByte(mouseMoved);
        constructingPacket = true;
    }

    public final void finishPacket() {
        if (!constructingPacket)
            throw new IllegalStateException("packet construction not in process!");

        int packetLen = offset - this.packetStart - 2;
        this.dataBuffer[this.packetStart] = (byte) (packetLen >> 8);
        this.dataBuffer[this.packetStart + 1] = (byte) packetLen;
        this.packetStart = offset;
        constructingPacket = false;
    }

    public void resetOffset() {
        this.packetStart = 0;
        this.offset = 3;
    }

}
