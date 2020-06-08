package com.rscgl.assets.model;

public class Sprite {

    public int id = -1;
    public boolean translate;

    public int imgX = 0;
    public int imgY = 0;
    /**
     * The pixels in this image, maps to colors in imgColorLookup array
     */
    public byte[] imgPixels;
    /**
     * The colours in this sprites image, supports up to 255 colours.
     */
    public int[] imgColorLookup;
    /**
     * Unpacked pixels using imgPixels and imgColorLookup
     */
    public int[] pixelData;

    public int spriteWidth = 0;
    public int spriteHeight = 0;


    public int[] getSpritePixels() {
        if(pixelData != null)
            return pixelData;

        byte[] pixels = getImgPixels(); //spriteColorIndices[spriteId];
        int[] colours = getImgColorLookup(); //spriteColors[spriteId];
        int[] spritePixels = new int[imgWidth * imgHeight];
        for (int i = 0; i < imgWidth * imgHeight; i++) {
            int colour = colours[pixels[i] & 0xff];
            if (colour == 0)
                colour = 1;
            else if (colour == 0xff00ff)
                colour = 0;
            spritePixels[i] = colour;
        }
        setPixelData(spritePixels);
        return spritePixels;
    }

    public int imgWidth;
    public int imgHeight;

    public byte[] getImgPixels() {
        return imgPixels;
    }

    public int[] getImgColorLookup() {
        return imgColorLookup;
    }


    @Override
    public String toString() {
        return id + "";
    }

    public void setPixelData(int[] spritePixels) {
        this.pixelData = spritePixels;
    }
}