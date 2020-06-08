package com.rscgl.assets;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rscgl.Config;
import com.rscgl.assets.model.Sector;
import com.rscgl.assets.model.Tile;
import com.rscgl.assets.util.Utility;
import com.rscgl.model.SectorLocation;

import java.io.*;
import java.util.HashMap;


public class RSMap {


    private static byte[] freeHeightmap;
    private static byte[] freeMapData;

    private static byte[] membersHeightmap;
    private static byte[] membersMapData;

    private final static int SECTOR_TILE_COUNT = 2304;

    public static Sector loadSection(int x, int y, int plane) {
        Sector sector = new Sector();
        Tile[] tiles = sector.getTiles();
        String sectorName = "m" + plane + x / 10 + x % 10 + y / 10 + y % 10;
        try {
            if (freeHeightmap != null) {
                byte[] heightmapData = Utility.uncompressData(sectorName + ".hei", 0, freeHeightmap);
                if (heightmapData == null && membersHeightmap != null)
                    heightmapData = Utility.uncompressData(sectorName + ".hei", 0, membersHeightmap);

                if (heightmapData != null && heightmapData.length > 0) {
                    int idx = 0;
                    int previous = 0;

                    for (int i = 0; i < SECTOR_TILE_COUNT; ) {
                        int val = heightmapData[idx++] & 0xff;
                        if (val < 128) {
                            tiles[i++].height = (byte) val;
                            //tileHeight[i++] = (byte) val;
                            previous = val;
                        }
                        if (val >= 128) {
                            for (int j = 0; j < val - 128; j++) {
                                //tileHeight[j++] = (byte) defaultVal;
                                tiles[i++].height = (byte) previous;
                            }
                        }
                    }

                    previous = 64;
                    for (int tileY = 0; tileY < 48; tileY++) {
                        for (int tileX = 0; tileX < 48; tileX++) {
                            previous = tiles[tileX * 48 + tileY].height + previous & 0x7f;
                            tiles[tileX * 48 + tileY].height = (byte) (previous * 2);
                            //tileHeight[tileX * 48 + tileY] = (byte) (defaultVal * 2);
                        }
                    }

                    previous = 0;
                    for (int i = 0; i < SECTOR_TILE_COUNT; ) {
                        int value = heightmapData[idx++] & 0xff;
                        if (value < 128) {
                            tiles[i++].colour = (byte) (value);
                            //tileColour[i++] = (byte) value;
                            previous = value;
                        }
                        if (value >= 128) {
                            for (int j = 0; j < value - 128; j++) {
                                tiles[i++].colour = (byte) (previous);
                                //tileColour[j++] = (byte) defaultVal;
                            }
                        }
                    }

                    previous = 35;
                    for (int tileY = 0; tileY < 48; tileY++) {
                        for (int tileX = 0; tileX < 48; tileX++) {
                            //defaultVal = tileColour[tileX * 48 + tileY] + defaultVal & 0x7f;
                            //tileColour[tileX * 48 + tileY] = (byte) (defaultVal * 2);
                            previous = tiles[tileX * 48 + tileY].colour + previous & 0x7f;
                            tiles[tileX * 48 + tileY].colour = (byte) (previous * 2);
                        }
                    }
                } else {
                    for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                        tiles[i++].height = 0;
                        tiles[i++].colour = 0;

                        //tileHeight[i] = 0;
                        //tileColour[i] = 0;
                    }
                }

                byte[] data = Utility.uncompressData(sectorName + ".dat", 0, freeMapData);
                if (data == null && membersMapData != null) {
                    data = Utility.uncompressData(sectorName + ".dat", 0, membersMapData);
                }
                if (data == null || data.length == 0) {
                    throw new IOException();
                }
                int offset = 0;
                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    //wallsVertical[i] = membersMapData[offset++];
                    tiles[i].wallHorizontal = data[offset++];
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    //wallsHorizontal[i] = membersMapData[offset++];
                    tiles[i].wallVertical = data[offset++];
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    //wallsDiagonal[i] = membersMapData[offset++] & 0xff;
                    tiles[i].wallDiagonal = data[offset++] & 0xff;
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    int val = data[offset++] & 0xff;
                    if (val > 0) {
                        //wallsDiagonal[i] = val + 12000;
                        tiles[i].wallDiagonal = val + 12000;
                    }
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; ) {
                    int val = data[offset++] & 0xff;
                    if (val < 128) {
                        tiles[i++].roof = (byte) val;
                        // tileRoof[i++] = (byte) val;
                    } else {
                        for (int j = 0; j < val - 128; j++) {
                            tiles[i++].roof = 0;
                            //tileRoof[j++] = 0;
                        }
                    }
                }

                int defaultVal = 0;
                for (int i = 0; i < SECTOR_TILE_COUNT; ) {
                    int value = data[offset++] & 0xff;
                    if (value < 128) {
                        //tileDecoration[i++] = (byte) value;
                        tiles[i++].texture = (byte) value;
                        defaultVal = value;
                    } else {
                        for (int j = 0; j < value - 128; j++) {
                            tiles[i++].texture = (byte) defaultVal;
                            //tileDecoration[j++] = (byte) defaultVal;
                        }
                    }
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; ) {
                    int value = data[offset++] & 0xff;
                    if (value < 128) {
                        //tileDirection[i++] = (byte) value;
                        tiles[i++].objectDir = (byte) value;
                    } else {
                        for (int j = 0; j < value - 128; j++) {
                            //tileDirection[j++] = 0;
                            tiles[i++].objectDir = 0;
                        }
                    }
                }

                data = Utility.uncompressData(sectorName + ".loc", 0, freeMapData);
                if (data != null && data.length > 0) {
                    offset = 0;
                    for (int i = 0; i < SECTOR_TILE_COUNT; ) {
                        int value = data[offset++] & 0xff;
                        if (value < 128) {
                            //wallsDiagonal[i++] = value + 48000;
                            tiles[i++].wallDiagonal = value + 48000;
                        } else {
                            i += value - 128;
                        }
                    }
                    return sector;
                }
            } else {
                byte[] mapData = new byte[20736];
                Utility.readFully("../gamedata/maps/" + sectorName + ".jm", mapData, 20736);
                int value = 0;
                int offset = 0;
                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    value = value + mapData[offset++] & 0xff;
                    //tileHeight[i] = (byte) value;
                    tiles[i].height = (byte) value;
                }

                value = 0;
                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    value = value + mapData[offset++] & 0xff;
                    tiles[i].colour = (byte) value;
                    //tileColour[i] = (byte) value;
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    //wallsVertical[i] = mapData[offset++];
                    tiles[i].wallVertical = mapData[offset++];
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    //wallsHorizontal[i] = mapData[offset++];
                    tiles[i].wallHorizontal = mapData[offset++];
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    //wallsDiagonal[i] = (mapData[offset] & 0xff) * 256 + (mapData[offset + 1] & 0xff);
                    tiles[i].wallDiagonal = (mapData[offset] & 0xff) * 256 + (mapData[offset + 1] & 0xff);
                    offset += 2;
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    //tileRoof[tile] = mapData[offset++];
                    tiles[i].roof = mapData[offset++];
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    //tileDecoration[tile] = mapData[offset++];
                    tiles[i].texture = mapData[offset++];
                }

                for (int i = 0; i < SECTOR_TILE_COUNT; i++) {
                    //tileDirection[i] = mapData[offset++];
                    tiles[i].objectDir = mapData[offset++];
                }
            }
            return sector;
        } catch (IOException ex) {
        }
        for (int i = 0; i < SECTOR_TILE_COUNT; i++) {

            tiles[i].height = 0;
            tiles[i].colour = 0;
            tiles[i].texture = 0;

            tiles[i].roof = 0;
            tiles[i].wallVertical = 0;
            tiles[i].wallHorizontal = 0;
            tiles[i].wallDiagonal = 0;
            tiles[i].objectDir = 0;

            if (plane == 0) {
                tiles[i].texture = -6;
                //tileDecoration[i] = -6;
            }
            if (plane == 3) {
                tiles[i].texture = 8;
                //tileDecoration[i] = 8;
            }
        }
        return sector;
    }

    public static void loadLand() {
        FileHandle mapFile = Gdx.files.local(Config.CACHE_DIR + "maps64.jag");
        FileHandle membersMapFile = Gdx.files.local(Config.CACHE_DIR + "maps64.mem");

        freeMapData = Utility.readDataFile(mapFile);
        membersMapData = Utility.readDataFile(membersMapFile);

        FileHandle landscapeFile = Gdx.files.local(Config.CACHE_DIR + "land64.jag");
        FileHandle membersLandscapeFile = Gdx.files.local(Config.CACHE_DIR + "land64.mem");

        freeHeightmap = Utility.readDataFile(landscapeFile);
        membersHeightmap = Utility.readDataFile(membersLandscapeFile);
    }

    public static Tile getWorldTile(int x, int y, int plane) {
        int sX = (int) Math.floor(x / Sector.SIZE);
        int sY = (int) Math.floor(y / Sector.SIZE);
        int tX = (x - (sX * Sector.SIZE));
        int tY = (y - (sY * Sector.SIZE));

        Sector sector = get(SectorLocation.fromWorld(x, y, plane));
        return sector.getTile(tX, tY);
    }

    private static HashMap<SectorLocation, Sector> loadedMap = new HashMap<SectorLocation, Sector>();

    public static Sector get(SectorLocation location) {
        if (!loadedMap.containsKey(location)) {
            loadedMap.put(location, loadSection(location.sectorX(), location.sectorY(), location.getPlane()));
        }
        return loadedMap.get(location);
    }
}
