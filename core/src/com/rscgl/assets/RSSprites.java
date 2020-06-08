package com.rscgl.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rscgl.Config;
import com.rscgl.assets.model.Sprite;
import com.rscgl.assets.util.Utility;
import com.rscgl.util.builder.SpriteTextureBuilder;

public class RSSprites {

    public static int MEDIA = 2000;
    public static int UTIL = MEDIA + 100;
    public static int ITEM = UTIL + 50;
    public static int LOGO = ITEM + 1000;
    public static int PROJECTILE = LOGO + 10;
    public static int TEXTURE = PROJECTILE + 50;

    public static Sprite[] sprites = new Sprite[4000];
    public static Sprite[] textures = new Sprite[100];

    public static void loadSprite(int spriteId, byte[] spriteData, byte[] indexData, int frameCount) {
        int indexOff = Utility.getUnsignedShort(spriteData, 0);
        int width = Utility.getUnsignedShort(indexData, indexOff);
        indexOff += 2;
        int height = Utility.getUnsignedShort(indexData, indexOff);
        indexOff += 2;

        int colorCount = indexData[indexOff++] & 255;
        int[] colours = new int[colorCount];
        colours[0] = 0xff00ff;
        for (int i = 0; i < colorCount - 1; i++) {
            colours[i + 1] =
                    ((indexData[indexOff] & 0xff) << 16)
                            + ((indexData[indexOff + 1] & 0xff) << 8)
                            + (indexData[indexOff + 2] & 0xff);
            indexOff += 3;
        }
        int spriteOff = 2;
        for (int id = spriteId; id < spriteId + frameCount; id++) {
            sprites[id] = new Sprite();
            sprites[id].id = id;

            sprites[id].imgX = indexData[indexOff++] & 0xff;
            sprites[id].imgY = indexData[indexOff++] & 0xff;

            int imageWidth = Utility.getUnsignedShort(indexData, indexOff);
            indexOff += 2;
            int imageHeight = Utility.getUnsignedShort(indexData, indexOff);
            indexOff += 2;

            int imageType = indexData[indexOff++] & 0xff;
            int imageSize = imageWidth * imageHeight;

            //Maps to an index in the int color array
            sprites[id].imgPixels = new byte[imageSize];
            //The color lookup table used by this sprite set.
            sprites[id].imgColorLookup = colours;

            //The bounding box the sprite is in
            sprites[id].spriteWidth = width;
            sprites[id].spriteHeight = height;

            sprites[id].imgWidth = imageWidth;
            sprites[id].imgHeight = imageHeight;

            //The position in the bounding box
            sprites[id].translate = sprites[id].imgX != 0 || sprites[id].imgY != 0;

            if (imageType == 0) { //Sprite is read in one stream
                for (int j = 0; j < imageSize; j++) {
                    sprites[id].imgPixels[j] = spriteData[spriteOff++];
                    if (sprites[id].imgPixels[j] == 0) {
                        sprites[id].translate = true;
                    }
                }
            } else if (imageType == 1) { //Sprite is read line per line.
                for (int x = 0; x < imageWidth; x++) {
                    for (int y = 0; y < imageHeight; y++) {
                        sprites[id].imgPixels[x + y * imageWidth] = spriteData[spriteOff++];
                        if (sprites[id].imgPixels[x + y * imageWidth] == 0) {
                            sprites[id].translate = true;
                        }
                    }
                }
            }
        }
    }

    public static void loadTextures() {
        FileHandle fileHandle = Gdx.files.local(Config.CACHE_DIR + "textures17.jag");
        byte[] textureData = Utility.readDataFile(fileHandle);
        if (textureData == null) {
            throw new NullPointerException("Failed to load textures");
        }

        SpriteTextureBuilder textureBuilder = new SpriteTextureBuilder(128, 128);
        byte[] indexData = Utility.uncompressData("index.dat", 0, textureData, null);
        for (int i = 0; i < RSCache.TEXTURE_COUNT; i++) {
            String name = RSCache.TEXTURES[i].textureName;

            byte[] spriteData = Utility.uncompressData(name + ".dat", 0, textureData, null);

            //Draw pink background for texture transparency.
            textureBuilder.drawBox(0, 0, 128, 128, 0xff00ff);

            //Load the texture image to array
            loadSprite(TEXTURE, spriteData, indexData, 1);
            //Draw the texture
            textureBuilder.drawSprite(sprites[TEXTURE], 0, 0);

            int width = sprites[TEXTURE].spriteWidth;
            String overlay = RSCache.TEXTURES[i].overlayName;
            if (overlay != null && overlay.length() > 0) {
                //Did they draw textures on top of each other ?
                byte[] overlayData = Utility.uncompressData(overlay + ".dat", 0, textureData, null);
                //Draw the overlay sprite on top
                loadSprite(TEXTURE, overlayData, indexData, 1);
                textureBuilder.drawSprite(sprites[TEXTURE], 0, 0);
            }

            textures[i] = textureBuilder.buildSprite(i, 0, 0, width, width);
            for (int j = 0; j < width * width; j++) {
                //Convert red background into pink background, again for transparency.
                if (textures[i].pixelData[j] == 65280) { //converted to hex. int: 65280
                    textures[i].pixelData[j] = 0;
                }
            }
            //Unload the raw pixel format to save memory.
            createColorTable(textures[i]);
        }
    }

    public static void loadEntities() {
        byte[] archive = null;
        byte[] index = null;
        archive = loadDataFile("entity24.jag", "people and monsters", 30);
        if (archive == null) {
            throw new NullPointerException("Failed to load entities");
        }
        index = Utility.uncompressData("index.dat", 0, archive);
        byte[] archiveMembers = null;
        byte[] indexMembers = null;
        archiveMembers = loadDataFile("entity24.mem", "member graphics", 45);
        if (archiveMembers == null) {
            throw new NullPointerException("Failed to load entities (members) ");
        }
        indexMembers = Utility.uncompressData("index.dat", 0, archiveMembers);

        int frameCount = 0;
        int animZero = 0;
        int animHead = animZero;

        primary:
        for (int j = 0; j < RSCache.ANIMATION_COUNT; j++) {
            String animName = RSCache.ANIMATIONS[j].name;//GameData.animationName[j];
            for (int k = 0; k < j; k++) {
                if (!RSCache.ANIMATIONS[k].name.equalsIgnoreCase(animName))
                    continue;
                RSCache.ANIMATIONS[j].spriteIndex = RSCache.ANIMATIONS[k].spriteIndex;
                continue primary;
            }

            byte[] dat = Utility.uncompressData(animName + ".dat", 0, archive);
            byte[] indexData = index;
            if (dat == null) {
                dat = Utility.uncompressData(animName + ".dat", 0, archiveMembers);
                indexData = indexMembers;
            }
            if (dat != null) {
                loadSprite(animHead, dat, indexData, 15);
                frameCount += 15;
                if (RSCache.ANIMATIONS[j].combatFrames) {
                    byte[] combatData = Utility.uncompressData(animName + "a.dat", 0, archive);
                    byte[] combatIndex = index;
                    if (combatData == null) {
                        combatData = Utility.uncompressData(animName + "a.dat", 0, archiveMembers);
                        combatIndex = indexMembers;
                    }
                    loadSprite(animHead + 15, combatData, combatIndex, 3);
                    frameCount += 3;
                }
                if (RSCache.ANIMATIONS[j].walkFrames) {
                    byte[] stepData = Utility.uncompressData(animName + "f.dat", 0, archive);
                    byte[] stepIndex = index;
                    if (stepData == null) {
                        stepData = Utility.uncompressData(animName + "f.dat", 0, archiveMembers);
                        stepIndex = indexMembers;
                    }
                    loadSprite(animHead + 18, stepData, stepIndex, 9);
                    frameCount += 9;
                }
                if (RSCache.ANIMATIONS[j].genderModel != 0) {
                    for (int sprite = animHead; sprite < animHead + 27; sprite++) {
                        unpackColorTable(sprite);
                    }
                }
            }
            RSCache.ANIMATIONS[j].spriteIndex = animHead;
            animHead += 27;
        }

        System.out.println("Loaded: " + frameCount + " frames of animation");
    }

    private static byte[] loadDataFile(String file, String status, int percent) {
        FileHandle fileHandle = Gdx.files.local(Config.CACHE_DIR + file);
        byte[] archive = Utility.readDataFile(fileHandle);
        return archive;
    }

    public static void loadMedia() {
        byte[] media = loadDataFile("media58.jag", "2d graphics", 20);
        if (media == null) {
            throw new NullPointerException("Failed to load media");
        }
        byte[] indexData = Utility.uncompressData("index.dat", 0, media);

        loadSprite(MEDIA, Utility.uncompressData("inv1.dat", 0, media), indexData, 1);
        loadSprite(MEDIA + 1, Utility.uncompressData("inv2.dat", 0, media), indexData, 6);
        loadSprite(MEDIA + 9, Utility.uncompressData("bubble.dat", 0, media), indexData, 1);
        loadSprite(MEDIA + 10, Utility.uncompressData("runescape.dat", 0, media), indexData, 1);
        loadSprite(MEDIA + 11, Utility.uncompressData("splat.dat", 0, media), indexData, 3);
        loadSprite(MEDIA + 14, Utility.uncompressData("icon.dat", 0, media), indexData, 8);
        loadSprite(MEDIA + 22, Utility.uncompressData("hbar.dat", 0, media), indexData, 1);
        loadSprite(MEDIA + 23, Utility.uncompressData("hbar2.dat", 0, media), indexData, 1);
        loadSprite(MEDIA + 24, Utility.uncompressData("compass.dat", 0, media), indexData, 1);
        loadSprite(MEDIA + 25, Utility.uncompressData("buttons.dat", 0, media), indexData, 2);
        loadSprite(UTIL, Utility.uncompressData("scrollbar.dat", 0, media), indexData, 2);
        loadSprite(UTIL + 2, Utility.uncompressData("corners.dat", 0, media), indexData, 4);
        loadSprite(UTIL + 6, Utility.uncompressData("arrows.dat", 0, media), indexData, 2);
        loadSprite(PROJECTILE, Utility.uncompressData("projectile.dat", 0, media), indexData, RSCache.projectileSprite);

        int itemSprite = RSCache.itemSpriteCount;
        for (int j = 1; itemSprite > 0; j++) {
            int loadCount = itemSprite;
            itemSprite -= 30;
            if (loadCount > 30)
                loadCount = 30;
            loadSprite(ITEM + (j - 1) * 30, Utility.uncompressData("objects" + j + ".dat", 0, media), indexData, loadCount);
        }

        unpackColorTable(MEDIA);
        unpackColorTable(MEDIA + 9);
        for (int i = 11; i <= 26; i++)
            unpackColorTable(MEDIA + i);
        for (int i = 0; i < RSCache.projectileSprite; i++)
            unpackColorTable(PROJECTILE + i);
        for (int i = 0; i < RSCache.itemSpriteCount; i++)
            unpackColorTable(ITEM + i);

    }

    /**
     * Loads sprite from 8 bit format into 32 bit raw pixel array.
     *
     * @param spriteId
     */
    public static void unpackColorTable(int spriteId) {
        if(sprites[spriteId] == null || sprites[spriteId].imgColorLookup == null) {
            return;
        }

        Sprite sprite = sprites[spriteId];
        int size = sprite.imgWidth * sprite.imgHeight; //spriteImageHeight[spriteId];
        byte[] pixels = sprite.getImgPixels(); //spriteColorIndices[spriteId];
        int[] colours = sprite.getImgColorLookup(); //spriteColors[spriteId];
        int[] spritePixels = new int[size];
        for (int i = 0; i < size; i++) {
            int colour = colours[pixels[i] & 0xff];
            if (colour == 0)
                colour = 1;
            else if (colour == 0xff00ff)
                colour = 0;
            spritePixels[i] = colour;
        }
        sprite.setPixelData(spritePixels);
        sprite.imgPixels = null;
        sprite.imgColorLookup = null;
        /*spriteRawPixels[spriteId] = spritePixels;
        spriteColorIndices[spriteId] = null;
        spriteColors[spriteId] = null;*/
    }

    /**
     * Interesting method to reduce colours in image
     * Saving this here as I refactored it.
     */
    public static void createColorTable(Sprite sprite) {
        int[] colorCounts = new int[32768];
        int[] spritePixels = sprite.pixelData;

        int imageSize = sprite.imgWidth * sprite.imgHeight;

        for (int i = 0; i < imageSize; i++) {
            int pixel = spritePixels[i];
            int value = ((pixel & 0xf80000) >> 9) + ((pixel & 0xf800) >> 6) + ((pixel & 0xf8) >> 3);
            colorCounts[value]++;
        }

        int[] colors = new int[256];
        colors[0] = 0xff00ff;

        int[] tmp = new int[256];
        for (int i = 0; i < 32768; i++) {
            int colorCount = colorCounts[i];

            if (colorCount > tmp[255]) {

                for (int j = 1; j < 256; j++) {
                    if (colorCount <= tmp[j]) {
                        continue;
                    }
                    for (int k = 255; k > j; k--) {
                        colors[k] = colors[k - 1];
                        tmp[k] = tmp[k - 1];
                    }
                    colors[j] = ((i & 0x7c00) << 9) + ((i & 0x3e0) << 6) + ((i & 0x1f) << 3) + 0x40404;
                    tmp[j] = colorCount;
                    break;
                }

            }
            colorCounts[i] = -1;
        }

        byte[] pixels = new byte[imageSize];
        for (int i = 0; i < imageSize; i++) {
            int pixel = spritePixels[i];

            int color = ((pixel & 0xf80000) >> 9) + ((pixel & 0xf800) >> 6) + ((pixel & 0xf8) >> 3);
            int colorIndex = colorCounts[color];

            if (colorIndex == -1) {
                int previous = 0x3b9ac9ff;
                int R = pixel >> 16 & 0xff;
                int G = pixel >> 8 & 0xff;
                int B = pixel & 0xff;

                for (int j = 0; j < 256; j++) {
                    int otherColor = colors[j];
                    int otherR = otherColor >> 16 & 0xff;
                    int otherG = otherColor >> 8 & 0xff;
                    int otherB = otherColor & 0xff;
                    int result = (R - otherR) * (R - otherR) + (G - otherG) * (G - otherG) + (B - otherB) * (B - otherB);
                    if (result < previous) {
                        previous = result;
                        colorIndex = j;
                    }
                }

                colorCounts[color] = colorIndex;
            }

            pixels[i] = (byte) colorIndex;
        }
        sprite.imgPixels = pixels;
        sprite.imgColorLookup = colors;
        sprite.pixelData = null;
    }

}
