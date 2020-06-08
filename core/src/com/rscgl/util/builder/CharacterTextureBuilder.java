package com.rscgl.util.builder;

import com.badlogic.gdx.graphics.Texture;
import com.rscgl.Game;
import com.rscgl.assets.Assets;
import com.rscgl.assets.def.AnimationDef;
import com.rscgl.assets.model.Sprite;
import com.rscgl.model.entity.MobEntity;
import com.rscgl.model.entity.NpcEntity;

public class CharacterTextureBuilder extends SpriteTextureBuilder {

    private final static int[][] layerRenderOrderForDirection = new int[][]{
            {11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 3, 4},
            {11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 3, 4},
            {11, 3, 2, 9, 7, 1, 6, 10, 0, 5, 8, 4},
            {3, 4, 2, 9, 7, 1, 6, 10, 8, 11, 0, 5},
            {3, 4, 2, 9, 7, 1, 6, 10, 8, 11, 0, 5},
            {4, 3, 2, 9, 7, 1, 6, 10, 8, 11, 0, 5},
            {11, 4, 2, 9, 7, 1, 6, 10, 0, 5, 8, 3},
            {11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 4, 3}};

    private static final int FRAMES_PER_ANGLE = 3;

    private static final int combatSeqLength = 8;
    private static final int[] combatSeqA = new int[]{0, 1, 2, 1, 0, 0, 0, 0};
    private static final int[] combatSeqB = new int[]{0, 0, 0, 0, 0, 1, 2, 1};

    private static final int walkSeqLength = 4;
    private static final int[] walkSeq = new int[]{0, 1, 2, 1};

    private static final int SPRITE_FRONT = 0;//0-3
    private static final int SPRITE_NORTH_EAST = 1;//3-6
    private static final int SPRITE_EAST = 2;//6-9
    private static final int SPRITE_SOUTH_EAST = 3;//9-12
    private static final int SPRITE_BACK = 4;//12-15
    private static final int SPRITE_COMBAT = 5;//15-18
    /**
     * The following are mirrored, sprites for them don't exist.
     */
    private static final int ANGLE_SOUTH_WEST = 5;
    private static final int ANGLE_WEST = 6;
    private static final int ANGLE_NORTH_WEST = 7;

    private final int[] playerSkinColors = new int[]{15523536, 13415270, 11766848, 10056486, 9461792};
    private final int[] playerClothingColors = new int[]{16711680, 16744448, 16769024, 10543104, '\ue000', '\u8000',
            '\ua080', '\ub0ff', '\u80ff', 12528, 14680288, 3158064, 6307840, 8409088, 16777215};
    private final int[] playerHairColors = new int[]{16760880, 16752704, 8409136, 6307872, 3158064, 16736288,
            16728064, 16777215, '\uff00', '\uffff'};

    public CharacterTextureBuilder(int width, int height) {
        super(width, height);
    }

    public Texture build(BuilderSettings settings) {
        reset();

        boolean flipSpriteImage = settings.mirror;
        int spriteDir = settings.spriteDir;
        int renderDir = settings.renderDir;
        int frameOffset = settings.frameOffset;
        MobEntity mob = settings.mob;

        for (int count = 0; count < 12; ++count) {
            int layer = layerRenderOrderForDirection[spriteDir][count];
            AnimationDef animDef = mob.layerAnimation[layer];

            if (animDef == null) {
                continue;
            }

            int offsetX = 0;
            int offsetY = 0;
            int id = frameOffset;

            int nextStepFrame = (mob.stepSprite / 6 + 2) % walkSeqLength;

            if (flipSpriteImage && renderDir >= 1 && renderDir <= 3) {
                if (animDef.hasF()) {
                    id = frameOffset + 15;
                } else if (layer == 4 && renderDir == 1) {
                    id = (renderDir * FRAMES_PER_ANGLE) + walkSeq[nextStepFrame];
                    offsetY = -3;
                    offsetX = -22;
                } else if (layer == 4 && renderDir == 2) {
                    id = (renderDir * FRAMES_PER_ANGLE) + walkSeq[nextStepFrame];
                    offsetX = 0;
                    offsetY = -8;
                } else if (layer == 4 && renderDir == 3) {
                    id = (renderDir * FRAMES_PER_ANGLE) + walkSeq[nextStepFrame];
                    offsetY = -5;
                    offsetX = 26;
                } else if (layer == 3 && renderDir == 1) {
                    id = (renderDir * FRAMES_PER_ANGLE) + walkSeq[nextStepFrame];
                    offsetX = 22;
                    offsetY = 3;
                } else if (layer == 3 && renderDir == 2) {
                    id = (renderDir * FRAMES_PER_ANGLE) + walkSeq[nextStepFrame];
                    offsetY = 8;
                    offsetX = 0;
                } else if (layer == 3) {
                    id = (renderDir * FRAMES_PER_ANGLE) + walkSeq[nextStepFrame];
                    offsetX = -26;
                    offsetY = 5;
                }
            }

            if (renderDir != SPRITE_COMBAT || animDef.hasCombatSprites()) {
                Sprite sprite = Assets.inst.getAnim(animDef.spriteIndex, id);
                if(sprite == null) {
                    continue;
                }
                int boundWidth = sprite.spriteWidth;
                int boundHeight = sprite.spriteHeight;
                int firstFrame = Assets.inst.getAnim(animDef.spriteIndex, 0).spriteWidth;
                if (boundWidth == 0 && boundHeight == 0 && firstFrame == 0)
                    continue;

                int xOffset = (offsetX * screenWidth) / boundWidth;
                int yOffset = (offsetY * screenHeight) / boundHeight;
                int spriteWidth = (boundWidth * screenWidth) / firstFrame;
                //xOffset -= (spriteWidth - screenWidth) / 2;

                int grayScale = animDef.getCharColour();
                int redScale = mob instanceof NpcEntity ? mob.colourSkin : playerSkinColors[mob.colourSkin];
                int blueScale = animDef.blueColorMask;

                if (grayScale == 1) {
                    grayScale = mob instanceof NpcEntity ? mob.colourHair : playerHairColors[mob.colourHair];
                } else if (grayScale == 2) {
                    grayScale = mob instanceof NpcEntity ? mob.colourTop : playerClothingColors[mob.colourTop];
                } else if (grayScale == 3) {
                    grayScale = mob instanceof NpcEntity ? mob.colourBottom : playerClothingColors[mob.colourBottom];
                }
                draw(sprite, xOffset, yOffset, screenWidth, screenHeight, grayScale, redScale, blueScale, 255, flipSpriteImage, 0);
            }
        }
        return new Texture(toPixmap());
    }

    public static class BuilderSettings {

        private final MobEntity mob;

        public int frameOffset;
        public int renderDir;

        public boolean mirror;
        public int spriteDir;

        public BuilderSettings(int sprite, MobEntity mob) {
            this.mob = mob;
            this.spriteDir = sprite;
            this.renderDir = sprite;
            this.mirror = false;

            if (spriteDir == ANGLE_SOUTH_WEST) {
                renderDir = SPRITE_SOUTH_EAST;
                mirror = true;
            } else if (spriteDir == ANGLE_WEST) {
                renderDir = SPRITE_EAST;
                mirror = true;
            } else if (spriteDir == ANGLE_NORTH_WEST) {
                renderDir = SPRITE_NORTH_EAST;
                mirror = true;
            }
            frameOffset = walkSeq[mob.stepSprite / 6 % 4] + renderDir * FRAMES_PER_ANGLE;
            if (mob.getDirection() == MobEntity.Direction.COMBAT_B) {
                spriteDir = 2;
                frameOffset = combatSeqB[Game.frameCounter / 6 % 8] + renderDir * FRAMES_PER_ANGLE;
                renderDir = SPRITE_COMBAT;
                mirror = true;
            } else if (mob.getDirection() == MobEntity.Direction.COMBAT_A) {
                spriteDir = 2;
                frameOffset = combatSeqA[Game.frameCounter / 5 % 8] + renderDir * FRAMES_PER_ANGLE;
                renderDir = SPRITE_COMBAT;
                mirror = false;
            }
        }
    }

}
