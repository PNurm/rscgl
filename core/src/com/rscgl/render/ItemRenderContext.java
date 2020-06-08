package com.rscgl.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.rscgl.GameCamera;
import com.rscgl.Game;
import com.rscgl.assets.Assets;
import com.rscgl.model.entity.ItemEntity;
import com.rscgl.model.entity.ObjectEntity;

import static com.rscgl.GameWorld.TSIZE;

public class ItemRenderContext extends EntityRenderContext<ItemEntity> {

    private ItemEntity itemEntity;
    public final Decal decal;
    private final Texture texture;

    public ItemRenderContext(ItemEntity entity) {
        this.itemEntity = entity;

        texture = Assets.inst.getItemSprite(itemEntity.getDef());
        decal = Decal.newDecal(new TextureRegion(texture), true);
        decal.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }


    public void update(Camera camera) {
        float y = itemEntity.position.y + 0.5f;
        ObjectEntity obj = Game.world().getObject(itemEntity.getTileX(), itemEntity.getTileY());
        if(obj != null) {
            y += (obj.getDef().getItemHeight() / 128F) * TSIZE;
        }
        decal.setDimensions(1.5f, 1.5f);
        decal.setPosition(itemEntity.position.x - decal.getWidth(), y, itemEntity.position.z + decal.getWidth());
        decal.lookAt(camera.position, Vector3.Y);
    }

    @Override
    public void render(GameCamera camera, ModelBatch batch, DecalBatch decalBatch) {
        decalBatch.add(decal);
    }

    @Override
    public boolean rayHit(Ray ray) {

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


    @Override
    public void dispose() {

    }
}
