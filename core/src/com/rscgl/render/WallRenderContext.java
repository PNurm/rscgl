package com.rscgl.render;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.collision.Ray;
import com.rscgl.GameCamera;
import com.rscgl.model.RSMesh;
import com.rscgl.model.entity.ObjectEntity;
import com.rscgl.model.entity.WallEntity;
import com.rscgl.util.builder.RSModelBuilder;

public class WallRenderContext extends EntityRenderContext<ObjectEntity> {

    private final WallEntity wall;

    private RSMesh mesh;

    public WallRenderContext(WallEntity entity) {
        this.wall = entity;
        this.mesh = RSModelBuilder.createWall(wall.getType(), wall.getDirection(), wall.getTileX(), wall.getTileY());
    }


    @Override
    public void render(GameCamera camera, ModelBatch batch, DecalBatch decalBatch) {
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
        mesh.dispose();
    }
}
