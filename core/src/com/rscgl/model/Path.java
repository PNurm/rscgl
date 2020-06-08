package com.rscgl.model;

import com.rscgl.Game;
import com.rscgl.util.CollisionFlag;

import java.util.ArrayList;

/**
 * Very bad
 */
public class Path {

    private final int lastStepX;
    private final int lastStepY;
    private final Waypoint[] waypoints;

    public Path(int lastX, int lastY, Waypoint[] waypoints) {
        this.lastStepX = lastX;
        this.lastStepY = lastY;
        this.waypoints = waypoints;
    }

    public int getLastStepX() {
        return lastStepX;
    }

    public int getLastStepY() {
        return lastStepY;
    }

    public Waypoint[] getWaypoints() {
        return waypoints;
    }
    public static class Waypoint {

        public Waypoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x, y;
    }
    
    private static int CollisionMapSize = 944;
    private static final int[][] pathFindSource = new int[CollisionMapSize][CollisionMapSize];
    private static final int[] pathX = new int[8000];
    private static final int[] pathZ = new int[8000];

    public static final Path generate(int startX, int startZ, int xLow, int xHigh, int zLow, int zHigh, boolean reachBorder) {
        for (int x = 0; x < 944; ++x)
            for (int y = 0; y < 944; ++y)
                pathFindSource[x][y] = 0;

        pathFindSource[startX][startZ] = 99;

        int b = 0;
        pathX[b] = startX;
        pathZ[b] = startZ;

        int openListRead = 0;
        int openListWrite = b + 1;
        int openListSize = 500;
        boolean pathFound = false;

        int x = startX;
        int z = startZ;
        while (openListRead != openListWrite) {
            x = pathX[openListRead];
            z = pathZ[openListRead];
            openListRead = (1 + openListRead) % openListSize;
            if (x >= xLow && x <= xHigh && z >= zLow && z <= zHigh) {
                pathFound = true;
                break;
            }

            if (reachBorder) {
                if (x > 0
                        && xLow <= x - 1
                        && xHigh >= x - 1
                        && zLow <= z
                        && zHigh >= z
                        && Game.world().flag(x - 1, z, CollisionFlag.WALL_WEST)) {
                    pathFound = true;
                    break;
                }

                if (x < CollisionMapSize
                        && 1 + x >= xLow
                        && x + 1 <= xHigh
                        && z >= zLow
                        && zHigh >= z
                        && Game.world().flag(x + 1, z, CollisionFlag.WALL_EAST)) {
                    pathFound = true;
                    break;
                }

                if (z > 0
                        && xLow <= x
                        && xHigh >= x
                        && z - 1 >= zLow
                        && zHigh >= z - 1
                        && Game.world().flag(x, z - 1, CollisionFlag.WALL_SOUTH)) {
                    pathFound = true;
                    break;
                }

                if (z < CollisionMapSize
                        && xLow <= x
                        && x <= xHigh
                        && zLow <= z + 1
                        && zHigh >= z + 1
                        &&  Game.world().flag(x, z + 1, CollisionFlag.WALL_NORTH)) {
                    pathFound = true;
                    break;
                }
            }

            if (x > 0 && pathFindSource[x - 1][z] == 0
                    && Game.world().flag(x - 1, z, CollisionFlag.WEST_BLOCKED)) {
                pathX[openListWrite] = x - 1;
                pathZ[openListWrite] = z;
                pathFindSource[x - 1][z] = CollisionFlag.SOURCE_WEST;
                openListWrite = (openListWrite + 1) % openListSize;
            }

            if (x < CollisionMapSize - 1 && pathFindSource[1 + x][z] == 0
                    && Game.world().flag(x + 1, z, CollisionFlag.EAST_BLOCKED)) {
                pathX[openListWrite] = 1 + x;
                pathZ[openListWrite] = z;
                pathFindSource[x + 1][z] = CollisionFlag.SOURCE_EAST;
                openListWrite = (1 + openListWrite) % openListSize;
            }

            if (z > 0 && pathFindSource[x][z - 1] == 0
                    && Game.world().flag(x, z - 1, CollisionFlag.SOUTH_BLOCKED)) {
                pathX[openListWrite] = x;
                pathZ[openListWrite] = z - 1;
                pathFindSource[x][z - 1] = CollisionFlag.SOURCE_SOUTH;
                openListWrite = (openListWrite + 1) % openListSize;
            }

            if (z < CollisionMapSize - 1 && pathFindSource[x][1 + z] == 0
                    &&  Game.world().flag(x, z + 1, CollisionFlag.NORTH_BLOCKED)) {
                pathX[openListWrite] = x;
                pathZ[openListWrite] = z + 1;
                pathFindSource[x][z + 1] = CollisionFlag.SOURCE_NORTH;
                openListWrite = (openListWrite + 1) % openListSize;
            }

            if (x > 0 && z > 0
                    && Game.world().flag(x, z - 1, CollisionFlag.SOUTH_BLOCKED)
                    && Game.world().flag(x - 1, z, CollisionFlag.WEST_BLOCKED)
                    && Game.world().flag(x - 1, z - 1, CollisionFlag.SOUTH_WEST_BLOCKED)
                    && pathFindSource[x - 1][z - 1] == 0) {
                pathX[openListWrite] = x - 1;
                pathZ[openListWrite] = z - 1;
                pathFindSource[x - 1][z - 1] = CollisionFlag.SOURCE_SOUTH_WEST;
                openListWrite = (1 + openListWrite) % openListSize;
            }

            if (x < CollisionMapSize - 1 && z > 0
                    && Game.world().flag(x, z - 1, CollisionFlag.SOUTH_BLOCKED)
                    && Game.world().flag(x + 1, z, CollisionFlag.EAST_BLOCKED)
                    && Game.world().flag(x + 1, z - 1, CollisionFlag.SOUTH_EAST_BLOCKED)
                    && pathFindSource[1 + x][z - 1] == 0) {

                pathX[openListWrite] = 1 + x;
                pathZ[openListWrite] = z - 1;
                pathFindSource[x + 1][z - 1] = CollisionFlag.SOURCE_SOUTH_EAST;
                openListWrite = (1 + openListWrite) % openListSize;
            }

            if (x > 0 && z < CollisionMapSize - 1
                    && Game.world().flag(x, z + 1, CollisionFlag.NORTH_BLOCKED)
                    && Game.world().flag(x - 1, z, CollisionFlag.WEST_BLOCKED)
                    && Game.world().flag(x - 1, z + 1, CollisionFlag.NORTH_WEST_BLOCKED)
                    && pathFindSource[x - 1][1 + z] == 0) {
                pathX[openListWrite] = x - 1;
                pathZ[openListWrite] = 1 + z;
                openListWrite = (1 + openListWrite) % openListSize;
                pathFindSource[x - 1][z + 1] = CollisionFlag.SOURCE_NORTH_WEST;
            }

            if (x < 2303 && z < CollisionMapSize - 1
                    && Game.world().flag(x, z + 1, CollisionFlag.NORTH_BLOCKED)
                    && Game.world().flag(x + 1, z, CollisionFlag.EAST_BLOCKED)
                    && Game.world().flag(x + 1, z + 1, CollisionFlag.NORTH_EAST_BLOCKED)
                    && pathFindSource[x + 1][1 + z] == 0) {
                pathX[openListWrite] = 1 + x;
                pathZ[openListWrite] = 1 + z;
                pathFindSource[1 + x][1 + z] = CollisionFlag.SOURCE_NORTH_EAST;
                openListWrite = (openListWrite + 1) % openListSize;
            }
        }

        if (!pathFound)
            return null;
        else {
            pathX[0] = x;
            pathZ[0] = z;
            int stepCount = 1;
            int prevSource;
            int source = prevSource = pathFindSource[x][z];
            while ((x != startX || z != startZ)) {
                if (prevSource != source) {
                    prevSource = source;
                    pathX[stepCount] = x;
                    pathZ[stepCount++] = z;
                }

                if ((source & CollisionFlag.SOURCE_SOUTH) != 0)
                    ++z;
                else if ((CollisionFlag.SOURCE_NORTH & source) != 0)
                    --z;

                if ((CollisionFlag.SOURCE_WEST & source) != 0)
                    ++x;
                else if ((source & CollisionFlag.SOURCE_EAST) != 0)
                    --x;
                source = pathFindSource[x][z];
            }

            if(stepCount == -1) {
                return null;
            }

            ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();

            --stepCount;
            startZ = pathZ[stepCount];
            startX = pathX[stepCount];
            --stepCount;
            for (int i = stepCount; i >= 0 && i > stepCount - 25; --i) {
                waypoints.add(new Waypoint(pathX[i] - startX,pathZ[i] - startZ));
            }

            return new Path(startX, startZ, waypoints.toArray(new Waypoint[waypoints.size()]));
        }
    }
}
