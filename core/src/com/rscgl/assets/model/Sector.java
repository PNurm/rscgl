package com.rscgl.assets.model;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Sector {
    public static final short SIZE = 48;

    public Tile[] getTiles() {
        return tiles;
    }

    /**
     * An array containing all the tiles within this Sector
     */
    private Tile[] tiles;

    public int tiles() {
        return tiles.length;
    }
    /**
     * Creates a new Sector full of blank tiles
     */
    public Sector() {
        tiles = new Tile[Sector.SIZE * Sector.SIZE];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new Tile();
        }
    }

    /**
     * Sets the the Tile at the given coords
     */
    public void setTile(int x, int y, Tile t) {
        setTile(x * Sector.SIZE + y, t);
    }

    /**
     * Sets the Tile at the given index
     */
    public void setTile(int i, Tile t) {
        tiles[i] = t;
    }

    /**
     * Gets the Tile at the given coords
     */
    public Tile getTile(int x, int y) {
        return getTile(x * Sector.SIZE + y);
    }

    /**
     * Gets the Tile at the given index
     */
    public Tile getTile(int i) {
        if(i < 0 || i >= tiles.length) {
            System.out.println("Null tile " + i);
            return new Tile();
        }

        return tiles[i];
    }

    /**
     * Writes the Sector raw data into a ByteBuffer
     */
    public ByteBuffer pack() throws IOException {
        ByteBuffer out = ByteBuffer.allocate(10 * tiles.length);

        for (int i = 0; i < tiles.length; i++) {
            out.put(tiles[i].pack());
        }

        out.flip();
        return out;
    }

    /**
     * Create a new Sector from raw data packed into the given ByteBuffer
     */
    public static Sector unpack(ByteBuffer in) throws IOException {
        int length = Sector.SIZE * Sector.SIZE;
        if (in.remaining() < (10 * length)) {
            throw new IOException("Provided buffer too short");
        }
        Sector sector = new Sector();

        for (int i = 0; i < length; i++) {
            sector.setTile(i, Tile.unpack(in));
        }

        return sector;
    }

    public void flipTiles() {
        Tile[][] flip = new Tile[SIZE][SIZE];

        for(int x = 0; x < SIZE;x++) {
            for(int y = 0; y < SIZE;y++) {
                Tile t = getTile(x * Sector.SIZE + y);
                flip[x][y] = t;
            }
        }

        for(int i = 0; i < (flip.length / 2); i++) {
            Tile[] temp = flip[i];
            flip[i] = flip[flip.length - i - 1];
            flip[flip.length - i - 1] = temp;
        }

        for(int x = 0; x < SIZE;x++) {
            for(int y = 0; y < SIZE;y++) {
                setTile(x, y, flip[x][y]);
            }
        }
    }
}