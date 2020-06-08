package com.rscgl.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.rscgl.GameCamera;
import com.rscgl.model.entity.MobEntity;
import com.rscgl.model.entity.NpcEntity;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.util.builder.CharacterTextureBuilder;

public class MobRenderContext extends EntityRenderContext<MobEntity> {

    private MobEntity mob;

    private final static CharacterTextureBuilder textureBuilder = new CharacterTextureBuilder(145, 220);
    private static final Texture healthBarRed = Backgrounds.createSolidBackgroundT(Color.RED, 100, 5);
    private static final Texture healthBarGreen = Backgrounds.createSolidBackgroundT(Color.GREEN, 100, 5);

    private CharacterTextureBuilder.BuilderSettings lastRenderSettings;
    private TextureRegion textureRegion;
    public Decal decal;

    public MobRenderContext(MobEntity mob) {
        this.mob = mob;
        this.createDecal();
    }

    private void createDecal() {
        textureRegion = new TextureRegion();
        generateAppearance();

        decal = Decal.newDecal(textureRegion, true);
        decal.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        decal.setDimensions(2.5f, 4.5f);

        if (mob instanceof NpcEntity) {
            NpcEntity npc = (NpcEntity) mob;
            decal.setDimensions(3f / 145 * npc.getDef().width, 4.5f / 220 * npc.getDef().height);
        }
    }

    public void generateAppearance() {
        if (lastRenderSettings == null)
            lastRenderSettings = new CharacterTextureBuilder.BuilderSettings(0, mob);

        if (textureRegion.getTexture() != null)
            textureRegion.getTexture().dispose();
        textureRegion.setRegion(textureBuilder.build(lastRenderSettings));
    }

    public void dispose() {
        textureRegion = null;
        decal = null;
    }

    public void updateSpriteAngle(Camera camera) {

        int cameraRotation = (int) ((-Math.atan2(camera.up.x, camera.up.z) * MathUtils.radiansToDegrees));
        int camRotation = (int) (cameraRotation * (255F / 360F)) & 255;
        int spriteDir = 7 & mob.getDirection() + (camRotation + 16) / 32;

        CharacterTextureBuilder.BuilderSettings settings = new CharacterTextureBuilder.BuilderSettings(spriteDir, mob);
        boolean buildTexture = lastRenderSettings == null || (settings.frameOffset != lastRenderSettings.frameOffset || settings.spriteDir != lastRenderSettings.spriteDir);
        if (buildTexture) {
            lastRenderSettings = settings;

            if (textureRegion.getTexture() != null)
                textureRegion.getTexture().dispose();

            textureRegion.setRegion(textureBuilder.build(settings));
        }
    }

    public void updateScreenPosition() {
        /*Vector3 screenCoords = Core.cam().project();
        screenCoords.y = Core.inst.getViewport().getScreenHeight() - screenCoords.y;
        Vector3 stageCoord = Core.ui().getCamera().unproject(screenCoords);

        screenPos = toScreen(screenPos.set(decal.getPosition()));
        screenPosHead.set(decal.getPosition());
        screenPosHead.y += 3.5f;
        screenPosHead = toScreen(screenPosHead);*/
    }

    public void renderOverlays(Batch spriteBatch) {
        /*if (mob.getDirection() == MobEntity.Direction.COMBAT_B) {
            screenPos.x += 24;
            screenPosHead.x += 24;
        }
        if (mob.getDirection() == MobEntity.Direction.COMBAT_A) {
            screenPos.x -= 24;
            screenPosHead.x -= 24;
        }
        if (timerMessage > 0) {
            Fonts.Font12B.draw(spriteBatch, "[#FFFF00]" + message, screenPosHead.x, screenPosHead.y, message.length(), Align.center, false);
        }
        if (timerCombat > 0) {
            float healthWidth = 30;
            float percent = healthCurrent * healthWidth / healthMax;

            spriteBatch.draw(healthBarGreen, screenPosHead.x - 15, screenPosHead.y, percent, 5);
            spriteBatch.draw(healthBarRed, screenPosHead.x + percent - 15, screenPosHead.y, healthWidth - percent, 5);
        }
        if (timerCombat > 150) {
            TextureRegion icon = Assets.inst.getSprite("interfaces", 9);
            if (mob instanceof NpcEntity) {
                icon = Assets.inst.getSprite("interfaces", 10);
            }
            spriteBatch.draw(icon, screenPos.x - 12, screenPos.y - 12, 24, 24);
            Fonts.Font14B.draw(spriteBatch, damageTaken + "", screenPos.x - 2, screenPos.y + 5, 3, Align.center, false);
        }*/
    }

    @Override
    public void update(GameCamera camera) {
        updateSpriteAngle(camera);
        decal.setScale(1, 1);
        if (mob.inCombat()) {
            decal.setScale(1.5f, 1);
        }
        decal.setPosition(mob.position().x - 1.5F, mob.position().y + decal.getHeight() / 2, mob.position().z + 1.5F);
        decal.lookAt(camera.position, Vector3.Y);
    }

    @Override
    public void render(GameCamera camera, ModelBatch batch, DecalBatch decalBatch) {
        decalBatch.add(decal);
    }

    public boolean rayHit(Ray ray) {
        if (!mob.isPicked()) {
            return false;
        }
        float[] verts = decal.getVertices();
        Vector3 v0 = new Vector3(verts[0], verts[1], verts[2]);
        Vector3 v1 = new Vector3(verts[6], verts[7], verts[8]);
        Vector3 v2 = new Vector3(verts[12], verts[13], verts[14]);
        Vector3 v3 = new Vector3(verts[18], verts[19], verts[20]);
        if (Intersector.intersectRayTriangle(ray, v1, v3, v2, null)) {
            return true;
        }
        if (Intersector.intersectRayTriangle(ray, v2, v0, v1, null)) {
            return true;
        }
        return false;
    }

}
