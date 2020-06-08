package com.rscgl.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.PixmapPackerIO;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.rscgl.assets.def.ItemDef;
import com.rscgl.assets.def.TileDef;
import com.rscgl.assets.model.RSArchiveModel;
import com.rscgl.assets.model.RSMaterial;
import com.rscgl.assets.model.RSModelLoader;
import com.rscgl.assets.model.Sprite;
import com.rscgl.model.RSMesh;
import com.rscgl.ui.util.ColorUtil;
import com.rscgl.util.builder.ItemTextureBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Assets {

    public static Assets inst;

    private TextureAtlas textureAtlas;
    private TextureAtlas interfaceAtlas;

    public Sprite getAnim(int animSpriteID, int id) {
        return RSSprites.sprites[animSpriteID + id];
    }

    public TextureRegion getInterSprite(int id) {
        if(interfaceAtlas == null) {

        }
        TextureAtlas.AtlasRegion textureAtlas = interfaceAtlas.findRegion("" + id);
        if(textureAtlas == null) {
            System.out.println("Interface " + id + " not found");
        }
        return textureAtlas;
    }

    public TextureRegion getMaterial(int id) {
        if (id >= 0) {
            return textureAtlas.findRegion("texture:" + id);
        } else {
            return textureAtlas.findRegion("color:" + id);
        }
    }

    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    private TextureAtlas generateAtlas(FileHandle f) {
        PixmapPacker packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 1, true);

        HashMap<String, Color> coloursToPack = new HashMap<String, Color>();

        coloursToPack.put("color:" + 0, RSMaterial.getColor(0));
        for (int i = 0; i < 256; i++) {
            int color = RSMaterial.colorToResource[i];
            coloursToPack.put("color:" + color, RSMaterial.convert(color));
        }
        for (TileDef t : RSCache.TILES) {
            if (t.colour < 0) {
                Color c = RSMaterial.convert(t.colour);
                coloursToPack.put("color:" + t.colour, c);
            }
        }
        for (RSArchiveModel j : RSModels.getModels()) {
            for (int face = 0; face < j.faceCount; face++) {
                int frontTexture = j.faceTextureFront[face];
                int backTexture = j.faceTextureBack[face];
                if (frontTexture < 0) {
                    Color c = RSMaterial.convert(frontTexture);
                    coloursToPack.put("color:" + frontTexture, c);
                }
                if (backTexture < 0) {
                    Color c = RSMaterial.convert(backTexture);
                    coloursToPack.put("color:" + backTexture, c);
                }
            }
        }

        Pixmap pixmap = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
        for (Map.Entry<String, Color> colors : coloursToPack.entrySet()) {
            pixmap.setColor(colors.getValue());
            pixmap.fill();
            if (packer.getRect(colors.getKey()) == null)
                packer.pack(colors.getKey(), pixmap);
        }
        pixmap.dispose();

        for (int i = 0; i < RSCache.TEXTURE_COUNT; i++) {
            Sprite sprite = RSSprites.textures[i];
            if (sprite == null) {
                continue;
            }
            int[] pixels = sprite.getSpritePixels();
            pixmap = new Pixmap(sprite.imgWidth, sprite.imgHeight, Pixmap.Format.RGBA8888);
            for (int x = 0; x < pixmap.getWidth(); x++) {
                for (int y = 0; y < pixmap.getHeight(); y++) {
                    int rgb = pixels[x + y * sprite.imgWidth];
                    if (rgb != 0xff00ff) {
                        pixmap.setColor(ColorUtil.toColor(rgb));
                    } else {
                        pixmap.setColor(0, 0, 0, 0);
                    }
                    pixmap.drawPixel(x, y);
                }
            }
            if (packer.getRect("texture:" + i) == null)
                packer.pack("texture:" + i, pixmap);
            pixmap.dispose();
        }

        PixmapPackerIO io = new PixmapPackerIO();
        try {
            io.save(f, packer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packer.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
    }

    public static void init() {
        inst = new Assets();
    }

    public void loadRSCache() {
        RSCache.loadData();

        RSSprites.loadMedia();
        RSSprites.loadEntities();
        RSSprites.loadTextures();

        RSModels.loadModels();
        RSMap.loadLand();
    }

    public RSMesh loadModel(int id) {
        return RSModelLoader.loadModel(RSModels.getModels()[id]);
    }

    public int getTextureColor(int resource) {
        return 0;
        //SpritePack.getPack("textures").getSpriteSet("").getSprite(resource).getSpritePixels()[0];
    }

    private ItemTextureBuilder itemSpriteBuilder = new ItemTextureBuilder();

    public Texture getItemSprite(ItemDef def) {
        Sprite sprite = RSSprites.sprites[RSSprites.ITEM + def.sprite];
        itemSpriteBuilder.reset();
        itemSpriteBuilder.draw(sprite, 0, 0, 48, 32, def.colour, 0, def.pictureMask2, 255, false, 0);
        Pixmap pixmap = itemSpriteBuilder.toPixmap();
        Texture texture = new Texture(pixmap);
        return texture;
    }

    public void loadGL() {
        generateInterfaceAtlas();
        FileHandle f = Gdx.files.local("atlas-world");
        if (!f.exists()) {
            System.out.println("Generating world texture atlas...");
            textureAtlas = generateAtlas(f);
        } else {
            textureAtlas = new TextureAtlas(f);
        }
    }

    private void generateInterfaceAtlas() {
        PixmapPacker packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 2, false);
        for (int id = RSSprites.MEDIA; id < RSSprites.UTIL; id++) {
            Sprite sprite = RSSprites.sprites[id];
            int offset = id - RSSprites.MEDIA;
            if (sprite == null)  {
                System.out.println(id + " is null");
                continue;
            }
            int[] pixels = sprite.getSpritePixels();
            Pixmap pixmap = new Pixmap(sprite.imgWidth, sprite.imgHeight, Pixmap.Format.RGBA8888);
            for (int x = 0; x < pixmap.getWidth(); x++) {
                for (int y = 0; y < pixmap.getHeight(); y++) {
                    int rgb = pixels[x + y * sprite.imgWidth];
                    if (rgb != 0x000000) {
                        pixmap.setColor(ColorUtil.toColor(rgb));
                    } else {
                        pixmap.setColor(0, 0, 0, 0);
                    }
                    pixmap.drawPixel(x, y);
                }
            }
            if (packer.getRect("" + offset) == null) {
                packer.pack("" + offset, pixmap);
            }
        }
        interfaceAtlas = packer.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        PixmapPackerIO io = new PixmapPackerIO();
        try {
            io.save(Gdx.files.local("interface-atlas"), packer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
