package com.rscgl.render;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import com.rscgl.GameCamera;
import com.rscgl.model.entity.Entity;

/**
 * The render implementation for entity
 * I'm not sure whether 'context' is right naming for this, however it makes sense.
 * @param <T>
 */
public abstract class EntityRenderContext<T extends Entity>  implements Disposable {

    private T entity;

    public abstract void render(GameCamera camera, ModelBatch batch, DecalBatch decalBatch);

    public abstract boolean rayHit(Ray ray);

    public void update(GameCamera camera) {

    }
}
