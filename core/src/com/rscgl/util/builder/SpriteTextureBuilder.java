package com.rscgl.util.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.rscgl.assets.model.Sprite;

public class SpriteTextureBuilder {

    protected int screenHeight;
    protected int screenWidth;
    protected int[] pixels;

    protected int maxWidth;
    protected int maxHeight;

    private int screenZeroY = 0;
    private int screenZeroX = 0;

    private Pixmap pixmap;

    public SpriteTextureBuilder(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.maxWidth = width;
        this.maxHeight = height;
        this.pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        reset();
    }


    public Pixmap toPixmap() {
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        for (int x = 0; x < pixmap.getWidth(); x++) {
            for (int y = 0; y < pixmap.getHeight(); y++) {
                int rgb = pixels[x + y * screenWidth];
                pixmap.setColor(0, 0, 0, 0);
                if (rgb != 0xff00ff && rgb != 0xFFFFFF) {
                    pixmap.setColor(toColor(rgb));
                }
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    private static Color toColor(int color) {
        float red = ((color >> 16) & 0xff);
        float green = ((color >> 8) & 0xff);
        float blue = ((color) & 0xff);
        return new Color(red / 255F, green / 255F, blue / 255F, 1f);
    }

    public Sprite buildSprite(int sprite, int x, int y, int width, int height) {
        Sprite s = new Sprite();
        s.id = sprite;
        s.imgX = s.imgY = 0;
        s.imgWidth = width;
        s.imgHeight = height;
        s.spriteWidth = width;
        s.spriteHeight = height;

        int area = width * height;
        int pixel = 0;

        s.pixelData = new int[area];
        for (int yy = y; yy < y + height; yy++) {
            for (int xx = x; xx < x + width; xx++)
                s.pixelData[pixel++] = pixels[xx + yy * screenWidth];
        }
        return s;
    }

    public void drawSprite(Sprite sprite, int x, int y) {
        if (sprite.translate) {
            x += sprite.imgX;
            y += sprite.imgY;
        }
        int rY = x + y * screenWidth;
        int rX = 0;
        int height = sprite.imgWidth;
        int width = sprite.imgHeight;
        int w2 = screenWidth - width;
        int h2 = 0;
        if (y < 0) {
            int j2 = 0 - y;
            height -= j2;
            y = 0;
            rX += j2 * width;
            rY += j2 * screenWidth;
        }
        if (y + height >= screenHeight)
            height -= ((y + height) - screenHeight) + 1;
        if (x < 0) {
            int k2 = 0 - x;
            width -= k2;
            x = 0;
            rX += k2;
            rY += k2;
            h2 += k2;
            w2 += k2;
        }
        if (x + width >= screenWidth) {
            int l2 = ((x + width) - screenWidth) + 1;
            width -= l2;
            h2 += l2;
            w2 += l2;
        }
        if (width <= 0 || height <= 0)
            return;
        byte inc = 1;

        if(sprite.pixelData == null) {
            drawSprite(pixels, sprite.imgPixels, sprite.imgColorLookup, rX, rY, width, height, w2, h2, inc);
        } else {
            drawSprite(pixels, sprite.pixelData, 0, rX, rY, width, height, w2, h2, inc);
        }
    }
    private void drawSprite(int dest[], int src[], int i, int srcPos, int destPos, int width, int height,
                            int j1, int k1, int yInc) {
        int i2 = -(width >> 2);
        width = -(width & 3);
        for (int j2 = -height; j2 < 0; j2 += yInc) {
            for (int k2 = i2; k2 < 0; k2++) {
                i = src[srcPos++];
                if (i != 0)
                    dest[destPos++] = i;
                else
                    destPos++;
                i = src[srcPos++];
                if (i != 0)
                    dest[destPos++] = i;
                else
                    destPos++;
                i = src[srcPos++];
                if (i != 0)
                    dest[destPos++] = i;
                else
                    destPos++;
                i = src[srcPos++];
                if (i != 0)
                    dest[destPos++] = i;
                else
                    destPos++;
            }

            for (int l2 = width; l2 < 0; l2++) {
                i = src[srcPos++];
                if (i != 0)
                    dest[destPos++] = i;
                else
                    destPos++;
            }

            destPos += j1;
            srcPos += k1;
        }

    }
    private void drawSprite(int target[], byte colourIdx[], int colours[], int srcPos, int destPos, int width, int height,
                            int w2, int h2, int rowInc) {
        int l1 = -(width >> 2);
        width = -(width & 3);
        for (int i2 = -height; i2 < 0; i2 += rowInc) {
            for (int j2 = l1; j2 < 0; j2++) {
                byte byte0 = colourIdx[srcPos++];
                if (byte0 != 0)
                    target[destPos++] = colours[byte0 & 0xff];
                else
                    destPos++;
                byte0 = colourIdx[srcPos++];
                if (byte0 != 0)
                    target[destPos++] = colours[byte0 & 0xff];
                else
                    destPos++;
                byte0 = colourIdx[srcPos++];
                if (byte0 != 0)
                    target[destPos++] = colours[byte0 & 0xff];
                else
                    destPos++;
                byte0 = colourIdx[srcPos++];
                if (byte0 != 0)
                    target[destPos++] = colours[byte0 & 0xff];
                else
                    destPos++;
            }

            for (int k2 = width; k2 < 0; k2++) {
                byte byte1 = colourIdx[srcPos++];
                if (byte1 != 0)
                    target[destPos++] = colours[byte1 & 0xff];
                else
                    destPos++;
            }

            destPos += w2;
            srcPos += h2;
        }
    }

    public final void draw(Sprite sprite, int x, int y, int width, int height, int colorMask, int colorMask2,
                           boolean mirrorX, int topPixelSkew) {
        if (colorMask2 == 0) {
            colorMask2 = 0xFFFFFF;
        }

        if (colorMask == 0) {
            colorMask = 0xFFFFFF;
        }
        int spriteWidth = sprite.imgWidth;
        int spriteHeight = sprite.imgHeight;
        int scaleX = (spriteWidth << 16) / width;
        int scaleY = (spriteHeight << 16) / height;
        int srcStartX = 0;
        int srcStartY = 0;
        int destFirstColumn = topPixelSkew << 16;
        int destColumnSkewPerRow = -(topPixelSkew << 16) / height;

        if (sprite.translate) {
            int spriteOffsetX = sprite.imgX;
            int spriteOffsetY = sprite.imgY;
            int boundWidth = sprite.spriteWidth;
            int boundHeight = sprite.spriteHeight;
            if (boundWidth == 0 || boundHeight == 0) {
                return;
            }
            scaleX = (boundWidth << 16) / width;
            scaleY = (boundHeight << 16) / height;

            if (mirrorX) {
                spriteOffsetX = boundWidth - sprite.imgWidth - spriteOffsetX;
            }
            x += (boundWidth + spriteOffsetX * width - 1) / boundWidth;
            if (spriteOffsetX * width % boundWidth != 0) {
                srcStartX = (boundWidth - width * spriteOffsetX % boundWidth << 16) / width;
            }

            int drawOffsetY = (spriteOffsetY * height + boundHeight - 1) / boundHeight;
            y += drawOffsetY;
            destFirstColumn += drawOffsetY * destColumnSkewPerRow;
            if (spriteOffsetY * height % boundHeight != 0) {
                srcStartY = (boundHeight - height * spriteOffsetY % boundHeight << 16) / height;
            }

            width = (scaleX + ((sprite.imgWidth << 16) - (srcStartX + 1))) / scaleX;
            height = ((sprite.imgHeight << 16) - srcStartY - (1 - scaleY)) / scaleY;
        }
        int skipEveryOther;
        int destRowHead = this.screenWidth * y;
        destFirstColumn += x << 16;
        if (y < this.screenZeroY) {
            skipEveryOther = this.screenZeroY - y;
            destFirstColumn += destColumnSkewPerRow * skipEveryOther;
            height -= skipEveryOther;
            srcStartY += skipEveryOther * scaleY;
            destRowHead += this.screenWidth * skipEveryOther;
            y = this.screenZeroY;
        }
        if (y + height >= this.maxHeight) {
            height -= 1 + y + height - this.maxHeight;
        }
        //skipEveryOther = destRowHead / this.screenWidth & dummy;

        skipEveryOther = 2;

         if (mirrorX) {
            plotTransScale(pixels, sprite.getSpritePixels(), width,
                    destColumnSkewPerRow, destFirstColumn, colorMask2, scaleY, -scaleX,
                    (sprite.imgWidth << 16) - srcStartX - 1, skipEveryOther, srcStartY, spriteWidth,
                    colorMask, height, destRowHead);
        } else {
            plotTransScale(pixels, sprite.getSpritePixels(), width,
                    destColumnSkewPerRow, destFirstColumn, colorMask2, scaleY, scaleX, srcStartX,
                    skipEveryOther, srcStartY, spriteWidth, colorMask, height, destRowHead);
        }
    }

    private final void plotTransScale(int[] dest, int[] src, int destColumnCount,
                                      int destColumnSkewPerRow, int destFirstColumn, int mask2, int scaleY, int scaleX,
                                      int srcStartX, int skipEveryOther, int srcStartY, int srcWidth, int mask1, int destHeight,
                                      int destRowHead) {

        int mask1_red = mask1 >> 16 & 255;
        int mask1_green = mask1 >> 8 & 255;
        int mask1_blue = mask1 & 255;

        int mask2_red = mask2 >> 16 & 255;
        int mask2_green = mask2 >> 8 & 255;
        int mask2_blue = 255 & mask2;

        try {
            int var27 = srcStartX;

            for (int row = -destHeight; row < 0; ++row) {
                int var29 = (srcStartY >> 16) * srcWidth;
                int pixelStart = destFirstColumn >> 16;
                int pixelEnd = destColumnCount;
                int pixel;
                if (this.screenZeroX > pixelStart) {
                    pixel = this.screenZeroX - pixelStart;
                    pixelEnd = destColumnCount - pixel;
                    srcStartX += pixel * scaleX;
                    pixelStart = this.screenZeroX;
                }

                if (this.maxWidth <= pixelStart + pixelEnd) {
                    pixel = pixelStart - this.maxWidth + pixelEnd;
                    pixelEnd -= pixel;
                }

                skipEveryOther = 1 - skipEveryOther;
                if (skipEveryOther != 0) {
                    for (pixel = pixelStart; pixelStart + pixelEnd > pixel; ++pixel) {
                        int spritePixelData = src[var29 + (srcStartX >> 16)];
                        if (spritePixelData != 0) {
                            int pixelRed = 255 & spritePixelData >> 16;
                            int pixelGreen = 255 & spritePixelData >> 8;
                            int pixelBlue = spritePixelData & 255;
                            if (pixelRed == pixelGreen && pixelBlue == pixelGreen) {
                                dest[pixel + destRowHead] = (pixelBlue * mask1_blue >> 8)
                                        + (mask1_green * pixelGreen >> 8 << 8)
                                        + (pixelRed * mask1_red >> 8 << 16);
                            } else if (pixelRed == 255 && pixelGreen == pixelBlue) {
                                dest[pixel + destRowHead] = (mask2_blue * pixelBlue >> 8) + (pixelRed * mask2_red >> 8 << 16) + (pixelGreen * mask2_green >> 8 << 8);
                            } else {
                                dest[pixel + destRowHead] = spritePixelData;
                            }
                        }
                        srcStartX += scaleX;
                    }
                }

                srcStartY += scaleY;
                srcStartX = var27;
                destRowHead += this.screenWidth;
                destFirstColumn += destColumnSkewPerRow;
            }
        } catch (Exception var33) {
            System.out.println("error in transparent sprite plot routine");
        }
    }

    public void reset() {
        this.pixels = new int[screenWidth * screenHeight];
        for (int i = 0; i < pixels.length; i++)
            pixels[i] = 0xff00ff;
    }

    public void drawBox(int x, int y, int width, int height, int colour) {
        if (x + width > screenWidth)
            width = screenWidth - x;
        if (y + height > screenHeight)
            height = screenHeight - y;

        int skip = screenWidth - width;// wat
        int pixel = x + y * screenWidth;
        for (int i = -height; i < 0; i += 1) {
            for (int i2 = -width; i2 < 0; i2++)
                pixels[pixel++] = colour;

            pixel += skip;
        }
    }

    public int[] getPixels() {
        return pixels;
    }
}
