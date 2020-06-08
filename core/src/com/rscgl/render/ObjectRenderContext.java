package com.rscgl.render;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.collision.Ray;
import com.rscgl.GameCamera;
import com.rscgl.assets.Assets;
import com.rscgl.model.RSMesh;
import com.rscgl.model.entity.ObjectEntity;

public class ObjectRenderContext extends EntityRenderContext<ObjectEntity> {

    private RSMesh mesh;

    public ObjectRenderContext(ObjectEntity entity) {
        this.mesh = Assets.inst.loadModel(entity.getDef().modelID);
    }


    @Override
    public void render(GameCamera camera, ModelBatch batch, DecalBatch decalBatch) {
        if(mesh == null) {
            return;
        }
        batch.render(mesh);
    }

    @Override
    public boolean rayHit(Ray ray) {//Their bounding boxes are way too big, thats why they were disabled.
        if (mesh.rayHit(ray)) {
            return true;
        }
        return false;
    }


    @Override
    public void dispose() {
        if(mesh != null) {
            mesh.dispose();
            mesh = null;
        }
    }

    public RSMesh getMesh() {
        return mesh;
    }
}
