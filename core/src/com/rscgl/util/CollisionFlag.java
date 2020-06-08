package com.rscgl.util;

public class CollisionFlag {
    public static final int WALL_EAST = 2;
    public static final int WALL_NORTH = 1;
    public static final int WALL_SOUTH = 4;
    public static final int WALL_WEST = 8;

    public static final int WALL_NORTH_EAST = WALL_NORTH | WALL_EAST;
    public static final int WALL_NORTH_WEST = WALL_NORTH | WALL_WEST;
    public static final int WALL_SOUTH_EAST = WALL_SOUTH | WALL_EAST;
    public static final int WALL_SOUTH_WEST = WALL_SOUTH | WALL_WEST;

    public static final int DIAGONAL_A = 16;
    public static final int DIAGONAL_B = 32;
    public static final int FULL_BLOCK_C = 64;
    public static final int FULL_BLOCK = DIAGONAL_A | DIAGONAL_B | FULL_BLOCK_C;

    public static final int OBJECT = 128;

    public static final int WEST_BLOCKED = FULL_BLOCK | WALL_WEST;
    public static final int SOUTH_BLOCKED = FULL_BLOCK | WALL_SOUTH;
    public static final int NORTH_BLOCKED = FULL_BLOCK | WALL_NORTH;
    public static final int EAST_BLOCKED = FULL_BLOCK | WALL_EAST;

    public static final int SOUTH_EAST_BLOCKED = FULL_BLOCK | WALL_SOUTH_EAST;
    public static final int SOUTH_WEST_BLOCKED = FULL_BLOCK | WALL_SOUTH_WEST;
    public static final int NORTH_EAST_BLOCKED = FULL_BLOCK | WALL_NORTH_EAST;
    public static final int NORTH_WEST_BLOCKED = FULL_BLOCK | WALL_NORTH_WEST;

    public static final int SOURCE_SOUTH = 1;
    public static final int SOURCE_WEST = 2;
    public static final int SOURCE_NORTH = 4;
    public static final int SOURCE_EAST = 8;

    public static final int SOURCE_NORTH_EAST = SOURCE_NORTH | SOURCE_EAST;
    public static final int SOURCE_NORTH_WEST = SOURCE_NORTH | SOURCE_WEST;
    public static final int SOURCE_SOUTH_EAST = SOURCE_SOUTH | SOURCE_EAST;
    public static final int SOURCE_SOUTH_WEST = SOURCE_SOUTH | SOURCE_WEST;
}

