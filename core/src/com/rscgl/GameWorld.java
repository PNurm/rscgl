package com.rscgl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.RSMap;
import com.rscgl.assets.def.GameObjectDef;
import com.rscgl.assets.def.WallObjectDef;
import com.rscgl.assets.model.Sector;
import com.rscgl.assets.model.Tile;
import com.rscgl.model.Point2D;
import com.rscgl.model.SectorLocation;
import com.rscgl.model.entity.*;
import com.rscgl.render.MobRenderContext;
import com.rscgl.render.ObjectRenderContext;
import com.rscgl.render.SectorRenderContext;
import com.rscgl.render.WallRenderContext;
import com.rscgl.util.CollisionFlag;
import com.rscgl.util.builder.MapImageBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class GameWorld {

    public static final float TSIZE = 3;
    public static int WORLD_WIDTH = 48 * Sector.SIZE;//48 sector x offset
    public static int WORLD_HEIGHT = 37 * Sector.SIZE;//37 sector y offset
    public static int planeOffset = 944;
    public static int currentPlane;

    /**
     * Currently loaded sectors along with the render context containing the mesh
     */
    public ConcurrentHashMap<SectorLocation, SectorRenderContext> loadedSectors = new ConcurrentHashMap<>();

    /**
     * Map of travel mask values for path finding.
     */
    private HashMap<Point2D, Integer> collisionMap = new HashMap<>();

    /**
     * Currently loading sectors
     */
    private ArrayList<SectorLocation> loadingSectors = new ArrayList<>();

    /**
     * List of new sectors to be loaded
     */
    private ArrayList<SectorLocation> newLocations = new ArrayList<>();

    /**
     * Minimap image builder
     */
    private MapImageBuilder mapBuilder = new MapImageBuilder();

    /**
     * Original game runs at maximum 50 FPS.
     * This variable is responsible for keeping the timings accurate to original
     */
    private float mobProcessRate = 0f;

    /**
     * Temporary cached value
     */
    private Point2D lookupPoint = new Point2D();

    /**
     * Temporary cached value
     */
    private Vector3 position = new Vector3();

    /**
     * Entities registered to the game world
     */
    private final Map<EntityType, ArrayList<Entity>> entities = new EnumMap<>(EntityType.class);


    public GameWorld() {
        for (EntityType type : EntityType.values()) {
            entities.put(type, new ArrayList<>());
        }
    }

    public <E extends Entity> boolean addEntity(E entity) {
        synchronized (entities.get(entity.getEntityType())) {
            return entities.get(entity.getEntityType()).add(entity);
        }
    }

    public <E extends Entity> boolean removeEntity(E entity) {
        synchronized (entities.get(entity.getEntityType())) {
            return entities.get(entity.getEntityType()).remove(entity);
        }
    }

    public <E extends Entity> boolean contains(E entity) {
        synchronized (entities.get(entity.getEntityType())) {
            return entities.get(entity.getEntityType()).contains(entity);
        }
    }

    public <E extends Entity> ArrayList<E> get(EntityType entityType) {
        return (ArrayList<E>) entities.get(entityType);
    }


    public boolean flag(int x, int y, int value) {
        return (getCollisionFlag(x, y) & value) == 0;
    }

    public void setCollisionBit(int x, int z, int value) {
        int putValue = 0;
        lookupPoint.set(x, z);
        Point2D putKey = null;
        if (collisionMap.containsKey(lookupPoint)) {
            putValue = collisionMap.get(lookupPoint);
        } else {
            putKey = new Point2D();
        }
        putValue = putValue | value;
        collisionMap.put(putKey, putValue);
    }

    public void removeCollisionBit(int x, int z, int value) {
        int putValue = 0;
        lookupPoint.set(x, z);
        Point2D putKey = null;
        if (collisionMap.containsKey(lookupPoint)) {
            putValue = collisionMap.get(lookupPoint);
        } else {
            putKey = new Point2D();
        }
        putValue = putValue & ~value;
        collisionMap.put(putKey, putValue);
    }

    public int getCollisionFlag(int x, int z) {
        if (collisionMap.containsKey(lookupPoint.set(x, z))) {
            return collisionMap.get(lookupPoint);
        }
        return 0;
    }

    public void registerWallObject(int x, int y, int id, int dir) {
        WallObjectDef def = RSCache.WALLS[id];
        if (def == null) {
            return;
        }
        if (def.getType() == 1) {
            if (dir == 0) {
                setCollisionBit(x, y - 1, CollisionFlag.WALL_NORTH);
                setCollisionBit(x, y - 1, CollisionFlag.WALL_SOUTH);
            } else if (dir == 1) {
                setCollisionBit(x - 1, y, CollisionFlag.WALL_EAST);
                setCollisionBit(x - 1, y, CollisionFlag.WALL_WEST);
            } else if (dir == 2) {
                setCollisionBit(x, y, CollisionFlag.DIAGONAL_A);
            } else if (dir == 3) {
                setCollisionBit(x, y, CollisionFlag.DIAGONAL_B);
            }
            setVertexLightArea(x, y, 1, 1);
        }

        WallEntity wallEntity = new WallEntity();
        wallEntity.setType(id);
        wallEntity.setDirection(dir);
        addEntity(wallEntity);

        wallEntity.setRenderContext(new WallRenderContext(wallEntity));
    }

    public ObjectEntity registerObject(int id, int x, int y, int dir) {
        if(id > RSCache.OBJECT_COUNT) {
            return null;
        }
        GameObjectDef def = RSCache.OBJECTS[id];
        if (def == null) {
            System.out.println("Null object " + id);
            return null;
        }

        ObjectEntity objectEntity = new ObjectEntity();
        objectEntity.setType(id);
        objectEntity.setTile(x, y);
        objectEntity.setDirection(dir);

        ObjectRenderContext renderContext = new ObjectRenderContext(objectEntity);
        objectEntity.setRenderContext(renderContext);

        float posX = (x * 2F + objectEntity.getWidth()) * TSIZE / 2;
        float posZ = (y * 2F + objectEntity.getHeight()) * TSIZE / 2;
        float posY = getInterpolatedElevation(-posX, posZ);
        if (id == 74)
            posY = posY + (3 * TSIZE);

        Matrix4 transform = new Matrix4();
        transform.scl(-1, -1, 1);
        transform.setTranslation(-posX, posY, posZ);
        transform.rotate(Vector3.Y, (objectEntity.getDirection() * 45F));

        Vector3 v = new Vector3();
        transform.getTranslation(v);
        objectEntity.setPosition(v.x, v.y, v.z);
        renderContext.getMesh().worldTransform.set(transform);

        if (def.getType() == 1 || def.getType() == 2) {
            int width = objectEntity.getWidth();
            int height = objectEntity.getHeight();
            for (int xx = x; xx < x + width; ++xx) {
                for (int yy = y; y + height > yy; ++yy) {
                    if (RSCache.OBJECTS[id].getType() == 1)
                        setCollisionBit(xx, yy, CollisionFlag.FULL_BLOCK_C);
                    else {
                        if (dir == 0) {
                            setCollisionBit(xx, yy, CollisionFlag.WALL_EAST);
                            setCollisionBit(xx - 1, yy, CollisionFlag.WALL_WEST);
                        } else if (dir == 2) {
                            setCollisionBit(xx, yy, CollisionFlag.WALL_SOUTH);
                            setCollisionBit(xx, 1 + yy, CollisionFlag.WALL_NORTH);
                        } else if (dir == 6) {
                            setCollisionBit(xx, yy, CollisionFlag.WALL_NORTH);
                            setCollisionBit(xx, yy - 1, CollisionFlag.WALL_SOUTH);
                        } else if (dir == 4) {
                            setCollisionBit(xx, yy, CollisionFlag.WALL_WEST);
                            setCollisionBit(xx + 1, yy, CollisionFlag.WALL_EAST);
                        }
                    }
                }
            }
            setVertexLightArea(x, y, objectEntity.getWidth(), objectEntity.getHeight());
        }


        addEntity(objectEntity);

        return objectEntity;
    }

    public final void unregisterObject(ObjectEntity object) {

        if (object.getDef().getType() != 1 && object.getDef().getType() != 2) {
            return;
        }

        if (object.getRenderContext() != null) {
            object.getRenderContext().dispose();
            object.setRenderContext(null);
        }

        int width = object.getWidth();
        int height = object.getHeight();

        for (int x = object.getTileX(); object.getTileX() + width > x; ++x) {
            for (int y = object.getTileY(); height + object.getTileY() > y; ++y) {
                if (object.getDef().getType() == 1) {
                    removeCollisionBit(x, y, CollisionFlag.FULL_BLOCK_C);
                    continue;
                }
                if (object.getDirection() == 0) {
                    removeCollisionBit(x, y, CollisionFlag.WALL_EAST);
                    removeCollisionBit(x - 1, y, CollisionFlag.WALL_WEST);
                } else if (object.getDirection() == 2) {
                    removeCollisionBit(x, y, CollisionFlag.WALL_SOUTH);
                    removeCollisionBit(x, y + 1, CollisionFlag.WALL_NORTH);
                } else if (object.getDirection() == 4) {
                    removeCollisionBit(x, y, CollisionFlag.WALL_WEST);
                    removeCollisionBit(x, y + 1, CollisionFlag.WALL_EAST);
                } else if (object.getDirection() == 6) {
                    removeCollisionBit(x, y, CollisionFlag.WALL_NORTH);
                    removeCollisionBit(x, y - 1, CollisionFlag.WALL_SOUTH);
                }
            }
        }
        setVertexLightArea(object.getTileX(), object.getTileY(), object.getWidth(), object.getHeight());
    }

    public void unregisterWallObject(WallEntity wall) {
        wall.getRenderContext().dispose();//getMesh().dispose();
        if (wall.getDef().getType() == 1) {
            int x = wall.getTileX();
            int y = wall.getTileY();

            if (wall.getDirection() == 0) {
                removeCollisionBit(x, y, CollisionFlag.WALL_NORTH);
                removeCollisionBit(x, y - 1, CollisionFlag.WALL_SOUTH);
            } else if (wall.getDirection() == 1) {
                removeCollisionBit(x, y, CollisionFlag.WALL_EAST);
                removeCollisionBit(x - 1, y, CollisionFlag.WALL_WEST);
            } else if (wall.getDirection() == 2) {
                removeCollisionBit(x, y, CollisionFlag.DIAGONAL_A);
            } else if (wall.getDirection() == 3) {
                removeCollisionBit(x, y, CollisionFlag.DIAGONAL_B);
            }
            setVertexLightArea(wall.getTileX(), wall.getTileY(), 1, 1);
        }
        removeEntity(wall);
    }

    public final PlayerEntity createPlayer(int serverIndex, float x, float z, int direction) {
        PlayerEntity c = getPlayer(serverIndex);
        if (c != null) {
            c.setNextDirection(direction);
            int stepIndex = c.stepPointer;
            if (x != c.stepX[stepIndex] || c.stepZ[stepIndex] != z) {
                c.stepPointer = stepIndex = (1 + stepIndex) % 10;
                c.setPath(stepIndex, x, z);
            }
        } else {
            c = new PlayerEntity();
            c.setServerIndex(serverIndex);
            c.resetMovement();
            c.setPath(0, x, z);
            c.setPosition(x, 0, z);
            c.setDirection(direction);
            c.setNextDirection(direction);

            addEntity(c);
            System.out.println("Created player at " + c.getPosition());

            MobRenderContext renderContext = new MobRenderContext(c);
            c.setRenderContext(renderContext);

        }
        return c;
    }

    public final MobEntity createNpc(int serverIndex, int type, float x, float y, int direction) {
        NpcEntity c = getNpc(serverIndex);
        if (c != null) {
            c.setType(type);
            c.setNextDirection(direction);
            c.setServerIndex(serverIndex);
            int stepIndex = c.stepPointer;
            if (x != c.stepX[stepIndex] || c.stepZ[stepIndex] != y) {
                c.stepPointer = stepIndex = (1 + stepIndex) % 10;
                c.setPath(stepIndex, x, y);
            }
        } else {
            c = new NpcEntity(type);
            c.setServerIndex(serverIndex);
            c.resetMovement();
            c.setPath(0, x, y);
            c.setPosition(x, 0, y);
            c.setDirection(direction);
            c.setNextDirection(direction);

            addEntity(c);
            if (c.getDef() != null) {
                MobRenderContext renderContext = new MobRenderContext(c);
                c.setRenderContext(renderContext);
                renderContext.generateAppearance();
            }
        }

        return c;
    }

    public void resetFlags() {
        collisionMap.clear();
    }

    public final float getInterpolatedElevation(float xf, float zf) {
        xf = -xf;

        float a = worldTile(xf, zf).getHeight();
        float b = worldTile(xf + TSIZE, zf).getHeight();
        float c = worldTile(xf, zf + TSIZE).getHeight();
        float d = worldTile(xf + TSIZE, zf + TSIZE).getHeight();

        xf /= TSIZE;
        zf /= TSIZE;

        float tx = (float) (xf - Math.floor(xf));
        float ty = (float) (zf - Math.floor(zf));

        float ha = a * (1.0F - tx) + b * tx;
        float hb = c * (1.0F - tx) + d * tx;

        return ha * (1.0F - ty) + hb * ty;
    }

    public Tile worldTile(float wx, float wy) {
        wx = (float) (Math.floor(wx / TSIZE));
        wy = (float) (Math.floor(wy / TSIZE));
        wx += WORLD_WIDTH;
        wy += WORLD_HEIGHT;

        return RSMap.getWorldTile((int) wx, (int) wy, currentPlane);
    }

    public boolean manageLoadedSectors(int tileX, int tileZ) {
        int worldTileX = tileX + this.WORLD_WIDTH;
        int worldTileZ = tileZ + this.WORLD_HEIGHT;

        int newSectorX = (worldTileX) / 48;
        int newSectorZ = (worldTileZ) / 48;

        newLocations.clear();
        final int regionSpan = 1;
        newLocations.add(SectorLocation.create(newSectorX, newSectorZ, currentPlane));

        for (int offX = -regionSpan; offX <= regionSpan; offX++) {
            for (int offZ = -regionSpan; offZ <= regionSpan; offZ++) {
                SectorLocation l = SectorLocation.create(newSectorX - offX, newSectorZ - offZ, currentPlane);
                if (!newLocations.contains(l))
                    newLocations.add(l);
            }
        }

        for (Iterator<SectorLocation> it = loadedSectors.keySet().iterator(); it.hasNext(); ) {
            SectorLocation location = it.next();
            if (!newLocations.contains(location)) {
                loadedSectors.get(location).dispose();
                it.remove();
                //System.out.println("Unloading " + location);
            }
        }

        for (SectorLocation newLocation : newLocations) {
            if (!loadedSectors.containsKey(newLocation) && !loadingSectors.contains(newLocation)) {
                loadSector(newLocation);
                //System.out.println("Loading " + newLocation);
            }
        }
        if (mapBuilder.buildMinimap(tileX, tileZ, currentPlane)) {
            Game.ui().getMinimap().set(mapBuilder.getCurrentMap());
        }
        return true;
    }

    private void loadSector(final SectorLocation sectorLocation) {
        loadingSectors.add(sectorLocation);
        final SectorRenderContext sectorRenderContext = new SectorRenderContext(sectorLocation);
        Game.inst.addLoadingTask(() -> {
            System.out.println("Loaded " + sectorLocation);
            int worldX = sectorLocation.worldX() - WORLD_WIDTH;
            int worldZ = sectorLocation.worldY() - WORLD_HEIGHT;

            Matrix4 transform = new Matrix4();
            transform.scl(-1, 1, 1);
            transform.setTranslation(-TSIZE * worldX, 0, TSIZE * worldZ);
            sectorRenderContext.getTerrain().worldTransform.set(transform);
            sectorRenderContext.getWalls().worldTransform.set(transform);

            loadingSectors.remove(sectorLocation);
            loadedSectors.put(sectorLocation, sectorRenderContext);
        });
    }

    public PlayerEntity getPlayer(int serverIndex) {
        for (PlayerEntity p : getPlayers()) {
            if (serverIndex == p.getServerIndex()) {
                return p;
            }
        }
        return null;
    }

    public NpcEntity getNpc(int serverIndex) {
        for (NpcEntity n : getNpcs())
            if (n.getServerIndex() == serverIndex)
                return n;
        return null;
    }

    public void reset() {
        loadingSectors.clear();

        entities.values().forEach(v -> v.forEach(f -> f.getRenderContext().dispose()));
        entities.values().forEach(ArrayList::clear);

        loadedSectors.values().forEach(SectorRenderContext::dispose);
        loadedSectors.clear();

        resetFlags();

    }

    public boolean hasObject(int x, int z) {
        for (ObjectEntity g : getObjects()) {
            if (g.getTileX() == x && g.getTileY() == z) {
                return true;
            }
        }
        return false;
    }

    public ObjectEntity getObject(int x, int y) {
        for (ObjectEntity g : getObjects()) {
            if (g.getTileX() == x && g.getTileY() == y) {
                return g;
            }
        }
        return null;
    }

    public void dispose() {
        loadedSectors.clear();
        reset();
    }

    public final void setVertexLightArea(int tileX, int tileZ, int width, int height) {
        for (int x = tileX; x <= width + tileX; ++x) {
            for (int z = tileZ; tileZ + height >= z; ++z) {
                int flag00 = CollisionFlag.FULL_BLOCK_C | CollisionFlag.DIAGONAL_B | CollisionFlag.WALL_NORTH | CollisionFlag.WALL_EAST;
                int flag10 = CollisionFlag.FULL_BLOCK_C | CollisionFlag.DIAGONAL_A | CollisionFlag.WALL_WEST | CollisionFlag.WALL_NORTH;
                int flag01 = CollisionFlag.FULL_BLOCK_C | CollisionFlag.DIAGONAL_A | CollisionFlag.WALL_SOUTH | CollisionFlag.WALL_EAST;
                int flag11 = CollisionFlag.FULL_BLOCK_C | CollisionFlag.DIAGONAL_B | CollisionFlag.WALL_WEST | CollisionFlag.WALL_SOUTH;

                if ((flag00 & getCollisionFlag(x, z)) == 0
                        && (flag10 & getCollisionFlag(x - 1, z)) == 0
                        && (getCollisionFlag(x, z - 1) & flag01) == 0
                        && (getCollisionFlag(x - 1, z - 1) & flag11) == 0) {
                    this.setVertexShadow(x, z, 0);
                } else {
                    this.setVertexShadow(x, z, 0.25F);
                }
            }
        }
    }

    /**
     * Sets tile vertex lighting value to darken tiles under game objects.
     */
    public void setVertexShadow(int x, int y, float value) {
        SectorLocation location = SectorLocation.getWorld(WORLD_WIDTH + x, WORLD_HEIGHT + y, 0);
        if (loadedSectors.containsKey(location)) {
            SectorRenderContext context = loadedSectors.get(location);
            context.setVertexShadow(WORLD_WIDTH + x - location.worldX(), WORLD_HEIGHT + y - location.worldY(), value);
        }
    }

    public Map<SectorLocation, SectorRenderContext> getLoadedSectors() {
        return loadedSectors;
    }

    /**
     * Updates world entities
     */
    public void update() {
        mobProcessRate += Gdx.graphics.getDeltaTime();
        if (mobProcessRate <= 0.020F) {
            return;
        }
        mobProcessRate = 0F;
        Game.frameCounter++;

        for (Entity entity : entities.get(EntityType.PLAYER)) {
            PlayerEntity player = (PlayerEntity) entity;
            player.tick();
        }

        for (Entity entity : entities.get(EntityType.NPC)) {
            NpcEntity npcEntity = (NpcEntity) entity;
            npcEntity.tick();
        }
    }

    public Map<EntityType, ArrayList<Entity>> getEntities() {
        return entities;
    }

    public ArrayList<WallEntity> getWalls() {
        return get(EntityType.WALL);
    }

    public ArrayList<ObjectEntity> getObjects() {
        return get(EntityType.OBJECT);
    }

    public ArrayList<ItemEntity> getItems() {
        return get(EntityType.ITEM);
    }

    public ArrayList<NpcEntity> getNpcs() {
        return get(EntityType.NPC);
    }

    public ArrayList<PlayerEntity> getPlayers() {
        return get(EntityType.PLAYER);
    }

    public WallEntity getWallObject(int localX, int localY) {
        for (WallEntity wallEntity : getWalls()) {
            if (wallEntity.getTileX() == localX && wallEntity.getTileY() == localY) {
                return wallEntity;
            }
        }
        return null;
    }

    public ItemEntity getItem(int localX, int localY, int type) {
        for (ItemEntity itemEntity : getItems()) {
            if (itemEntity.getTileX() == localX && itemEntity.getTileY() == localY && itemEntity.getType() == type) {
                return itemEntity;
            }
        }
        return null;
    }

    public void registerItem(int itemID, int localX, int localY) {
        ItemEntity item = new ItemEntity(itemID);
        item.setTile(localX, localY);
        item.setObjectHeight(0);

        float worldX = -localX * GameWorld.TSIZE + 0.5F;
        float worldY = Game.world().getInterpolatedElevation(localX, localY);
        float worldZ = localY * GameWorld.TSIZE + 0.5F;
        this.position.set(worldX, worldY, worldZ);

        ObjectEntity objectEntity = getObject(localX, localY);
        if (objectEntity != null) {
            item.setObjectHeight(objectEntity.getDef().getItemHeight());
        }
    }
}