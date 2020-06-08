package com.rscgl.ui.container;

import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.ItemDef;

public class Item {

    public Item() {}

    private int id;
    private int amount;
    private int slotIndex;

    public int getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public ItemDef getDef() {
        return id > RSCache.ITEM_COUNT || id < 0 ? null : RSCache.ITEMS[id];
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public Item set(int id, int amount) {
        this.id = id;
        this.amount = amount;
        return this;
    }

    public Item set(int id, int amount, int slotIndex) {
        this.id = id;
        this.amount = amount;
        this.slotIndex = slotIndex;
        return this;
    }

    public void setAmount(int a) {
        this.amount = a;
    }
}
