package com.rscgl.ui.shop;

import com.rscgl.ui.container.Item;

public class ShopItem extends Item {

    private int price;
    private int stock;
    private int inventoryCount;

    public ShopItem() {}

    public ShopItem(int id, int amount, int slotIndex, int stock, int price) {
        set(id, amount, slotIndex);
        this.price = price;
        this.stock = stock;
    }

    public void set(int id, int amount, int slotIndex, int stock, int price) {
        set(id, amount, slotIndex);
        this.price = price;
        this.stock = stock;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public int getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(int inventoryCount) {
        this.inventoryCount = inventoryCount;
    }
}
