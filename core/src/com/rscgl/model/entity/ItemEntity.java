package com.rscgl.model.entity;

import com.badlogic.gdx.math.collision.Ray;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.ItemDef;

public class ItemEntity extends Entity {

    private final ItemDef def;
    private int objectHeight;

    public ItemEntity(int id) {
        super(EntityType.ITEM);
        this.setType(id);
        this.def = RSCache.ITEMS[id];
    }

    public ItemDef getDef() {
        return def;
    }

    public void setObjectHeight(int i) {
        this.objectHeight = i;
    }
}
