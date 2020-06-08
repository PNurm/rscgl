package com.rscgl.assets.def;


public abstract class EntityDef extends GameDefinition {
	public String name;
	public String description;
	public int id;
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
}
