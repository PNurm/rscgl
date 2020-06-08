package com.rscgl.model.entity;


public class PlayerEntity extends MobEntity {

    public PlayerEntity() {
        super(EntityType.PLAYER);
    }

    public String username;
    public int skullType = 0;

}
