package com.rscgl.assets.model;

import com.rscgl.assets.util.Utility;

public class RSArchiveModel {

    public static final int TRANSPARENT = 12345678;
    private final int[] faceDiffuseLight;
    public final int faceCount;

    public int getIndexCount() {
        return indexCount;
    }

    private int indexCount;
    public String name = "";

    public float[] vertY;
    public float[] vertZ;
    public float[] vertX;

    public final int vertexCount;
    public final int[][] faceIndices;
    public final int[] faceIndexCount;
    public final int[] faceTextureFront;
    public final int[] faceTextureBack;

    public RSArchiveModel(int i, int j) {
        this.vertX = new float[i];
        this.vertY = new float[i];
        this.vertZ = new float[i];

        this.faceIndices = new int[j][];
        this.faceIndexCount = new int[j];
        this.faceTextureFront = new int[j];
        this.faceTextureBack = new int[j];
        this.faceDiffuseLight = new int[j];
        vertexCount = i;
        faceCount = j;
    }

    public RSArchiveModel(String modelName, byte[] data, int offset) {
        this.name = modelName;
        int vertexCount = Utility.getUnsignedShort(data, offset);
        offset += 2;
        int faceCount = Utility.getUnsignedShort(data, offset);
        offset += 2;

        this.vertX = new float[vertexCount];
        this.vertY = new float[vertexCount];
        this.vertZ = new float[vertexCount];

        this.faceIndices = new int[faceCount][];
        this.faceIndexCount = new int[faceCount];
        this.faceTextureFront = new int[faceCount];
        this.faceTextureBack = new int[faceCount];
        this.faceDiffuseLight = new int[faceCount];

        for (int j = 0; vertexCount > j; ++j) {
            float val = Utility.getSignedShort(data, offset);
            vertX[j] = (val / 128f) * 3f;
            offset += 2;
        }
        for (int j = 0; j < vertexCount; ++j) {
            float val = Utility.getSignedShort(data, offset);
            vertY[j] = (val / 128f) * 3f;
            offset += 2;
        }
        for (int j = 0; vertexCount > j; ++j) {
            float val = Utility.getSignedShort(data, offset);
            vertZ[j] = (val / 128f) * 3f;
            offset += 2;
        }

        this.vertexCount = vertexCount;

        for (int j = 0; j < faceCount; ++j) {
            this.faceIndexCount[j] = data[offset++] & 0xff;
        }

        for (int j = 0; j < faceCount; ++j) {
            this.faceTextureFront[j] = Utility.getSignedShort(data, offset);
            if (this.faceTextureFront[j] == 32767) {
                this.faceTextureFront[j] = this.TRANSPARENT;
            }

            offset += 2;
        }

        for (int j = 0; faceCount > j; ++j) {
            this.faceTextureBack[j] = Utility.getSignedShort(data, offset);
            offset += 2;
            if (32767 == this.faceTextureBack[j]) {
                this.faceTextureBack[j] = this.TRANSPARENT;
            }
        }

        int i;
        for (int j = 0; j < faceCount; ++j) {
            i = 255 & data[offset++];
            if (i != 0) {
                this.faceDiffuseLight[j] = this.TRANSPARENT;
            } else {
                this.faceDiffuseLight[j] = 0;
            }
        }

        for (int j = 0; faceCount > j; ++j) {
            this.faceIndices[j] = new int[this.faceIndexCount[j]];

            for (i = 0; this.faceIndexCount[j] > i; ++i) {
                if (vertexCount < 256) {
                    this.faceIndices[j][i] = (data[offset++] & 0xff);
                } else {
                    this.faceIndices[j][i] = Utility.getUnsignedShort(data, offset);
                    offset += 2;
                }
                this.indexCount += this.faceIndices[j][i];
            }
        }
        this.faceCount = faceCount;
    }
}
