package com.rscgl;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rscgl.assets.Assets;
import com.rscgl.assets.RSCache;
import com.rscgl.model.Point2D;
import com.rscgl.model.entity.*;
import com.rscgl.net.Buffer;
import com.rscgl.net.Connection;
import com.rscgl.net.PacketBuffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.render.MobRenderContext;
import com.rscgl.render.SectorRenderContext;
import com.rscgl.screen.LoadingScreen;
import com.rscgl.screen.LoginScreen;
import com.rscgl.shader.ObjectShader;
import com.rscgl.util.MessageType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game implements ApplicationListener, PacketHandler {

    public static Game inst;
    public static int frameCounter;
    public static GLProfiler glProfiler;
    public final HashMap<Integer, PacketHandler> packetHandlers = new HashMap<Integer, PacketHandler>();
    protected final PacketBuffer outBuffer = new PacketBuffer(5000);
    protected final Buffer readBuffer = new Buffer(25000);
    public UI ui;
    public Connection connection;

    public PlayerEntity localPlayer = new PlayerEntity();
    public int localPlayerX;
    public int localPlayerY;

    public Vector3 lightDirection = new Vector3(0.4f, -0.8f, 0.4f);
    public Vector3 lightColor = new Vector3(1f, 1, 1);
    private GameState gameState = GameState.LOADING;
    private LoginScreen loginScreen;
    private ModelBatch modelBatch;
    private DecalBatch decalBatch;
    private ObjectShader objectShader;
    private long lastWrite = System.currentTimeMillis();
    private ExecutorService backgroundLoader = Executors.newSingleThreadExecutor();
    private GameCamera camera;
    private GameWorld world;
    private LoadingScreen loadingScreen;
    private ScalingViewport viewport;
    private int ourPlayerIndex;
    /**
     * Mouse picked entities
     */
    public final Map<EntityType, ArrayList<Entity>> pickedEntities = new EnumMap<>(EntityType.class);

    public Point2D lastPickedTile;

    private void clearPicked() {
        lastPickedTile = null;
        pickedEntities.values().forEach(ArrayList::clear);
    }

    public static PacketBuffer outBuffer() {
        return inst.outBuffer;
    }

    public static Viewport viewport() {
        return inst.viewport;
    }

    public static GameCamera cam() {
        return inst.camera;
    }

    public static UI ui() {
        return inst.ui;
    }

    public static GameWorld world() {
        return inst.world;
    }

    public static Connection net() {
        return inst.connection;
    }

    public void processConnection() {
        for (int i = 0; i < 5; i++) {
            if (!readConnection()) {
                break;
            }
        }
    }

    public boolean readConnection() {
        readBuffer.offset = 0;
        int packetLength = connection.decodePacket(readBuffer.dataBuffer);
        if (packetLength > 0) {
            int opcode = readBuffer.readUnsignedByte();

            try {
                PacketHandler packetHandler = Game.inst.packetHandlers.get(opcode);
                if (packetHandler != null) {
                    packetHandler.handlePacket(opcode, packetLength, readBuffer);
                } else {
                    System.out.println("Missing packet handler for " + opcode);
                }
            } catch (Exception e) {
                e.printStackTrace();
                closeConnection(true);
            }
            return true;
        }
        return false;
    }

    public void flushConnection() {
        if (connection == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWrite > 5000L) {
            outBuffer().newPacket(67);
            outBuffer().finishPacket();
        }
        if (outBuffer.offset <= 3) {
            return;
        }
        try {
            connection.write(outBuffer);
            lastWrite = currentTime;
        } catch (IOException e) {
            e.printStackTrace();
            lostConnection();
            return;
        }
        outBuffer.resetOffset();
    }

    private void lostConnection() {
        initLoginScreen();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClearColor(Config.FOG_COLOR.r, Config.FOG_COLOR.g, Config.FOG_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        switch (gameState) {
            case LOADING:
                loadingScreen.render(Gdx.graphics.getDeltaTime());
                break;
            case LOGIN:
                loginScreen.render(Gdx.graphics.getDeltaTime(), camera, modelBatch, decalBatch);
                break;
            case GAME:
                processConnection();
                flushConnection();

                camera.updatePosition();
                world.update();


                modelBatch.begin(camera);

                for (SectorRenderContext sector : world.getLoadedSectors().values()) {
                    modelBatch.render(sector);
                }

                for (Map.Entry<EntityType, ArrayList<Entity>> entry : world.getEntities().entrySet()) {
                    for (Entity entity : entry.getValue()) {
                        if (entity.getRenderContext() != null) {
                            entity.setPicked(false);
                            entity.getRenderContext().update(camera);
                            if (camera.inFrustum(entity.getPosition())) {
                                entity.getRenderContext().render(camera, modelBatch, decalBatch);
                                entity.setPicked(true);
                            }
                        }
                    }
                }

                modelBatch.end();
                decalBatch.flush();

                ui.refreshActionMenu();
                ui.render();

                glProfiler.reset();
                frameCounter++;
                break;
        }

    }

    public void processPicking(Ray ray) {
        //Clear picked
        pickedEntities.values().forEach(ArrayList::clear);

        for (Map.Entry<EntityType, ArrayList<Entity>> entry : world.getEntities().entrySet()) {
            for (Entity entity : entry.getValue()) {
                if (entity.getRenderContext() != null) {
                    if (entity.getRenderContext().rayHit(ray)) {
                        pickedEntities.get(entity.getEntityType()).add(entity);
                    }
                }
            }
        }

        for (SectorRenderContext sector : world.getLoadedSectors().values()) {
            Point2D t = sector.pickTerrainTile(ray);
            if (t != null) {
                lastPickedTile = t;
            }
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    public void closeConnection(boolean sendPacket) {
        if (sendPacket) {
            outBuffer().newPacket(31);
            outBuffer().finishPacket();
            flushConnection();
        }
        connection.close();
        world.reset();
        Game.inst.initLoginScreen();
    }

    @Override
    public void create() {
        glProfiler = new GLProfiler(Gdx.graphics);
        loadingScreen = new LoadingScreen();
        loginScreen = new LoginScreen();
        camera = new GameCamera();
        viewport = new ScalingViewport(Scaling.stretch, camera.viewportWidth, camera.viewportHeight, camera);
        world = new GameWorld();
        ui = new UI();

        objectShader = new ObjectShader();
        modelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(final Renderable renderable) {
                return objectShader;
            }
        });
        decalBatch = new DecalBatch(new CameraGroupStrategy(camera));

        inst = this;
    }

    @Override
    public void resize(int width, int height) {
        ui.resize(width, height);
        viewport.update(width, height, true);
        camera.update();
    }

    public void registerPacketHandler(PacketHandler handler) {
        for (int opcode : handler.opcodes())
            packetHandlers.put(opcode, handler);
    }

    public void init() {
        for (EntityType type : EntityType.values()) {
            pickedEntities.put(type, new ArrayList<>());
        }

        registerPacketHandler(this);
        glProfiler.enable();
        Assets.inst.loadGL();
        ui.setup();
    }

    public void initLoginScreen() {
        world.reset();
        setState(GameState.LOGIN);
    }

    public void setState(GameState state) {
        this.gameState = state;

        if (gameState == GameState.GAME) {
            GameInput gameInput = new GameInput(ui, camera, world);
            Gdx.input.setInputProcessor(gameInput);
            ui.getStage().setKeyboardFocus(ui.getChat().getChatInput());
        }

        if (gameState == GameState.LOGIN) {
            loginScreen.show();
        }
        if (gameState == GameState.LOADING) {
            loadingScreen.show();
        }
    }

    public void addLoadingTask(Runnable runnable) {
        backgroundLoader.submit(runnable);
    }

    public void setNet(Connection netConnection) {
        this.connection = netConnection;
    }

    public int wildernessLevel() {
        int wild = 2203 - (localPlayer.getGameX() + (1776 - (944 * world.currentPlane)));
        if (localPlayer.getGameZ() + 2304 >= 2640) {
            wild = -50;
        }
        if (wild > 0) {
            return 1 + wild / 6;
        }
        return 0;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void sendCommand(String str) {
        Game.outBuffer().newPacket(38);
        Game.outBuffer().putString(str);
        Game.outBuffer().finishPacket();
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 165) {
            Game.inst.closeConnection(false);
        } else if (opcode == 4) {
            Game.inst.closeConnection(true);
        } else if (opcode == 183) {
            ui.getChat().addMessage("@cya@Sorry you can't logout at this moment");
        } else if (opcode == 240) {
            boolean optionCameraModeAuto = packet.readUnsignedByte() == 1;
            boolean optionMouseButtonOne = packet.readUnsignedByte() == 1;
            boolean optionSoundDisabled = packet.readUnsignedByte() == 1;
        } else if (opcode == 25) {
            ourPlayerIndex = packet.readShort();
            GameWorld.WORLD_WIDTH = packet.readShort();
            GameWorld.WORLD_HEIGHT = packet.readShort();
            GameWorld.currentPlane = packet.readShort();
            GameWorld.planeOffset = packet.readShort();
            GameWorld.WORLD_HEIGHT -= world.currentPlane * world.planeOffset;
        }
        if (opcode == 191) {
            packet.startBitAccess();

            this.localPlayerX = packet.getBitMask(11);
            this.localPlayerY = packet.getBitMask(13);
            int direction = packet.getBitMask(4);

            float posX = 0.5F + this.localPlayerX * GameWorld.TSIZE;
            float posZ = 0.5F + this.localPlayerY * GameWorld.TSIZE;

            this.localPlayer = world.createPlayer(this.ourPlayerIndex, -posX, posZ, direction);

            ArrayList<PlayerEntity> removingPlayers = new ArrayList<>();

            int count = packet.getBitMask(8);
            for (int i = 0; count > i; ++i) {
                PlayerEntity player = world.getPlayers().get(i + 1);
                int needsUpdate = packet.getBitMask(1);
                if (needsUpdate == 1) {
                    int updateType = packet.getBitMask(1);
                    if (updateType == 0) {
                        int newDirection = packet.getBitMask(3);
                        int pathIndex = player.stepPointer;
                        float pathX = player.stepX[pathIndex];
                        float pathZ = player.stepZ[pathIndex];

                        if (newDirection == MobEntity.Direction.WEST || newDirection == MobEntity.Direction.NORTH_WEST || newDirection == MobEntity.Direction.SOUTH_WEST) {
                            pathX += GameWorld.TSIZE;
                        }
                        if (newDirection == MobEntity.Direction.EAST || newDirection == MobEntity.Direction.SOUTH_EAST || newDirection == MobEntity.Direction.NORTH_EAST) {
                            pathX -= GameWorld.TSIZE;
                        }
                        if (newDirection == MobEntity.Direction.SOUTH || newDirection == MobEntity.Direction.SOUTH_WEST || newDirection == MobEntity.Direction.SOUTH_EAST) {
                            pathZ += GameWorld.TSIZE;
                        }
                        if (newDirection == MobEntity.Direction.NORTH || newDirection == MobEntity.Direction.NORTH_WEST || newDirection == MobEntity.Direction.NORTH_EAST) {
                            pathZ -= GameWorld.TSIZE;
                        }

                        player.setPath((1 + pathIndex) % 10, pathX, pathZ);
                        player.setNextDirection(newDirection);
                    } else if (updateType == 1) {
                        int needsNextSprite = packet.getBitMask(2);
                        if (needsNextSprite == 3) {
                            removingPlayers.add(player);
                            continue;
                        }
                        player.setNextDirection(packet.getBitMask(2) + (needsNextSprite << 2));
                    }
                }
                //world.getPlayers().add(player);
            }

            for (PlayerEntity player : removingPlayers) {
                if (player.getRenderContext() != null) {
                    player.getRenderContext().dispose();
                    player.setRenderContext(null);
                }
                world.removeEntity(player);
            }

            while (length * 8 > packet.getBitHead() - -24) {
                int serverIndex = packet.getBitMask(11);
                int offsetX = packet.getBitMask(5);
                if (offsetX > 15) {
                    offsetX -= 32;
                }
                int offsetY = packet.getBitMask(5);
                if (offsetY > 15) {
                    offsetY -= 32;
                }
                int newDirection = packet.getBitMask(4);

                float worldX = (this.localPlayerX + offsetX) * GameWorld.TSIZE + 0.5F;
                float worldZ = (this.localPlayerY + offsetY) * GameWorld.TSIZE + 0.5F;

                world.createPlayer(serverIndex, -worldX, worldZ, newDirection);
            }

            packet.endBitAccess();
            return;
        }
        if (opcode == 79) {
            packet.startBitAccess();

            ArrayList<NpcEntity> removing = new ArrayList<>();

            int npcCount = packet.getBitMask(8);
            for (int i = 0; npcCount > i; ++i) {
                NpcEntity npc = world.getNpcs().get(i);
                int needsUpdate = packet.getBitMask(1);
                if (needsUpdate != 0) {
                    int updateType = packet.getBitMask(1);
                    if (updateType != 0) {
                        int nextSpriteOffset = packet.getBitMask(2);
                        if (nextSpriteOffset == 3) {
                            removing.add(npc);
                            continue;
                        }
                        npc.setNextDirection(packet.getBitMask(2) + (nextSpriteOffset << 2));
                    } else {
                        int newDirection = packet.getBitMask(3);
                        int pathIndex = npc.stepPointer;
                        float pathX = npc.stepX[pathIndex];
                        float pathZ = npc.stepZ[pathIndex];

                        if (newDirection == MobEntity.Direction.WEST || newDirection == MobEntity.Direction.NORTH_WEST || newDirection == MobEntity.Direction.SOUTH_WEST) {
                            pathX += GameWorld.TSIZE;
                        }
                        if (newDirection == MobEntity.Direction.EAST || newDirection == MobEntity.Direction.SOUTH_EAST || newDirection == MobEntity.Direction.NORTH_EAST) {
                            pathX -= GameWorld.TSIZE;
                        }
                        if (newDirection == MobEntity.Direction.SOUTH || newDirection == MobEntity.Direction.SOUTH_WEST || newDirection == MobEntity.Direction.SOUTH_EAST) {
                            pathZ += GameWorld.TSIZE;
                        }
                        if (newDirection == MobEntity.Direction.NORTH || newDirection == MobEntity.Direction.NORTH_WEST || newDirection == MobEntity.Direction.NORTH_EAST) {
                            pathZ -= GameWorld.TSIZE;
                        }

                        npc.setPath((1 + pathIndex) % 10, pathX, pathZ);
                        npc.setNextDirection(newDirection);
                    }
                }
            }

            for (NpcEntity npcEntity : removing) {
                world.removeEntity(npcEntity);
                if (npcEntity.getRenderContext() != null) {
                    npcEntity.getRenderContext().dispose();
                }
            }

            int createCount = 0;
            while (packet.getBitHead() + 34 < length * 8) {
                int serverIndex = packet.getBitMask(16);
                int offsetX = packet.getBitMask(5);
                if (offsetX > 15) {
                    offsetX -= 32;
                }
                int offsetY = packet.getBitMask(5);
                if (offsetY > 15) {
                    offsetY -= 32;
                }
                int direction = packet.getBitMask(4);
                int type = packet.getBitMask(10);

                float worldX = (this.localPlayerX + offsetX) * GameWorld.TSIZE + 0.5F;
                float worldZ = (this.localPlayerY + offsetY) * GameWorld.TSIZE + 0.5F;
                if (type >= RSCache.NPC_COUNT) {
                    type = 24;
                }
                world.createNpc(serverIndex, type, -worldX, worldZ, direction);
                createCount++;
            }

            packet.endBitAccess();
            return;
        }
        if (opcode == 48) {
            while (length > packet.offset) {
                if (packet.readUnsignedByte() != 255) {
                    --packet.offset;
                    int type = packet.readShort();
                    int localX = localPlayerX + packet.readUnsignedShort();
                    int localY = localPlayerY + packet.readUnsignedShort();
                    int direction = packet.readByte();

                    ObjectEntity object = world.getObject(localX, localY);
                    if (object != null) {
                        world.unregisterObject(object);
                    }
                    /* Create a new object */
                    if (type != 60000) {
                        world.registerObject(type, localX, localY, direction);
                    }
                } else {
                    /* 8x8 grid object clean up here. Removed for now . */
                }
            }
            return;
        }
        if (opcode == 91) {
            while (length > packet.offset) {
                if (packet.readUnsignedByte() != 255) {
                    --packet.offset;
                    int type = packet.readShort();
                    int localX = localPlayerX + packet.readUnsignedShort();
                    int localY = localPlayerY + packet.readUnsignedShort();
                    int direction = packet.readByte();
                    WallEntity wallEntity = world.getWallObject(localX, localY);
                    if (wallEntity != null) {
                        world.unregisterWallObject(wallEntity);
                    }
                    if (type != 60000) {
                        world.registerWallObject(localX, localY, type, direction);
                    }
                } else {
                    /* 8x8 grid object clean up here. Removed for now . */
                }
            }
            return;
        }
        if (opcode == 99) {
            while (length > packet.offset) {
                if (packet.readUnsignedByte() != 255) {
                    --packet.offset;
                    int itemID = packet.readShort();
                    int localX = this.localPlayerX + packet.readUnsignedShort();
                    int localY = this.localPlayerY + packet.readUnsignedShort();
                    if ((itemID & 32768) != 0) {
                        itemID &= 32767;
                        ItemEntity itemEntity = world.getItem(localX, localY, itemID);
                        if (itemEntity != null) {
                            world.removeEntity(itemEntity);
                        }
                    } else {
                        world.registerItem(itemID, localX, localY);
                    }
                } else {
                    /* 8x8 grid object clean up here. Removed for now . */
                }
            }
            return;
        }

        if (opcode == 104) {
            int updateCount = packet.readShort();
            /*for (int count = 0; updateCount > count; ++count) {
                int npcServerIndex = packet.readShort();
                NpcEntity npc = world.getNpc(npcServerIndex);

                int updateType = packet.readUnsignedByte();
                if (updateType == 1) {
                    int recipientIndex = packet.readShort();
                    String message = packet.readString();
                    npc.message = message;
                    npc.timerMessage = 150;
                    if (this.localPlayer.getServerIndex() == recipientIndex) {
                        NPCDef npcDef = Cache.getNpcDef(npc.getType());
                        Core.ui().getChat().addMessage(npcDef.getName() + ": " + message, MessageType.CHAT);
                    }
                } else if (updateType == 2) {
                    int type = packet.readByte();
                    int damage = packet.readShort();
                    int currentHealth = packet.readShort();
                    int maxHealth = packet.readShort();
                    if (npc != null) {
                        npc.healthCurrent = currentHealth;
                        npc.healthMax = maxHealth;
                        if (type == 0) {
                            npc.damageTaken = damage;
                            npc.timerDamage = 200;
                        }
                        npc.timerCombat = 200;
                    }
                }
            }*/
            return;
        }

        if (opcode == 234) {
            int playerCount = packet.readShort();
            for (int i = 0; playerCount > i; ++i) {
                int index = packet.readShort();
                PlayerEntity player = world.getPlayer(index); //this.playersServer[index];
                int updateType = packet.readByte();
                if (updateType == 0) {
                    int itemType = packet.readShort();
                    if (player != null) {
                        player.bubbleItem = itemType;
                        player.timerBubble = 150;
                    }
                } else if (updateType == 1) {
                    if (player != null) {
                        int crownID = packet.readUnsignedByte();
                        String message = packet.readString();

                        player.timerMessage = 150;
                        player.message = "@yel@" + message;

                        Game.ui().getChat().addMessage(player.username + ": " + message, MessageType.CHAT);
                    }
                } else if (updateType == 2) {
                    int damage = packet.readShort();
                    int newHP = packet.readShort();
                    int maxHP = packet.readShort();
                    if (player != null) {
                        player.healthMax = maxHP;
                        player.healthCurrent = newHP;

                        player.damageTaken = damage;
                        player.timerDamage = 200;
                        player.timerCombat = 200;

                        if (this.localPlayer == player) {
                            Game.ui().getStatMenu().getSkills().setCurStat(3, newHP);
                            Game.ui().getStatMenu().getSkills().setMaxStat(3, maxHP);
                            Game.ui().getStatMenu().getSkills().update();
                            /*
                                this.showDialogServerMessage = false;
                                this.showDialogMessage = false;
                            */
                        }
                    }
                } else if (updateType == 3) {
                    int sprite = packet.readShort();
                    int shooterServerIndex = packet.readShort();
                    if (player != null) {
                       /* player.projectileTravel = PROJECTILE_MAX_RANGE;
                        player.projectileTarget = getServerNPC(shooterServerIndex);
                        player.incomingProjectileSprite = sprite;*/
                    }
                } else if (updateType == 4) {
                    int sprite = packet.readShort();
                    int shooterServerIndex = packet.readShort();
                    if (player != null) {
                       /* player.projectileTarget = getServerPlayer(shooterServerIndex);
                        player.projectileTravel = PROJECTILE_MAX_RANGE;
                        player.incomingProjectileSprite = sprite;*/
                    }
                }
                if (updateType == 6 && player != null) {
                    player.message = packet.readString();
                    player.timerMessage = 150;
                    if (this.localPlayer == player) {
                        Game.ui().getChat().addMessage(player.username + ": " + player.message, MessageType.QUEST);
                    }
                } else if (updateType == 5) {
                    if (player != null) {
                        player.username = packet.readString();
                        int itemCount = packet.readUnsignedByte();
                        for (int j = 0; j < itemCount; ++j) {
                            int animationID = packet.readShort() - 1;
                            if (animationID > -1 && animationID < RSCache.ANIMATION_COUNT)
                                player.layerAnimation[j] = RSCache.ANIMATIONS[animationID];
                        }

                        for (int j = itemCount; j < 12; ++j) {
                            player.layerAnimation[j] = null;
                        }

                        player.colourHair = packet.readUnsignedByte();
                        player.colourTop = packet.readUnsignedByte();
                        player.colourBottom = packet.readUnsignedByte();
                        player.colourSkin = packet.readUnsignedByte();
                        player.combatLevel = packet.readUnsignedByte();
                        player.skullType = packet.readUnsignedByte();

                        if (player.getRenderContext() != null) {
                            MobRenderContext renderContext = (MobRenderContext) player.getRenderContext();
                            renderContext.generateAppearance();
                        }
                    }
                }
            }
            return;
        }
    }

    @Override
    public int[] opcodes() {
        return new int[]{165, 4, 183, 134, 240, 104, 79, 234, 191, 91, 48, 25, 99, 88};
    }

    public <E extends Entity> ArrayList<E> getPicked(EntityType type) {
        return (ArrayList<E>) pickedEntities.get(type);
    }

    public enum GameState {
        LOADING,
        LOGIN,
        GAME,
    }
}
