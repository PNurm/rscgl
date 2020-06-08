package com.rscgl.util.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.rscgl.assets.Assets;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.RSMap;
import com.rscgl.assets.def.TileDef;
import com.rscgl.assets.model.Sector;
import com.rscgl.assets.model.Tile;
import com.rscgl.assets.model.RSMaterial;
import com.rscgl.ui.util.ColorUtil;
import com.rscgl.model.SectorLocation;
import com.rscgl.GameWorld;
import com.rscgl.ui.onscreen.MinimapImage;

import static com.rscgl.assets.model.RSMaterial.TRANSPARENT;

public class MapImageBuilder {

    private final Color wallC = ColorUtil.toColor(6316128);
    private MinimapImage currentMap;
    private Sector[] sectors;

    public Pixmap flipPixmap(Pixmap src) {
        final int width = src.getWidth();
        final int height = src.getHeight();
        Pixmap flipped = new Pixmap(width, height, src.getFormat());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                flipped.drawPixel(x, y, src.getPixel(width - x - 1, y));
            }
        }
        src.dispose();
        return flipped;
    }


    public static final int WIDTH = 96;
    public static final int HEIGHT = 96;

    public MinimapImage getCurrentMap() {
        return currentMap;
    }

    public boolean buildMinimap(int x, int z, int plane) {
        int worldTileX = x + GameWorld.WORLD_WIDTH;
        int worldTileZ = z + GameWorld.WORLD_HEIGHT;
        if (getCurrentMap() != null && getCurrentMap().plane == GameWorld.currentPlane
                && getCurrentMap().bounds.contains(worldTileX, worldTileZ)) {
            return false;
        }
        int newSectorX = (worldTileX + 24) / 48;
        int newSectorZ = (worldTileZ + 24) / 48;

        MinimapImage image = new MinimapImage(
                (newSectorX * Sector.SIZE - Sector.SIZE) - GameWorld.WORLD_WIDTH,
                (newSectorZ * Sector.SIZE - Sector.SIZE) - GameWorld.WORLD_HEIGHT,
                GameWorld.currentPlane);
        image.bounds.set(
                newSectorX * Sector.SIZE - 32,
                newSectorZ * Sector.SIZE - 32,
                newSectorX * Sector.SIZE + 32,
                newSectorZ * Sector.SIZE + 32
        );
        sectors = new Sector[]{
                RSMap.get(SectorLocation.create(newSectorX - 1, newSectorZ - 1, plane)),
                RSMap.get(SectorLocation.create(newSectorX, newSectorZ - 1, plane)),
                RSMap.get(SectorLocation.create(newSectorX - 1, newSectorZ, plane)),
                RSMap.get(SectorLocation.create(newSectorX, newSectorZ, plane))
        };
        createMap(image);
        if(currentMap != null) {
            currentMap.dispose();
        }
        currentMap = image;
        return true;
    }

    private void createMap(MinimapImage image) {
        Pixmap p = image.image;
        for (int x = 0; x < WIDTH - 1; ++x)
            for (int z = 0; z < HEIGHT - 1; ++z) {
                RSMaterial material = getTileMaterials(x, z);
                drawTile(p, x, z, material.getType(), material.getDecorA(), material.getDecorB());
            }
        for (int x = 0; x < WIDTH - 1; ++x) {
            for (int z = 0; z < WIDTH - 1; ++z) {
                Tile t = lookupTile(x, z);
                int wall = t.getWallVertical();
                if (wall > 0
                        && (RSCache.WALLS[wall - 1].getInvisible() == 0)) {
                    drawVerticalWall(p, x, z);
                }

                wall = t.getWallHorizontal();
                if (wall > 0
                        && (RSCache.WALLS[wall - 1].getInvisible() == 0)) {
                    drawHorizontalWall(p, x, z);
                }

                wall = t.getWallDiagonal();
                if (wall > 0 && wall < 12000
                        && RSCache.WALLS[wall - 1].getInvisible() == 0) {
                    drawDiagonalWall(p, x, z);
                }

                if (wall > 12000 && wall < 24000 && (RSCache.WALLS[wall - 12001].getInvisible() == 0)) {
                    drawDiagonalWall2(p, x, z);
                }
            }
        }
        image.image = flipPixmap(p);
    }


    public Tile getTile(int x, int z) {
        if (x >= 0 && x < WIDTH && z >= 0 && z < HEIGHT) {
            byte sector = 0;
            if (x >= 48 && z < 48) {
                x = x - 48;
                sector = 1;
            } else if (x < 48 && z >= 48) {
                sector = 2;
                z = z - 48;
            } else if (x >= 48 && z >= 48) {
                sector = 3;
                z = z - 48;
                x = x - 48;
            }
            Tile t = sectors[sector].getTile(x, z);
            if (t != null) {
                return t;
            }
        }
        return new Tile();
    }

    public Tile lookupTile(int x, int z) {
        Tile tile = getTile(x, z);
        //
        if (tile.getTexture() == 250) {
            Tile xp1 = getTile(x + 1, z);
            Tile zp1 = getTile(x, z + 1);
            if (x == 47
                    && xp1.getTexture() != 250
                    && xp1.getTexture() != 2) {
                tile.texture = (byte) 9;
            } else if (z == 47
                    && zp1.getTexture() != 250
                    && zp1.getTexture() != 2) {
                tile.texture = (byte) 9;
            } else {
                tile.texture = (byte) 2;
            }
        }
        return tile;
    }

    private RSMaterial getTileMaterials(int x, int z) {
        //x-n-1 means x-negative-1 etc

        Tile t = lookupTile(x, z);
        Tile xn1 = lookupTile(x - 1, z);
        Tile zn1 = lookupTile(x, z - 1);
        Tile xp1 = lookupTile(x + 1, z);
        Tile zp1 = lookupTile(x, z + 1);

        int colorA = RSMaterial.colorToResource[t.getColor()];
        int colorB = colorA;
        int defaultVal = colorA;

        int splitDirection = 0;
        if (t.getTexture() > 0) {
            TileDef tileDef = RSCache.TILES[t.getTexture() - 1];
            int tileValue = tileDef.getType();
            int tileValue0or1 = tileValue != 2 ? 0 : 1; //isTileType2(x, z);
            colorA = colorB = tileDef.getTexture();//TextureID can also be colour if theres no texture image.
            if (tileValue == 4) {//Bridge
                colorA = 1;
                colorB = 1;
                if (t.getTexture() == 12) {
                    colorA = 31;
                    colorB = 31;
                }
            }
            if (tileValue == 5) {
                if (t.getWallDiagonal() > 0 && t.getWallDiagonal() < 24000)
                    if (xn1.getTexture(defaultVal) != TRANSPARENT
                            && zn1.getTexture(defaultVal) != TRANSPARENT) {
                        splitDirection = 0;
                        colorA = xn1.getTexture(defaultVal);
                    } else if (xp1.getTexture(defaultVal) != TRANSPARENT
                            && zp1.getTexture(defaultVal) != TRANSPARENT) {
                        colorB = xp1.getTexture(defaultVal);
                        splitDirection = 0;
                    } else if (xp1.getTexture(defaultVal) != TRANSPARENT
                            && zn1.getTexture(defaultVal) != TRANSPARENT) {
                        colorB = xp1.getTexture(defaultVal);
                        splitDirection = 1;
                    } else if (xn1.getTexture(defaultVal) != TRANSPARENT
                            && zp1.getTexture(defaultVal) != TRANSPARENT) {
                        splitDirection = 1;
                        colorA = xn1.getTexture(defaultVal);
                    }
            } else if (tileValue != 2 || t.getWallDiagonal() > 0 && t.getWallDiagonal() < 24000) {
                if (tileValue0or1 != xn1.isPath() && zn1.isPath() != tileValue0or1) {
                    colorA = defaultVal;
                    splitDirection = 0;
                } else if (tileValue0or1 != xp1.isPath() && zp1.isPath() != tileValue0or1) {
                    splitDirection = 0;
                    colorB = defaultVal;
                } else if (tileValue0or1 != xp1.isPath() && zn1.isPath() != tileValue0or1) {
                    colorB = defaultVal;
                    splitDirection = 1;
                } else if (tileValue0or1 != xn1.isPath() && tileValue0or1 != zp1.isPath()) {
                    colorA = defaultVal;
                    splitDirection = 1;
                }
            }
        }
        return new RSMaterial(colorB, colorA, splitDirection);
    }

    public void drawVerticalWall(Pixmap minimap, int x, int z) {
        minimap.setColor(wallC);
        minimap.drawLine(x * 3, z * 3, x * 3 + 3, z * 3);
    }

    public void drawHorizontalWall(Pixmap minimap, int x, int z) {
        minimap.setColor(wallC);
        minimap.drawLine(x * 3, z * 3, x * 3, z * 3 + 3);
    }

    public void drawDiagonalWall(Pixmap minimap, int x, int z) {
        minimap.setColor(wallC);
        minimap.drawPixel(x * 3, z * 3);
        minimap.drawPixel(1 + x * 3, 1 + z * 3);
        minimap.drawPixel(x * 3 + 2, 2 + z * 3);
    }

    public void drawDiagonalWall2(Pixmap minimap, int x, int z) {
        minimap.setColor(wallC);
        minimap.drawPixel(2 + x * 3, z * 3);
        minimap.drawPixel(x * 3 + 1, z * 3 + 1);
        minimap.drawPixel(x * 3, 2 + z * 3);
    }

    public void drawTile(Pixmap minimap, int x, int z, int splitDirection, int a, int b) {

        a = resourceToColor(a);
        a = a >> 1 & 0x7F7F7F;

        b = resourceToColor(b);
        b = (0xFEFEFF & b) >> 1;

        Color colorA = ColorUtil.toColor(a);
        Color colorB = ColorUtil.toColor(b);

        int mx = x * 3;
        int my = z * 3;
        if (splitDirection == 0) {
            minimap.setColor(colorA);
            minimap.drawLine(mx, my, mx + 3, my);
            minimap.drawLine(mx, my + 1, mx + 2, my + 1);
            minimap.drawLine(mx, my + 2, mx + 1, my + 2);

            minimap.setColor(colorB);
            minimap.drawLine(mx + 2, my + 1, mx + 3, my + 1);
            minimap.drawLine(mx + 1, my + 2, mx + 3, my + 2);
        } else if (splitDirection == 1) {
            minimap.setColor(colorB);
            minimap.drawLine(mx, my, mx + 3, my);
            minimap.drawLine(mx + 1, my + 1, mx + 3, my + 1);
            minimap.drawLine(mx + 2, my + 2, mx + 3, my + 2);

            minimap.setColor(colorA);
            minimap.drawLine(mx, my + 1, mx + 1, my + 1);
            minimap.drawLine(mx, my + 2, mx + 2, my + 2);
        }
    }

    final int resourceToColor(int resource) {
        if (resource == TRANSPARENT) {
            return 0;
        } else {
            if (resource >= 0) {
                return Assets.inst.getTextureColor(resource);
            } else if (resource < 0) {
                resource = -(resource + 1);
                int var3 = (resource & 0x7C00) >> 10;
                int var4 = (0x3E0 & resource) >> 5;
                int var5 = 0x1F & resource;
                return (var5 << 3) + (var4 << 11) + (var3 << 19);
            } else {
                return 0;
            }
        }
    }

}
