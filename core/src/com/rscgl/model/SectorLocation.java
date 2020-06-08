package com.rscgl.model;

import com.rscgl.assets.model.Sector;

public class SectorLocation {

    private final int x;
    private final int y;
    private final int plane;

    public static SectorLocation create(int x, int y, int plane) {
        return new SectorLocation(x, y, plane);
    }

    public static SectorLocation fromWorld(float wX, float wY, int plane) {
        return create((int) Math.floor(wX / Sector.SIZE), (int) Math.floor(wY / Sector.SIZE), plane);
    }

    public static SectorLocation getWorld(int x, int y, int plane) {
        int sX = (int) Math.floor(x / Sector.SIZE);
        int sY = (int) Math.floor(y / Sector.SIZE);
        int tX = (x - (sX * Sector.SIZE));
        int tY = (y - (sY * Sector.SIZE));
        return SectorLocation.fromWorld(x, y, plane);
    }

    public static boolean localInBounds(float x, float y) {
        return x >= 0 && y >= 0 && x < Sector.SIZE && y < Sector.SIZE;
    }

    private SectorLocation(final int sX, final int sY, final int plane) {
        this.x = sX;
        this.y = sY;
        this.plane = plane;
    }

    public boolean containsWorld(float wX, float wY) {
        return Math.floor(wX / Sector.SIZE) == x && Math.floor(wY / Sector.SIZE) == y;
    }

    @Override
    public final boolean equals(final Object o) {
        if (o instanceof SectorLocation) {
            SectorLocation l = (SectorLocation) o;
            return l.x == x && l.y == y && l.plane == plane;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return (x * 73856093) ^ (y * 19349663);
    }

    public final int sectorX() {
        return x;
    }

    public final int sectorY() {
        return y;
    }

    public int getPlane() {
        return plane;
    }

    @Override
    public final String toString() {
        return "Sector [x=" + x + ", y=" + y + " plane="+ plane+"]";
    }

    public int worldX() {
        return x * Sector.SIZE;
    }

    public int worldY() {
        return y * Sector.SIZE;
    }

    public boolean inNegativeSpace() {
        return x < 0 || y < 0;
    }
}
