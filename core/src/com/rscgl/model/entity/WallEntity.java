package com.rscgl.model.entity;

import com.badlogic.gdx.math.collision.Ray;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.WallObjectDef;

public class WallEntity extends Entity {

    private int direction;
    private WallObjectDef wallObjectDef;

    public WallEntity() {
        super(EntityType.WALL);
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public WallObjectDef getDef() {
        if(wallObjectDef == null) {
            wallObjectDef = RSCache.WALLS[getType()];//Cache.getDoorDef(this.getType());
        }
        return wallObjectDef;
    }



    public int getDirection() {
        return direction;
    }

}
