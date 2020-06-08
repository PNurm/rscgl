package com.rscgl.assets.model;

import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.TileDef;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.rscgl.GameWorld.TSIZE;

/**
 * A representation of one tile within our world map
 */
public class Tile {
	/**
	 * The elevation of this tile
	 */
	public byte height = 0;

	/**
	 * The texture ID of this tile
	 */
	public byte colour = 0;

	/**
	 * The texture ID of the roof of this tile
	 */
	public byte roof = 0;

	/**
	 * The texture ID of any horizontal wall on this tile
	 */
	public byte wallHorizontal = 0;

	/**
	 * The texture ID of any vertical wall on this tile
	 */
	public byte wallVertical = 0;

	/**
	 * The ID of any diagonal walls on this tile
	 */
	public int wallDiagonal = 0;

	/**
	 * The overlay texture ID
	 */
	public byte texture = 0;
	public byte objectDir;

    /**
	 * Writes the Tile raw data into a ByteBuffer
	 */
	public ByteBuffer pack() throws IOException {
		ByteBuffer out = ByteBuffer.allocate(10);

		out.put(height);
		out.put(colour);
		out.put(texture);
		out.put(roof);

		out.put(wallHorizontal);
		out.put(wallVertical);
		out.putInt(wallDiagonal);

		out.flip();
		return out;
	}

	/**
	 * Create a new tile from raw data packed into the given ByteBuffer
	 */
	public static Tile unpack(ByteBuffer in) throws IOException {
		if (in.remaining() < 10) {
			throw new IOException("Provided buffer too short");
		}
		Tile tile = new Tile();

		tile.height = in.get();
		tile.colour = in.get();
		tile.texture = in.get();
		tile.roof = in.get();
		tile.wallHorizontal = in.get();
		tile.wallVertical = in.get();
		tile.wallDiagonal = in.getInt();

		return tile;
	}

	public byte getRoof() {
		return roof;
	}

	public byte getWallHorizontal() {
		return wallHorizontal;
	}


	public byte getWallVertical() {
		return wallVertical;
	}


	public int getWallDiagonal() {
		return wallDiagonal;
	}

	public int getColor() {
		return colour & 0xff;
	}

	public int getTexture() {
		return texture & 0xFF;
	}

	public float getHeight() {
		float elevationVal = (float) (height & 0xff);
		return (elevationVal * 3f / 128f) * TSIZE;
	}

	public TileDef getDef() {
		return RSCache.TILES[texture -1];//Cache.getTileDef(texture - 1);
	}

	public int getTexture(int defaultVal) {
		if (texture == 0) {
			return defaultVal;
		}
		return getDef().getTexture();
	}

	public int isPath() {
		if(texture == 0) {
			return -1;
		}
		int type = getDef().getType();
		return type == 2 ? 1 : 0;
	}
}