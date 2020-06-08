package com.rscgl.ui.container;

public abstract class ItemChangeListener {

    public void itemChanged(int slot, Item itemSlot) {}

    public void itemAdded(Item itemSlot) {}

    public void itemRemoved(int slot) {}
}
