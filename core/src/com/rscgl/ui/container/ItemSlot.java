package com.rscgl.ui.container;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.rscgl.assets.Assets;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.ItemDef;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Style;

public class ItemSlot<T extends Item> extends Stack {

    public static Drawable defaultBackground = Backgrounds.create( Color.CLEAR, Color.BLACK, 50, 35);

    private Texture texture;

    public Image getBackground() {
        return background;
    }

    private final Image background;
    private Label amountLabel;
    private Image itemSprite;

    private T item;
    private boolean forceAmountLabel;

    public ItemSlot() {
        this.amountLabel = new Label("", Style.labelBoldRegularW);
        this.amountLabel.setAlignment(Align.topLeft);

        this.itemSprite = new Image();
        this.itemSprite.setAlign(Align.center);
        this.itemSprite.setScaling(Scaling.fit);
        background = new Image();

        setBackground(defaultBackground);
        add(background);
        add(itemSprite);
        add(amountLabel);
    }

    public ItemSlot set(T item) {
        this.item = item;
        if(item == null) {
            itemSprite.setDrawable(null);
            amountLabel.setVisible(false);
            if(texture != null) {
                texture.dispose();
                texture = null;
            }
            return this;
        }
        if(item.getId() == -1) {
            itemSprite.setDrawable(null);
            amountLabel.setVisible(false);
            if(texture != null) {
                texture.dispose();
                texture = null;
            }
            return this;
        }
        if (RSCache.ITEM_COUNT < item.getId()) {
            return this;
        }
        ItemDef def = RSCache.ITEMS[item.getId()];
        if(def == null) {
            return this;
        }
        this.amountLabel.setVisible(def.isStackable() || forceAmountLabel);
        this.amountLabel.setText(" " + formatStackAmount(item.getAmount()));
        this.itemSprite.setDrawable(new TextureRegionDrawable(new TextureRegion(texture = Assets.inst.getItemSprite(def))));
        return this;
    }

    public static final String formatStackAmount(int length) {
        if (length < 100000) {
            return "[#FFFF00]" + String.valueOf(length);
        }
        if (length < 10000000) {
            return "[#FFFFFF]" + String.valueOf(length / 1000) + "K";
        }
        return "[#00FF00]" + String.valueOf(length / 1000000) + "M";
    }

    public Label getAmountLabel() {
        return amountLabel;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ItemSlot) {
            ItemSlot o = (ItemSlot) obj;
        }
        return super.equals(obj);
    }

    public void setBackground(Drawable o) {
        background.setDrawable(o);
    }

    public T getItem() {
        return item;
    }

    public void setForceAmountLabel(boolean forceAmountLabel) {
        this.forceAmountLabel = forceAmountLabel;
    }
}
