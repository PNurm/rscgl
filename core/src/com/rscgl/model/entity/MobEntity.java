package com.rscgl.model.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.rscgl.Game;
import com.rscgl.assets.Assets;
import com.rscgl.assets.def.AnimationDef;
import com.rscgl.GameWorld;

import static com.rscgl.GameWorld.TSIZE;

public class MobEntity extends Entity {

    public static final float MOVE_PER_UPDATE = 3F / 32F;

    public AnimationDef[] layerAnimation = new AnimationDef[12];
    public int colourBottom;
    public int colourHair;
    public int colourSkin;
    public int colourTop;
    public int bubbleItem;
    public int damageTaken = 0;
    public int healthCurrent = 0;
    public int healthMax = 1;
    public int combatLevel = -1;
    public int currentStep;
    public int stepPointer;
    public float[] stepX = new float[10];
    public float[] stepZ = new float[10];
    public String message;

    public int stepSprite;

    public int timerMessage;
    public int timerBubble;
    public int timerCombat;
    public int timerDamage;

    public float projectileTravelDistance = 0;
    private int direction = 0;
    private int nextDirection = 0;

    private int projectileTargetNpc;
    private int projectileTargetPlayer;
    private int projectileMaxRange = 100;

    private Vector3 projectilePosition = new Vector3();
    private Vector3 projectileStart = new Vector3();
    private Vector3 projectileDest = new Vector3();
    private Matrix4 projectileTransform = new Matrix4();

    public MobEntity(EntityType entityType) {
        super(entityType);
    }

    public void setPath(int index, float x, float z) {
        this.stepPointer = index;
        this.stepX[index] = x;
        this.stepZ[index] = z;
    }

    public void resetMovement() {
        currentStep = 0;
        stepPointer = 0;
        stepSprite = 0;
    }

    public void setNextDirection(int sprite) {
        this.nextDirection = sprite;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }


    public void update(GameWorld world, Camera camera) {
        position.y = world.getInterpolatedElevation(position.x, position.z);

        updateProjectile();
    }

    public boolean inCombat() {
        return direction == Direction.COMBAT_A || direction == Direction.COMBAT_B;
    }

    public void tick() {
        int nextStep = (1 + this.stepPointer) % 10;
        if (currentStep == nextStep) {
            direction = nextDirection;
        } else {
            int nextDir = -1;
            int stepCount;
            if (currentStep < nextStep) {
                stepCount = nextStep - currentStep;
            } else {
                stepCount = 10 + nextStep - currentStep;
            }
            float unitsToMove = MOVE_PER_UPDATE;
            if (stepCount > 2) {
                unitsToMove = (stepCount * MOVE_PER_UPDATE - MOVE_PER_UPDATE);
            }

            float deltaX = stepX[currentStep] - position.x;
            float deltaZ = stepZ[currentStep] - position.z;

            if (deltaX > TSIZE * 3 ||
                    deltaZ > TSIZE * 3 ||
                    deltaX < -TSIZE * 3 ||
                    deltaZ < -TSIZE * 3 && stepCount > 8) {
                position.x = stepX[currentStep];
                position.z = stepZ[currentStep];
            } else {
                if (deltaX < 0) {
                    position.x -= unitsToMove;
                    ++stepSprite;
                    nextDir = Direction.WEST;
                } else if (deltaX > 0) {
                    position.x += unitsToMove;
                    ++stepSprite;
                    nextDir = Direction.EAST;
                }

                if (deltaZ > 0) {
                    position.z += unitsToMove;
                    ++stepSprite;

                    if (nextDir > -1) {
                        if (nextDir == Direction.WEST) {
                            nextDir = Direction.SOUTH_WEST;
                        } else {
                            nextDir = Direction.SOUTH_EAST;
                        }
                    } else {
                        nextDir = Direction.SOUTH;
                    }
                } else if (deltaZ < 0) {
                    position.z -= unitsToMove;
                    ++stepSprite;

                    if (nextDir > -1) {
                        if (nextDir == Direction.WEST) {
                            nextDir = Direction.NORTH_WEST;
                        } else {
                            nextDir = Direction.NORTH_EAST;
                        }
                    } else {
                        nextDir = Direction.NORTH;
                    }
                }

                if (-deltaX < unitsToMove && -deltaX > -unitsToMove) {
                    position.x = stepX[currentStep];
                }
                if (-deltaZ < unitsToMove && -deltaZ > -unitsToMove) {
                    position.z = stepZ[currentStep];
                }
            }

            if (nextDir > -1) {
                direction = nextDir;
            }
            if (stepX[currentStep] == position.x && stepZ[currentStep] == position.z) {
                currentStep = (1 + currentStep) % 10;
            }
        }

        if (timerMessage > 0) {
            timerMessage--;
        }
        if (timerBubble > 0) {
            timerBubble--;
        }
        if (timerCombat > 0) {
            timerCombat--;
        }

        float glY = Game.world().getInterpolatedElevation(getPosition().x, getPosition().z);
        position.y = glY;
    }

    public int getGameX() {
        return (int) -((position.x - TSIZE / 2) / TSIZE);
    }

    public int getGameZ() {
        return (int) ((position.z - TSIZE / 2) / TSIZE);
    }

    public Vector3 position() {
        return position;
    }

    public void setProjectile(int sprite, int playerIndex, int npcIndex) {
        this.projectileTravelDistance = projectileMaxRange;
        this.projectileTargetPlayer = playerIndex;
        this.projectileTargetNpc = npcIndex;
        float ourX = position.x;
        float ourZ = position.z;
        float projectileHeight = Game.world().getInterpolatedElevation(ourX, ourZ) + 2.5F;
        projectileStart.set(position.x, projectileHeight, position.z);

        updateProjectile();
    }

    public void updateProjectile() {
        /*if (projectile != null) {
            projectileTravelDistance -= Gdx.graphics.getDeltaTime() * 100F;
            updateProjectileDest();
            float state = (projectileMaxRange - projectileTravelDistance) / projectileMaxRange;
            if (state < 1.0f) {
                projectilePosition.set(projectileStart.cpy().lerp(projectileDest, state));
                projectileTransform.setToTranslation(projectilePosition);

            } else {

                projectileTravelDistance = 0;
            }
        }*/
    }

    public void updateProjectileDest() {
        MobEntity projectileTarget = null;
        if (projectileTargetNpc == -1) {
            if (projectileTargetPlayer != -1) {
                projectileTarget = Game.world().getPlayer(projectileTargetPlayer);
            }
        } else {
            projectileTarget = Game.world().getNpc(projectileTargetNpc);
        }
        if (projectileTarget != null) {
            float targetX = projectileTarget.position.x;
            float targetZ = projectileTarget.position.z;
            float targetHeight = Game.world().getInterpolatedElevation(targetX, targetZ) + 2.5F;
            projectileDest.set(targetX, targetHeight, targetZ);
        }
    }

    public class Direction {
        public static final int NORTH = 0;
        public static final int NORTH_WEST = 1;
        public static final int WEST = 2;
        public static final int SOUTH_WEST = 3;
        public static final int SOUTH = 4;
        public static final int SOUTH_EAST = 5;
        public static final int EAST = 6;
        public static final int NORTH_EAST = 7;
        public static final int COMBAT_A = 8;
        public static final int COMBAT_B = 9;
    }
}
