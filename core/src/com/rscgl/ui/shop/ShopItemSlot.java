package com.rscgl.ui.shop;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.rscgl.ui.container.Item;
import com.rscgl.ui.container.ItemSlot;
import com.rscgl.ui.util.Style;

public class ShopItemSlot extends ItemSlot {

    private final Label inventoryAmount;

    public ShopItemSlot() {
        super();
        this.inventoryAmount = new Label("", Style.labelBoldRegularW);
        this.inventoryAmount.setAlignment(Align.topRight);

        add(inventoryAmount);
    }

    public void setInventoryAmount(int amount) {
        if(amount > 0) {
            this.inventoryAmount.setText("[#00FFFF]" + amount + " ");
        } else
            this.inventoryAmount.setText("");
    }

    @Override
    public ItemSlot set(Item item) {
        if(item instanceof ShopItem) {
            ShopItem i = (ShopItem) item;
            setInventoryAmount(i.getInventoryCount());
        }
        return super.set(item);
    }

    public void update() {
        setInventoryAmount(getItem().getInventoryCount());
        set(getItem());
    }

    public ShopItem getItem() {
        return (ShopItem) super.getItem();
    }
}
