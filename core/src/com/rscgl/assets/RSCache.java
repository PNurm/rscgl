package com.rscgl.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rscgl.Config;
import com.rscgl.assets.def.*;
import com.rscgl.assets.util.Utility;

public class RSCache {

    public static int SPELL_COUNT;
    public static int ITEM_COUNT;
    public static int itemSpriteCount;
    public static int TILE_COUNT;
    public static int WALL_COUNT;
    public static int MODEL_COUNT;
    public static int PRAYER_COUNT;

    public static int projectileSprite;
    public static int NPC_COUNT;
    public static int TEXTURE_COUNT;
    public static int ROOF_COUNT;
    public static int OBJECT_COUNT;
    public static int ANIMATION_COUNT;

    static byte[] dataString;
    static byte[] dataInteger;
    static int stringOffset;
    static int offset;

    public static ItemDef[] ITEMS;
    public static NPCDef[] NPCS;
    public static TextureDef[] TEXTURES;
    public static AnimationDef[] ANIMATIONS;
    public static GameObjectDef[] OBJECTS;
    public static WallObjectDef[] WALLS;
    public static RoofDef[] ROOFS;
    public static TileDef[] TILES;
    public static SpellDef[] SPELLS;
    public static PrayerDef[] PRAYERS;

    public static int getUnsignedByte() {
        int i = dataInteger[offset] & 0xff;
        offset++;
        return i;
    }

    public static int getUnsignedShort() {
        int i = Utility.getUnsignedShort(dataInteger, offset);
        offset += 2;
        return i;
    }

    public static int getUnsignedInt() {
        int i = Utility.getUnsignedInt(dataInteger, offset);
        offset += 4;
        if (i > 99999999)
            i = 99999999 - i;
        return i;
    }

    public static String getString() {
        String s;
        for (s = ""; dataString[stringOffset] != 0; s = s + (char) dataString[stringOffset++]) ;
        stringOffset++;
        return s;
    }

    public static void loadData() {
        FileHandle configFile = Gdx.files.local(Config.CACHE_DIR + "config85.jag");
        byte[] configData = Utility.readDataFile(configFile);
        if (configData == null) {
            throw new NullPointerException("Failed to load config data");
        }

        dataString = Utility.uncompressData("string.dat", 0, configData);
        stringOffset = 0;
        dataInteger = Utility.uncompressData("integer.dat", 0, configData);
        offset = 0;

        /**
         * Load items
         */
        int i;
        ITEM_COUNT = getUnsignedShort();
        ITEMS = new ItemDef[ITEM_COUNT];
        for (i = 0; i < ITEM_COUNT; i++) {
            ITEMS[i] = new ItemDef();
            ITEMS[i].name = getString();
        }
        for (i = 0; i < ITEM_COUNT; i++)
            ITEMS[i].description = getString();
        for (i = 0; i < ITEM_COUNT; i++)
            ITEMS[i].command = getString();

        for (i = 0; i < ITEM_COUNT; i++) {
            ITEMS[i].sprite = getUnsignedShort();
            if (ITEMS[i].sprite + 1 > itemSpriteCount) {
                itemSpriteCount = ITEMS[i].sprite + 1;
            }
        }
        for (i = 0; i < ITEM_COUNT; i++) {
            ITEMS[i].basePrice = getUnsignedInt();
        }
        for (i = 0; i < ITEM_COUNT; i++) {
            ITEMS[i].stackable = getUnsignedByte() == 1;
        }
        for (i = 0; i < ITEM_COUNT; i++) {
            ITEMS[i].unknown = getUnsignedByte() == 1;
        }
        for (i = 0; i < ITEM_COUNT; i++) {
            ITEMS[i].wearable = getUnsignedShort();
        }
        for (i = 0; i < ITEM_COUNT; i++) {
            ITEMS[i].colour = getUnsignedInt();
        }
        for (i = 0; i < ITEM_COUNT; i++) {
            ITEMS[i].tradable = getUnsignedByte() == 1;
        }
        for (i = 0; i < ITEM_COUNT; i++) {
            ITEMS[i].members = getUnsignedByte() == 1;
        }

        /*for (i = 0; i < itemCount; i++)
            if (!isMembers && itemMembers[i] == 1) {
                itemName[i] = "Members object";
                itemDescription[i] = "You need to be a member to use this object";
                itemBasePrice[i] = 0;
                itemCommand[i] = "";
                itemUnused[0] = 0;
                itemWearable[i] = 0;
                itemSpecial[i] = 1;
            }*/

        /**
         * Load npcs
         */
        NPC_COUNT = getUnsignedShort();
        NPCS = new NPCDef[NPC_COUNT];

        for (i = 0; i < NPC_COUNT; i++) {
            NPCS[i] = new NPCDef();
            NPCS[i].name = getString();
        }

        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].description = getString();

        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].attack = getUnsignedByte();

        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].strength = getUnsignedByte();

        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].hits = getUnsignedByte();

        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].defense = getUnsignedByte();

        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].attackable = getUnsignedByte() == 1;

        for (i = 0; i < NPC_COUNT; i++) {
            for (int j = 0; j < 12; j++) {
                NPCS[i].sprites[j] = getUnsignedByte();
                if (NPCS[i].sprites[j] == 255)
                    NPCS[i].sprites[j] = -1;
            }
        }

        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].hairColour = getUnsignedInt();
        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].topColour = getUnsignedInt();
        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].bottomColour = getUnsignedInt();
        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].skinColour = getUnsignedInt();
        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].width = getUnsignedShort();
        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].height = getUnsignedShort();
        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].walkModel = getUnsignedByte();
        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].combatModel = getUnsignedByte();
        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].combatSprite = getUnsignedByte();
        for (i = 0; i < NPC_COUNT; i++)
            NPCS[i].command = getString();

        /**
         * Load texture info
         */
        TEXTURE_COUNT = getUnsignedShort();
        TEXTURES = new TextureDef[TEXTURE_COUNT];

        for (i = 0; i < TEXTURE_COUNT; i++) {
            TEXTURES[i] = new TextureDef();
            TEXTURES[i].textureName = getString();
        }
        for (i = 0; i < TEXTURE_COUNT; i++) {
            TEXTURES[i].overlayName = getString();
        }

        /**
         * Load sprite animation pack data
         */
        ANIMATION_COUNT = getUnsignedShort();
        ANIMATIONS = new AnimationDef[ANIMATION_COUNT];

        for (i = 0; i < ANIMATION_COUNT; i++) {
            ANIMATIONS[i] = new AnimationDef();
            ANIMATIONS[i].name = getString();
        }
        for (i = 0; i < ANIMATION_COUNT; i++)
            ANIMATIONS[i].charColour = getUnsignedInt();
        for (i = 0; i < ANIMATION_COUNT; i++)
            ANIMATIONS[i].genderModel = getUnsignedByte();
        for (i = 0; i < ANIMATION_COUNT; i++)
            ANIMATIONS[i].combatFrames = getUnsignedByte() == 1;
        for (i = 0; i < ANIMATION_COUNT; i++)
            ANIMATIONS[i].walkFrames = getUnsignedByte() == 1;
        for (i = 0; i < ANIMATION_COUNT; i++) {
            ANIMATIONS[i].spriteIndex = getUnsignedByte();
        }

        OBJECT_COUNT = getUnsignedShort();
        OBJECTS = new GameObjectDef[OBJECT_COUNT];

        for (i = 0; i < OBJECT_COUNT; i++) {
            OBJECTS[i] = new GameObjectDef();
            OBJECTS[i].name = getString();
        }

        for (i = 0; i < OBJECT_COUNT; i++)
            OBJECTS[i].description = getString();
        for (i = 0; i < OBJECT_COUNT; i++)
            OBJECTS[i].option1 = getString();
        for (i = 0; i < OBJECT_COUNT; i++)
            OBJECTS[i].option2 = getString();
        for (i = 0; i < OBJECT_COUNT; i++)
            OBJECTS[i].modelID = RSModels.getModelIndex(getString());
        for (i = 0; i < OBJECT_COUNT; i++)
            OBJECTS[i].width = getUnsignedByte();
        for (i = 0; i < OBJECT_COUNT; i++)
            OBJECTS[i].height = getUnsignedByte();
        for (i = 0; i < OBJECT_COUNT; i++)
            OBJECTS[i].type = getUnsignedByte();
        for (i = 0; i < OBJECT_COUNT; i++)
            OBJECTS[i].itemHeight = getUnsignedByte();

        WALL_COUNT = getUnsignedShort();
        WALLS = new WallObjectDef[WALL_COUNT];
        for (i = 0; i < WALL_COUNT; i++) {
            WALLS[i] = new WallObjectDef();
            WALLS[i].name = getString();
        }

        for (i = 0; i < WALL_COUNT; i++)
            WALLS[i].description = getString();
        for (i = 0; i < WALL_COUNT; i++)
            WALLS[i].option1 = getString();
        for (i = 0; i < WALL_COUNT; i++)
            WALLS[i].option2 = getString();
        for (i = 0; i < WALL_COUNT; i++)
            WALLS[i].height = getUnsignedShort();
        for (i = 0; i < WALL_COUNT; i++)
            WALLS[i].frontTexture = getUnsignedInt();
        for (i = 0; i < WALL_COUNT; i++)
            WALLS[i].backTexture = getUnsignedInt();
        for (i = 0; i < WALL_COUNT; i++)
            WALLS[i].adjacent = getUnsignedByte();
        for (i = 0; i < WALL_COUNT; i++)
            WALLS[i].invisible = getUnsignedByte();

        ROOF_COUNT = getUnsignedShort();
        ROOFS = new RoofDef[ROOF_COUNT];
        for (i = 0; i < ROOF_COUNT; i++) {
            ROOFS[i] = new RoofDef();
            ROOFS[i].height = getUnsignedByte();
        }
        for (i = 0; i < ROOF_COUNT; i++)
            ROOFS[i].numVertices = getUnsignedByte();


        TILE_COUNT = getUnsignedShort();// and these
        System.out.println("Tile count "  + TILE_COUNT);
        TILES = new TileDef[TILE_COUNT];
        for (i = 0; i < TILE_COUNT; i++) {
            TILES[i] = new TileDef();
            TILES[i].colour = getUnsignedInt();
        }
        for (i = 0; i < TILE_COUNT; i++) //TODO: Guessed these.
            TILES[i].type = getUnsignedByte();
        for (i = 0; i < TILE_COUNT; i++)
            TILES[i].adjacent = getUnsignedByte();

        projectileSprite = getUnsignedShort();

        SPELL_COUNT = getUnsignedShort();

        SPELLS = new SpellDef[SPELL_COUNT];
        for (i = 0; i < SPELL_COUNT; i++) {
            SPELLS[i] = new SpellDef();
            SPELLS[i].name = getString();
        }

        for (i = 0; i < SPELL_COUNT; i++)
            SPELLS[i].description = getString();

        for (i = 0; i < SPELL_COUNT; i++)
            SPELLS[i].reqLevel = getUnsignedByte();

        for (i = 0; i < SPELL_COUNT; i++)
            SPELLS[i].runeCount = getUnsignedByte();

        for (i = 0; i < SPELL_COUNT; i++)
            SPELLS[i].type = getUnsignedByte();

        int[][] spellRunesId = new int[SPELL_COUNT][];
        int[][] spellRunesCount = new int[SPELL_COUNT][];
        for (i = 0; i < SPELL_COUNT; i++) {
            int runeCount = getUnsignedByte();
            spellRunesId[i] = new int[runeCount];
            for (int k = 0; k < runeCount; k++)
                spellRunesId[i][k] = getUnsignedShort();
        }
        for (i = 0; i < SPELL_COUNT; i++) {
            int count = getUnsignedByte();
            spellRunesCount[i] = new int[count];
            for (int k = 0; k < count; k++)
                spellRunesCount[i][k] = getUnsignedByte();
        }

        for (i = 0; i < SPELL_COUNT; i++) {
            for(int j = 0; j < SPELLS[i].runeCount; j++) {
                //SPELLS[i].requiredRunes.put(spellRunesId[i][j], spellRunesCount[i][j]);
            }
        }

        PRAYER_COUNT = getUnsignedShort();
        PRAYERS = new PrayerDef[PRAYER_COUNT];
        for (i = 0; i < PRAYER_COUNT; i++) {
            PRAYERS[i] = new PrayerDef();
            PRAYERS[i].name = getString();
        }
        for (i = 0; i < PRAYER_COUNT; i++)
            PRAYERS[i].description = getString();
        for (i = 0; i < PRAYER_COUNT; i++)
            PRAYERS[i].reqLevel = getUnsignedByte();
        for (i = 0; i < PRAYER_COUNT; i++)
            PRAYERS[i].drainRate = getUnsignedByte();

        dataString = null;
        dataInteger = null;
    }

}
