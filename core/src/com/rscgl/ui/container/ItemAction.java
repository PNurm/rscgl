package com.rscgl.ui.container;

public abstract class ItemAction {

    public abstract String getOption(Item itemSlot);
    public boolean visibleFor(Item itemSlot) { return true; }
    public abstract void action(Item itemSlot);

}
