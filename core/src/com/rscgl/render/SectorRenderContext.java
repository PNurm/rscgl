package com.rscgl.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.rscgl.Game;
import com.rscgl.GameWorld;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.RSMap;
import com.rscgl.assets.def.WallObjectDef;
import com.rscgl.assets.model.RSMaterial;
import com.rscgl.assets.model.Sector;
import com.rscgl.assets.model.Tile;
import com.rscgl.model.Point2D;
import com.rscgl.model.RSMesh;
import com.rscgl.model.SectorLocation;
import com.rscgl.model.Vertex;
import com.rscgl.util.CollisionFlag;

import static com.rscgl.GameWorld.TSIZE;
import static com.rscgl.assets.model.RSMaterial.TRANSPARENT;

public class SectorRenderContext implements Disposable, RenderableProvider {

    private final SectorLocation location;
    private final Sector sector;
    private final int length;

    private final RSMesh terrain;
    private final RSMesh walls;

    public SectorRenderContext(SectorLocation location) {
        this.location = location;
        this.sector = RSMap.get(location);
        this.length = Sector.SIZE + 1;

        this.terrain = new RSMesh(18688, 18688);
        this.walls = new RSMesh(18688, 18688);

        buildVertices();
        buildIndices();
        buildWalls();
        buildBridges();

        terrain.update();
        walls.update();

        System.out.println("SectorRenderContext " + location + " has been created succesfully");
    }


    public Vector3 worldPos(int tileX, int tileY) {
        Tile tile = lookupTile(location.worldX() + tileX, location.worldY() + tileY);

        float tileElevation = tile.getHeight();
        if (hasBridge(tileX, tileY))
            tileElevation = 0;

        return new Vector3().set(tileX * TSIZE, tileElevation, tileY * TSIZE);
    }

    public Vector3 linearComb(Vector3 dest, Vector3 one, float scale, Vector3 two, float scaleTwo) {
        dest.x = one.x * scale + two.x * scaleTwo;
        dest.y = one.y * scale + two.y * scaleTwo;
        dest.z = one.z * scale + two.z * scaleTwo;
        return dest;
    }

    Vector3 side1 = new Vector3();
    Vector3 side2 = new Vector3();
    Vector3 contribution = new Vector3();
    Vector3 dest = new Vector3();

    public void calculateNormal(Vector3 normal, int tileX, int tileY) {
        normal.setZero();
        Vector3 me = worldPos(tileX, tileY);

        int tileCount = 8;
        Vector3[] tiles = {
                worldPos(tileX - 1, tileY - 1),
                worldPos(tileX - 1, tileY),
                worldPos(tileX - 1, tileY + 1),
                worldPos(tileX, tileY + 1),
                worldPos(tileX + 1, tileY + 1),
                worldPos(tileX + 1, tileY),
                worldPos(tileX + 1, tileY - 1),
                worldPos(tileX, tileY - 1)
        };

        for (int i = 0; i < tileCount; i++) {
            for (int j = 0; j < 2; j++) {
                Vector3 thisTile = tiles[i];
                Vector3 nextTile = tiles[(i + 1) % tileCount];

                if (thisTile == null || nextTile == null
                        || Float.isNaN(thisTile.y) || Float.isNaN(nextTile.y)) {
                    continue;
                }
                linearComb(side1, thisTile, 1, me, -1);
                linearComb(side2, nextTile, 1, me, -1);
                cross(contribution, side1, side2).nor();
                if (contribution.y < 0)
                    contribution.scl(-1);
                dest.add(contribution);
            }
        }
        normal.set(dest).nor();
    }

    public Vector3 cross(Vector3 origin, Vector3 a, Vector3 b) {
        if (origin == a) {
            final float a0 = a.x;
            final float a1 = a.y;
            origin.x = (a.y * b.z) - (a.z * b.y);
            origin.y = (a.z * b.x) - (a0 * b.z);
            origin.z = (a.x * b.y) - (a1 * b.x);
        } else if (origin == b) {
            final float b0 = b.x;
            final float b1 = b.y;

            origin.x = (a.y * b.z) - (a.z * b.y);
            origin.y = (a.z * b0) - (a.x * b.z);
            origin.z = (a.x * b1) - (a.y * b.x);
        } else {
            origin.x = (a.y * b.z) - (a.z * b.y);
            origin.y = (a.z * b.x) - (a.x * b.z);
            origin.z = (a.x * b.y) - (a.y * b.x);
        }
        return origin;
    }

    public void buildVertices() {
        for (int x = 0; x < length; x++) {
            for (int z = 0; z < length; z++) {

                Vertex vertex = new Vertex();
                vertex.pos(worldPos(x, z));
                vertex.color.r = (float) (Math.random() * 0.080D) - 0.040F;
                vertex.uv.set(0.95F * (x & 1), 0.95F * (z & 1));
                terrain.vertex(vertex);

                calculateNormal(vertex.normal, x, z);
            }
        }
    }


    private boolean hasBridge(int worldX, int worldZ) {
        if (tileTexture(worldX, worldZ) > 0
                && RSCache.TILES[tileTexture(worldX, worldZ) - 1].getType() == 4)
            return true;
        if (tileTexture(worldX - 1, worldZ) > 0
                && RSCache.TILES[tileTexture(worldX - 1, worldZ) - 1].getType() == 4)
            return true;
        if (tileTexture(worldX, worldZ - 1) > 0
                && RSCache.TILES[tileTexture(worldX, worldZ - 1) - 1].getType() == 4)
            return true;
        if (tileTexture(worldX - 1, worldZ - 1) > 0
                && RSCache.TILES[tileTexture(worldX - 1, worldZ - 1) - 1].getType() == 4)
            return true;

        return false;

    }

    public final int tileTexture(int x, int z) {
        return lookupTile(location.worldX() + x, location.worldY() + z).getTexture();
    }

    private final float tileHeight(int x, int z) {
        return lookupTile(location.worldX() + x, location.worldY() + z).getHeight();
    }

    public void buildIndices() {

        for (int x = 0; x < length - 1; x++) {
            for (int z = 0; z < length - 1; z++) {
                int vID = x * length + z;

                int worldX = location.worldX() + x;
                int worldZ = location.worldY() + z;

                RSMaterial material = getTileMaterials(worldX, worldZ);
                int splitDirection = material.getType();

                float slope = (tileHeight(x + 1, 1 + z) - tileHeight(x, z)
                        + tileHeight(x, z + 1) - tileHeight(x, z));

                //minimapBuilder.drawTile(x, z, splitDirection, material.getDecorA(), material.getDecorB());

                if (material.getDecorA() == material.getDecorB() && slope == 0) {
                    if (material.getDecorA() != TRANSPARENT) {
                        terrain.copyTriangle(material.getDecorA(),
                                (short) (vID + length),
                                (short) (vID + 1),
                                (short) (vID));

                        terrain.copyTriangle(material.getDecorA(),
                                (short) (vID + length),
                                (short) (vID + length + 1),
                                (short) (vID + 1));
                    }
                    continue;
                }

                if (splitDirection == 0) {
                    if (material.getDecorA() != TRANSPARENT) {
                        terrain.copyTriangle(material.getDecorA(),
                                (short) (vID + length),
                                (short) (vID),
                                (short) (vID + 1));
                    }
                    if (material.getDecorB() != TRANSPARENT) {
                        terrain.copyTriangle(material.getDecorB(),
                                (short) (vID + 1),
                                (short) (vID + length + 1),
                                (short) (vID + length));
                    }
                } else {
                    if (material.getDecorA() != TRANSPARENT) {
                        terrain.copyTriangle(material.getDecorA(),
                                (short) (vID + 1),
                                (short) (vID + length + 1),
                                (short) (vID));
                    }
                    if (material.getDecorB() != TRANSPARENT) {
                        terrain.copyTriangle(material.getDecorB(),
                                (short) (vID + length),
                                (short) (vID),
                                (short) (vID + length + 1));
                    }
                }
            }
        }

    }

    private void buildBridges() {
        for (int x = 1; x < length - 1; ++x)
            for (int z = 1; z < length - 1; ++z) {

                Tile tile = lookupTile(x + location.worldX(), z + location.worldY());
                if (tile.getTexture() > 0 && tile.getDef().getType() == 4) {
                    int texture = tile.getDef().getTexture();
                    insertBridgeTile(texture, x, z);

                } else if (tile.getTexture() == 0 || tile.getDef().getType() != 3) {
                    Tile tileSouth = lookupTile(x + location.worldX(), z + location.worldY() + 1);
                    Tile tileNorth = lookupTile(x + location.worldX(), z + location.worldY() - 1);
                    Tile tileWest = lookupTile(x + location.worldX() + 1, z + location.worldY());
                    Tile tileEast = lookupTile(x + location.worldX() - 1, z + location.worldY());

                    if (tileSouth.getTexture() > 0
                            && tileSouth.getDef().getType() == 4) {
                        int texture = tileSouth.getDef().getTexture();
                        insertBridgeTile(texture, x, z);
                    }
                    if (tileNorth.getTexture() > 0
                            && tileNorth.getDef().getType() == 4) {
                        int texture = tileNorth.getDef().getTexture();
                        insertBridgeTile(texture, x, z);
                    }
                    if (tileWest.getTexture() > 0
                            && tileWest.getDef().getType() == 4) {
                        int texture = tileWest.getDef().getTexture();
                        insertBridgeTile(texture, x, z);
                    }
                    if (tileEast.getTexture() > 0
                            && tileEast.getDef().getType() == 4) {
                        int texture = tileEast.getDef().getTexture();
                        insertBridgeTile(texture, x, z);
                    }
                }
            }
    }

    private void insertBridgeTile(int texture, int x, int z) {
        short v0 = terrain.vertex(new Vertex()
                .pos(x * TSIZE, tileHeight(x, z), z * TSIZE)
                .uv(0, 1));

        short v1 = terrain.vertex(new Vertex()
                .pos((x + 1) * TSIZE, tileHeight(1 + x, z), z * TSIZE)
                .uv(1, 1));

        short v2 = terrain.vertex(new Vertex()
                .pos(x * TSIZE, tileHeight(x, 1 + z), (z + 1) * TSIZE)
                .uv(0, 0));

        short v3 = terrain.vertex(new Vertex()
                .pos((x + 1) * TSIZE, tileHeight(x + 1, z + 1), (z + 1) * TSIZE)
                .uv(1, 0));

        terrain.quad(texture, v0, v1, v2, v3);
    }


    public void addCollisionFlag(int x, int z, int bit) {
        Game.world().setCollisionBit(location.worldX() + x - GameWorld.WORLD_WIDTH, location.worldY() + z - GameWorld.WORLD_HEIGHT, bit);
    }

    public void buildWalls() {
        for (int x = 0; x < Sector.SIZE; ++x) {
            for (int z = 0; z < Sector.SIZE; ++z) {

                Tile t = lookupTile(x + location.worldX(), z + location.worldY());
                int wall = t.getWallVertical();
                if (wall > 0 && (RSCache.WALLS[wall - 1].getInvisible() == 0)) {
                    insertWallIntoModel(wall - 1, x, z, 1 + x, z);
                    if (RSCache.WALLS[wall - 1].getType() != 0) {
                        addCollisionFlag(x, z, CollisionFlag.WALL_NORTH);
                        if (z > 0)
                            addCollisionFlag(x, z - 1, CollisionFlag.WALL_SOUTH);
                    }
                }

                wall = t.getWallHorizontal();
                if (wall > 0
                        && (RSCache.WALLS[wall - 1].getInvisible() == 0)) {
                    insertWallIntoModel(wall - 1, x, z, x, 1 + z);

                    if (RSCache.WALLS[wall - 1].getType() != 0) {
                        addCollisionFlag(x, z, CollisionFlag.WALL_EAST);
                        if (x > 0)
                            addCollisionFlag(x - 1, z, CollisionFlag.WALL_WEST);
                    }
                }

                wall = t.getWallDiagonal();
                if (wall > 0 && wall < 12000
                        && (RSCache.WALLS[wall - 1].getInvisible() == 0)) {
                    insertWallIntoModel(wall - 1, x, z, x + 1, 1 + z);
                    if (RSCache.WALLS[wall - 1].getType() != 0) {
                        addCollisionFlag(x, z, CollisionFlag.FULL_BLOCK_C);
                    }
                }

                if (wall > 12000 && wall < 24000 && (RSCache.WALLS[wall - 12001].getInvisible() == 0)) {
                    insertWallIntoModel(wall - 12001, x + 1, z, x, 1 + z);
                    if (RSCache.WALLS[wall - 12001].getType() != 0) {
                        addCollisionFlag(x, z, CollisionFlag.DIAGONAL_A);
                    }
                }
            }
        }
    }

    public void insertWallIntoModel(int wallID, int tileX1, int tileY1, int tileX2, int tileY2) {
        setVertexShadow(tileX1, tileY1, 0.25f);
        setVertexShadow(tileX2, tileY2, 0.25f);

        WallObjectDef def = RSCache.WALLS[wallID];

        float height = (def.getHeight() / 128f) * TSIZE;// CacheValues.wallObjectHeight[var1];
        int frontTex = def.getFrontTex();
        int backTex = def.getBackTex();
        if (frontTex == TRANSPARENT && backTex == TRANSPARENT) {
            return;
        }
        float x1 = tileX1 * TSIZE;
        float x2 = tileX2 * TSIZE;

        float z1 = tileY1 * TSIZE;
        float z2 = tileY2 * TSIZE;

        Vertex v1 = new Vertex()//bottom left
                .pos(x1, tileHeight(tileX1, tileY1), z1)
                .uv(0, 1)
                .setNor(0, 1, 0);

        Vertex v2 = new Vertex()//top left
                .pos(x1, tileHeight(tileX1, tileY1) + height, z1)
                .uv(1, 1)
                .setNor(0, 1, 0);

        Vertex v3 = new Vertex() //top right
                .pos(x2, tileHeight(tileX2, tileY2) + height, z2)
                .uv(1, 0)
                .setNor(0, 1, 0);

        Vertex v4 = new Vertex() //bottom right
                .pos(x2, tileHeight(tileX2, tileY2), z2)
                .uv(0, 0)
                .setNor(0, 1, 0);

        short i0 = walls.vertex(v1);
        short i1 = walls.vertex(v2);
        short i2 = walls.vertex(v3);
        short i3 = walls.vertex(v4);

        walls.quad(frontTex, i0, i1, i2, i3);
        walls.quad(backTex, i3, i0, i1, i2);
    }

    private RSMaterial getTileMaterials(int x, int z) {
        Tile tile = lookupTile(x, z);
        Tile north = lookupTile(x, z - 1);
        Tile west = lookupTile(x + 1, z);
        Tile east = lookupTile(x - 1, z);
        Tile south = lookupTile(x, z + 1);

        int colorA = RSMaterial.colorToResource[tile.getColor()];
        int colorB = colorA;
        int original = colorA;

        if (location.getPlane() == 1 || location.getPlane() == 2) {
            colorA = TRANSPARENT;
            colorB = TRANSPARENT;
            original = TRANSPARENT;
        }
        int orientation = 0;
        if (tile.getTexture() > 0) {
            int type = tile.getDef().getType();//tileDef.getTileType();
            int isPath = tile.isPath(); //tileType != 2 ? 0 : 1;
            colorA = colorB = tile.getDef().getTexture();

            if (type == 4) {//Bridge
                colorA = 1;
                colorB = 1;
                if (tile.getTexture() == 12) {
                    colorA = 31;
                    colorB = 31;
                }
            }
            if (type == 5) {
                if (tile.getWallDiagonal() > 0 && tile.getWallDiagonal() < 24000)
                    if (east.getTexture(original) != TRANSPARENT
                            && north.getTexture(original) != TRANSPARENT) {
                        orientation = 0;
                        colorA = east.getTexture(original);
                    } else if (west.getTexture(original) != TRANSPARENT
                            && south.getTexture(original) != TRANSPARENT) {
                        colorB = west.getTexture(original);
                        orientation = 0;
                    } else if (west.getTexture(original) != TRANSPARENT
                            && north.getTexture(original) != TRANSPARENT) {
                        colorB = west.getTexture(original);
                        orientation = 1;
                    } else if (east.getTexture(original) != TRANSPARENT
                            && south.getTexture(original) != TRANSPARENT) {
                        orientation = 1;
                        colorA = east.getTexture(original);
                    }
            } else if (type != 2 || tile.getWallDiagonal() > 0 && tile.getWallDiagonal() < 24000) {
                if (isPath != east.isPath() && north.isPath() != isPath) {
                    colorA = original;
                    orientation = 0;
                } else if (isPath != west.isPath() && south.isPath() != isPath) {
                    orientation = 0;
                    colorB = original;
                } else if (isPath != west.isPath() && north.isPath() != isPath) {
                    colorB = original;
                    orientation = 1;
                } else if (isPath != east.isPath() && isPath != south.isPath()) {
                    colorA = original;
                    orientation = 1;
                }
            }
        }
        return new RSMaterial(colorB, colorA, orientation);
    }


    private Tile getTile(int x, int z) {
        int localX = x - location.worldX();
        int localZ = z - location.worldY();
        Tile tile;
        if (localX >= 0 && localX < Sector.SIZE && localZ >= 0 && localZ < Sector.SIZE) {
            tile = sector.getTile(localX, localZ);
        } else {
            tile = RSMap.getWorldTile(x, z, location.getPlane());
        }
        return tile;
    }

    public Tile lookupTile(int x, int z) {
        Tile tile = getTile(x, z);
        if (tile == null)
            return new Tile();
        //
        int localX = x - location.worldX();
        int localZ = z - location.worldY();
        if (tile.getTexture() == 250) {
            Tile west = getTile(x + 1, z);
            Tile south = getTile(x, z + 1);
            if (localX == 47
                    && west.getTexture() != 250
                    && west.getTexture() != 2) {
                tile.texture = (byte) 9;
            } else if (localZ == 47
                    && south.getTexture() != 250
                    && south.getTexture() != 2) {
                tile.texture = (byte) 9;
            } else {
                tile.texture = (byte) 2;
            }
        }
        return tile;
    }


    private final Vector3 v0pos = new Vector3();
    private final Vector3 v1pos = new Vector3();
    private final Vector3 v2pos = new Vector3();

    public Point2D pickTerrainTile(Ray ray) {
        for (int x = 0; x < Sector.SIZE; x++) {
            for (int z = 0; z < Sector.SIZE; z++) {

                int vID = x * length + z;

                int[][] tris = {
                        {
                                vID + length,
                                vID + 1,
                                vID
                        },
                        {
                                vID + length,
                                vID + length + 1,
                                vID + 1
                        }
                };
                for (int[] indices : tris) {
                    Vector3 v0 = terrain.getVertexPosition(v0pos, indices[0]);
                    Vector3 v1 = terrain.getVertexPosition(v1pos, indices[1]);
                    Vector3 v2 = terrain.getVertexPosition(v2pos, indices[2]);
                    if (Intersector.intersectRayTriangle(ray, v0, v1, v2, null)) {
                        return new Point2D(location.worldX() + x - GameWorld.WORLD_WIDTH, location.worldY() + z - GameWorld.WORLD_HEIGHT);
                    }
                }
            }
        }
        return null;
    }

    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        terrain.getRenderables(renderables, pool);
        walls.getRenderables(renderables, pool);
    }

    @Override
    public void dispose() {
        terrain.dispose();
        walls.dispose();
    }


    public void setVertexShadow(int x, int y, float value) {
        Vertex original = terrain.getVertex(x * length + y);
        for (int i = 0; i < terrain.vertexCount; i++) {
            Vertex other = terrain.getVertex(i);
            if (original.position.equals(other.position)) {
                other.color.g = value;
                terrain.updateColor(i);
            }
        }
        terrain.setDirty(true);
    }

    public void transform(Matrix4 transform) {
        terrain.transformVertices(transform);
        walls.transformVertices(transform);
    }

    public RSMesh getWalls() {
        return walls;
    }

    public RSMesh getTerrain() {
        return terrain;
    }
}
