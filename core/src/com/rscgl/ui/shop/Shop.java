package com.rscgl.ui.shop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.assets.RSCache;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.container.*;
import com.rscgl.ui.dialog.DialogInputAction;
import com.rscgl.ui.dialog.DialogPopup;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.util.Style;

public class Shop extends Table implements PacketHandler {


    private final Label shopBuyItemInfo;
    private final TextButton.TextButtonStyle buttonStyle;
    private final Label shopSellItemInfo;
    private final Table sellButtons;
    private final Table buyButtons;
    private final Label currencyLabel;

    private ItemContainerTable shopItemTable;
    private ItemContainer shop;

    private int lastX = 1;
    private int shopCurrency;
    private boolean shopCanSell;
    private int shopSellPriceMod;
    private int shopBuyPriceMod;
    private int shopPriceMultiplier;
    private ShopItemSlot selectedSlot;

    public Shop() {
        background(Backgrounds.create(Colors.BG_WHITE2, 5, 5));

        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.overFontColor = Color.RED;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.font = Fonts.Font14B;
        buttonStyle.font.getData().markupEnabled = true;

        Game.ui().getInventory().getContainer().addListener(new ItemChangeListener() {
            @Override
            public void itemChanged(int slot, Item item) {
                for (int i = 0; i < shop.size(); i++) {
                    ShopItem shopItem = (ShopItem) shop.get(i);
                    if(shopItem.getId() == item.getId()) {
                        shopItem.setInventoryCount(item.getAmount());
                        shop.update(i);
                        break;
                    }
                    if(item.getId() == 10) {
                        updateMoneyLabel(item.getAmount());
                    }
                }
            }
        });
        shop = new ItemContainer(40, ShopItem.class);
        shopItemTable = new ItemContainerTable(8, true, shop, ShopItemSlot.class);
        shopItemTable.setItemOptions(new ItemAction() {
            @Override
            public String getOption(Item itemSlot) {
                return "Select";
            }

            @Override
            public void action(Item itemSlot) {
                if(selectedSlot != null) {
                    selectedSlot.setBackground(ItemSlot.defaultBackground);
                }
                ShopItemSlot slot = (ShopItemSlot) shopItemTable.getSlot(itemSlot.getSlotIndex());
                if(slot.getItem().getId() != -1) {
                    ShopItem item = (ShopItem) itemSlot;
                    if(item.getInventoryCount() > 0) {
                        sellButtons.setVisible(true);
                        shopSellItemInfo.setText(item.getDef().getName() + ": sell for " + getSellPrice(item) + "gp each");
                    } else {
                        sellButtons.setVisible(false);
                        shopSellItemInfo.setText("You do not have any of this item to sell");
                    }
                    if(item.getAmount() <= 0) {
                        shopBuyItemInfo.setText("This item is not currently available to buy");
                        buyButtons.setVisible(false);
                    } else {
                        buyButtons.setVisible(true);
                        if(shopCurrency != 10) {
                            shopBuyItemInfo.setText(item.getDef().getName() + " buy for " + item.getPrice() + " " + RSCache.ITEMS[shopCurrency].getName() + " each");
                        } else {
                            shopBuyItemInfo.setText(item.getDef().getName() + " buy for " + item.getPrice() + " gp each");
                        }
                    }

                    selectedSlot = slot;
                    slot.setBackground(Backgrounds.create(Color.RED, Color.BLACK, 50, 35));
                } else {
                    resetSelection();
                }
            }
        });

        TextButton close = new TextButton("Close", buttonStyle);
        close.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                setVisible(false);
                Game.outBuffer().newPacket(212);
                Game.outBuffer().finishPacket();

                return super.touchDown(event, x, y, pointer, button);
            }
        });
        add(new Label("Shop:", Style.labelBoldRegularW)).left();
        add(currencyLabel = new Label("Your money:", Style.labelBoldRegularW)).pad(5).right();
        add(close).right().pad(5);
        row();
        add(shopItemTable).colspan(3).height(175).pad(5);
        row();


        add(shopBuyItemInfo = new Label("Select an object to buy or sell", Style.labelBoldRegularW)).padLeft(5).width(200).left();
        shopBuyItemInfo.setColor(Color.YELLOW);
        buyButtons = new Table();
        buyButtons.align(Align.right);
        buyButtons.add(new Label("Buy:", Style.labelBoldRegularW)).right();
        buyButtons.add(makeShopButton(1, true)).padLeft(10).right();
        buyButtons.add(makeShopButton(5, true)).padLeft(10).right();
        buyButtons.add(makeShopButton(10, true)).padLeft(10).right();
        buyButtons.add(makeShopButton(50, true)).padLeft(10).right();
        buyButtons.add(makeXButton(true)).padLeft(10).right();
        add(buyButtons).right().pad(5).colspan(2);
        row();

        add(shopSellItemInfo = new Label("", Style.labelBoldRegularW)).padLeft(5).width(200).left();
        shopSellItemInfo.setColor(Color.YELLOW);
        sellButtons = new Table();
        sellButtons.add(new Label("Sell:", Style.labelBoldRegularW));
        sellButtons.add(makeShopButton(1, false)).padLeft(10);
        sellButtons.add(makeShopButton(5, false)).padLeft(10);
        sellButtons.add(makeShopButton(10, false)).padLeft(10);
        sellButtons.add(makeShopButton(50, false)).padLeft(10);
        sellButtons.add(makeXButton(false)).padLeft(10);
        add(sellButtons).right().pad(5).colspan(2);

        pack();
        setVisible(false);
        Game.inst.registerPacketHandler(this);
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
    }

    private void updateMoneyLabel(int amount) {
        currencyLabel.setText("Your money: " + ItemSlot.formatStackAmount(amount) + " gp");
    }

    public void resetSelection() {
        if(selectedSlot != null) {
            selectedSlot.setBackground(ItemSlot.defaultBackground);
        }
        shopSellItemInfo.setText("Select an object to buy or sell");
        shopBuyItemInfo.setText("");
        buyButtons.setVisible(false);
        sellButtons.setVisible(false);
        selectedSlot = null;
    }

    public static final int computeItemCost(int basePrice, int shopItemPrice, int shopBuyPriceMod, int var3,
                                            boolean var4, int var5, int count, int shopPriceMultiplier) {
            int cost = 0;

            for (int k = 0; var5 > k; ++k) {
                int var10 = shopPriceMultiplier * (shopItemPrice + ((!var4 ? -k : k) - count));
                if (var10 >= -100) {
                    if (var10 > 100) {
                        var10 = 100;
                    }
                } else {
                    var10 = -100;
                }

                int scaling = shopBuyPriceMod + var10;
                if (scaling < 10) {
                    scaling = 10;
                }

                cost += basePrice * scaling / 100;
            }

            return cost;
    }

    private int getSellPrice(ShopItem item) {
        int sellCost = computeItemCost(item.getDef().getBasePrice(),
                item.getStock(), this.shopSellPriceMod, -30910, false, 1,
                item.getAmount(), this.shopPriceMultiplier);
        /*
        if (Cache.getItemDef(id).getNotedFormOf() > 0) {

            int originalItemID = Cache.getItemDef(id).getNotedFormOf();
            int originalItemPrice = 0;
            int originalItemCount = 0;
            for (int i = 0; i < shopItemID.length; i++) {
                if (shopItemStock[i] == originalItemID) {
                    originalItemPrice = shopItemStock[i];
                    break;
                }
            }

            for (int i = 0; i < shopItemID.length; i++) {
                if (shopItemID[i] == originalItemID) {
                    originalItemCount = shopItemCount[i];
                    break;
                }
            }
            sellCost = GenUtil.computeItemCost(Cache.getItemDef(originalItemID).getBasePrice(),
                    originalItemPrice, this.shopSellPriceMod, -30910, false, 1, originalItemCount,
                    this.shopPriceMultiplier);
        }*/

        return sellCost;
    }

    private TextButton makeXButton(boolean buy) {
        TextButton textButton = new TextButton("X", buttonStyle);
        textButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(selectedSlot != null) {
                    new DialogPopup("Enter amount to " + (buy ? "buy" : "sell"), (buy ? "buy" : "sell"), new DialogInputAction() {
                        @Override
                        public void action(String result) {
                            int amount = 0;
                            try {
                                amount = Integer.parseInt(result);
                            }catch (Exception e) {}
                            sendShopAction(amount, buy);
                            lastX = amount;
                        }
                    });
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        return textButton;
    }

    private TextButton makeShopButton(int amount, boolean buy) {
        TextButton textButton = new TextButton("" + amount, buttonStyle);
        textButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(selectedSlot != null) {
                    sendShopAction(amount, buy);
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        return textButton;
    }

    public void sendShopAction(int amount, boolean buy) {
        if(buy) {
            Game.outBuffer().newPacket(236);
            Game.outBuffer().putShort(selectedSlot.getItem().getId());
            Game.outBuffer().putShort(selectedSlot.getItem().getAmount());
            Game.outBuffer().putShort(amount);
            Game.outBuffer().finishPacket();

        } else {
            Game.outBuffer().newPacket(221);
            Game.outBuffer().putShort(selectedSlot.getItem().getId());
            Game.outBuffer().putShort(selectedSlot.getItem().getAmount());
            Game.outBuffer().putShort(amount);
            Game.outBuffer().finishPacket();
        }
    }

    public ItemContainer getContainer() {
        return shop;
    }

    public void show() {
        align(Align.center);
        pack();

        setPosition((getStage().getWidth() - getWidth()) / 2, (getStage().getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 101) {
            int shopItemCount = packet.readUnsignedByte();
            int shopType = packet.readByte();
            shopCurrency = packet.readShort();
            shopCanSell = packet.readByte() == 1;

            this.shopSellPriceMod = packet.readShort();
            this.shopBuyPriceMod = packet.readShort();
            this.shopPriceMultiplier = packet.readShort();

            ItemContainer inv = Game.ui().getInventory().getContainer();
            for(int i = 0; i < shop.size();i++) {
                ShopItem item = (ShopItem) shop.get(i);
                item.set(-1, -1);
                item.setInventoryCount(-1);
                shopItemTable.getSlot(i).set(item);
            }

            for (int i = 0; i < shopItemCount; i++) {
                int id = packet.readShort();
                int amount = packet.readShort();
                int stock = packet.readShort();
                int price = packet.readInt();
                ShopItem item = (ShopItem) shop.get(i);
                item.setInventoryCount(inv.countItem(id));
                item.set(id, amount, i, stock, price);
                shop.update(i);
            }

            if (shopType == 1) {
                int shopItemIndex = 39;
                for (int inventoryIndex = 0; inventoryIndex < 30
                        && shopItemCount <= shopItemIndex; ++inventoryIndex) {
                    Item invItem = inv.get(inventoryIndex);
                    if (invItem.getId() == -1) {
                        break;
                    }
                    boolean existsInShop = false;

                    for (int shopIndex = 0; shopIndex < 40; ++shopIndex) {
                        if (invItem.getId() == shop.get(shopIndex).getId()) {
                            existsInShop = true;
                            break;
                        }
                    }
                    if (invItem.getId() == 10) {
                        existsInShop = true;
                        updateMoneyLabel(invItem.getAmount());
                    }
                    if (!existsInShop) {
                        ShopItem item = (ShopItem) shop.get(shopItemIndex);
                        item.setInventoryCount(invItem.getAmount());
                        shop.set(shopItemIndex, invItem.getId(), 0);

                        ShopItemSlot slot = (ShopItemSlot) shopItemTable.getSlot(shopItemIndex);
                        slot.update();
                        --shopItemIndex;
                    }
                }
            }

            if (selectedSlot != null) {
                ShopItem item = selectedSlot.getItem();
                if(item.getId() != -1) {
                    if (item.getInventoryCount() > 0) {
                        sellButtons.setVisible(true);
                        shopSellItemInfo.setText(item.getDef().getName() + ": sell for " + getSellPrice(item) + "gp each");
                    } else {
                        sellButtons.setVisible(false);
                        shopSellItemInfo.setText("You do not have any of this item to sell");
                    }
                    if (item.getAmount() <= 0) {
                        shopBuyItemInfo.setText("This item is not currently available to buy");
                        buyButtons.setVisible(false);
                    } else {
                        sellButtons.setVisible(true);
                    }
                } else
                    resetSelection();
            }

            show();
            return;
        }

    }

    @Override
    public int[] opcodes() {
        return new int[]{101};
    }
}
