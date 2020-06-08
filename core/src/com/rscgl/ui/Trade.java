package com.rscgl.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.rscgl.Game;
import com.rscgl.assets.Assets;
import com.rscgl.net.Buffer;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Style;
import com.rscgl.model.entity.PlayerEntity;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.container.Item;
import com.rscgl.ui.container.ItemContainer;
import com.rscgl.ui.container.ItemContainerTable;
import com.rscgl.ui.container.ItemAction;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.util.Backgrounds;

import java.util.ArrayList;

public class Trade extends Table implements PacketHandler {

    private final Label.LabelStyle labelStyle;
    private final TradeWindow tradeWindow;
    private final TradeConfirmWindow tradeConfirmWindow;

    private final Cell<?> currentWindow;


    public Trade() {
        Game.inst.registerPacketHandler(this);
        background(Backgrounds.create(Colors.BG_WHITE2, 5, 5));

        labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.Font14B;
        labelStyle.fontColor = Color.WHITE;


        setVisible(false);
        tradeWindow = new TradeWindow();
        tradeConfirmWindow = new TradeConfirmWindow();
        currentWindow = add(tradeWindow);
        pack();
    }

    public void setTradeWindow(Table t) {
        currentWindow.setActor(t);
    }

    public TradeWindow getFirstWindow() {
        return tradeWindow;
    }

    public TradeConfirmWindow getConfirmWindow() {
        return tradeConfirmWindow;
    }

    public void show() {
        pack();
        setPosition((getStage().getWidth() - getWidth()) / 2, (getStage().getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 20) {
            setTradeWindow(getConfirmWindow());
            getConfirmWindow().reset();

            getConfirmWindow().setRecipientName(packet.readString());

            int tradeRecipientConfirmItemsCount = packet.readUnsignedByte();
            for (int var4 = 0; tradeRecipientConfirmItemsCount > var4; ++var4) {
                getConfirmWindow().addTheirOfferItem(new Item().set(packet
                        .readShort(), packet
                        .readInt()));
            }

            int tradeConfirmItemsCount = packet
                    .readUnsignedByte();
            for (int var4 = 0; var4 < tradeConfirmItemsCount; ++var4) {
                getConfirmWindow().addMyOfferItem(new Item().set(packet
                        .readShort(), packet
                        .readInt()));
            }
            show();
            return;
        }
        if (opcode == 15) {
            getFirstWindow().setAccepted(packet.readByte() == 1);
            return;
        }

        if (opcode == 162) {
            getFirstWindow().setRecipientAccepted(packet.readByte() == 1);
            return;
        }
        if (opcode == 92) {
            int serverIndex = packet.readShort();
            PlayerEntity recipient = Game.world().getPlayer(serverIndex);

            getFirstWindow().reset();
            getFirstWindow().setRecipientName(recipient.username);
            setTradeWindow(getFirstWindow());
            show();
            return;
        }
        if (opcode == 128) {
            //this.showDialogTradeConfirm = false;
            setVisible(false);
            return;
        }
        if (opcode == 97) {
            int tradeRecipientItemsCount = packet.readUnsignedByte();
            getFirstWindow().getRecipientTradeOffer().reset();
            for (int var4 = 0; var4 < tradeRecipientItemsCount; ++var4) {
                getFirstWindow().getRecipientTradeOffer().set(var4, packet.readShort(), packet.readInt());
            }
            getFirstWindow().setAccepted(false);
            getFirstWindow().setRecipientAccepted(false);
            return;
        }
    }

    @Override
    public int[] opcodes() {
        return new int[]{20, 15, 162, 92, 128, 97};
    }

    public class TradeConfirmWindow extends Table {
        private final Table myOffer;
        private final Table theirOffer;
        private final Cell<?> acceptedCell;
        private final ImageButton accept;

        public TradeConfirmWindow() {
            myOffer = new Table();
            theirOffer = new Table();

            ImageButton accept = new ImageButton(new TextureRegionDrawable(new TextureRegion(Assets.inst.getInterSprite(23))));
            accept.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    sendAccept();
                    return true;
                }

            });

            ImageButton decline = new ImageButton(new TextureRegionDrawable(new TextureRegion(Assets.inst.getInterSprite(24))));
            decline.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    sendDecline();
                    return true;
                }
            });

            Table left = new Table();
            left.pad(5);
            left.add(new Label("You are about to give:", Style.labelBoldRegularW));
            left.row();
            left.add(myOffer).size(200, 150);
            add(left);
            add().space(15);
            Table right = new Table();
            right.pad(5);
            right.add(new Label("In return you will receive:", Style.labelBoldRegularW));
            right.row();
            right.add(theirOffer).size(250, 150);
            right.row();
            add(right);

            row();

            add(new Label("[#" + Color.CYAN + "]Are you sure you want to do this?", labelStyle)).colspan(3);
            row();
            add(new Label("There is NO WAY to reverse a trade if you change your mind. \n Remember that not all players are trustworthy", labelStyle)).colspan(3);
            row();
            acceptedCell = add(this.accept = accept).size(70, 35);
            add().space(25);
            add(decline).size(70, 35);
            pack();
        }

        public void setAccepted(boolean accepted) {
            if (accepted) {
                Label label = new Label("Waiting for\nother player", Style.labelBoldRegularW);
                acceptedCell.setActor(label);
            } else {
                acceptedCell.setActor(accept);
            }
        }

        public void reset() {
            myOffer.clear();
            theirOffer.clear();
            setAccepted(false);
        }

        public void addMyOfferItem(Item item) {
            myOffer.add(new Label(item.getDef().getName() + "x" + formatItemCount(item.getAmount()), Style.labelBoldRegularW));
            myOffer.row();
        }

        public void addTheirOfferItem(Item item) {
            theirOffer.add(new Label(item.getDef().getName() + "x" + formatItemCount(item.getAmount()), Style.labelBoldRegularW));
            theirOffer.row();
        }

        private void sendAccept() {
            Game.outBuffer().newPacket(104);
            Game.outBuffer().finishPacket();
        }

        private void sendDecline() {
            Game.outBuffer().newPacket(230);
            Game.outBuffer().finishPacket();
            Trade.this.setVisible(false);
        }

        public void setRecipientName(String string) {

        }
    }

    public class TradeWindow extends Table {

        private final Label otherAcceptedLabel;
        private final Cell<?> acceptedCell;

        private ItemContainerTable inventoryItems;
        private ImageButton acceptButton;

        public ItemContainer getOurTradeOffer() {
            return ourTradeOffer;
        }

        public ItemContainer getRecipientTradeOffer() {
            return recipientTradeOffer;
        }

        private ItemContainer ourTradeOffer;
        private ItemContainer recipientTradeOffer;

        private final ItemContainerTable ourTradeOfferTable;
        private final ItemContainerTable recipientTradeOfferTable;


        public TradeWindow() {
            inventoryItems = new ItemContainerTable(5, false, Game.ui().getInventory().getContainer());
            inventoryItems.setItemOptions(new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Offer 1";
                }

                @Override
                public void action(Item itemSlot) {
                    addTradeItem(itemSlot, 1);
                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Offer 5";
                }

                @Override
                public void action(Item itemSlot) {
                    addTradeItem(itemSlot, 5);

                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Offer 10";
                }

                @Override
                public void action(Item itemSlot) {
                    addTradeItem(itemSlot, 10);

                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Offer All";
                }

                @Override
                public void action(Item itemSlot) {
                    addTradeItem(itemSlot, itemSlot.getAmount());
                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Offer X";
                }

                @Override
                public void action(Item itemSlot) {

                }
            });

            ourTradeOffer = new ItemContainer(12);
            recipientTradeOffer = new ItemContainer(12);

            ourTradeOfferTable = new ItemContainerTable(4, false, ourTradeOffer);
            ourTradeOfferTable.setItemOptions(new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Remove 1";
                }

                @Override
                public void action(Item itemSlot) {
                    removeTradeItem(itemSlot, 1);
                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Remove 5";
                }

                @Override
                public void action(Item itemSlot) {
                    removeTradeItem(itemSlot, 5);

                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Remove 10";
                }

                @Override
                public void action(Item itemSlot) {
                    removeTradeItem(itemSlot, 10);

                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Remove All";
                }

                @Override
                public void action(Item itemSlot) {
                    removeTradeItem(itemSlot, itemSlot.getAmount());
                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Remove X";
                }

                @Override
                public void action(Item itemSlot) {

                }
            });
            recipientTradeOfferTable = new ItemContainerTable(4, false, recipientTradeOffer);

            VerticalGroup group = new VerticalGroup();
            group.pad(0, 5, 5, 5);
            group.addActor(new Label("Your offer", labelStyle));
            group.addActor(ourTradeOfferTable);
            group.addActor(new Label("Opponents offer", labelStyle));
            group.addActor(recipientTradeOfferTable);


            System.out.println(Assets.inst.getInterSprite(23));
            ImageButton accept = new ImageButton(new TextureRegionDrawable(new TextureRegion(Assets.inst.getInterSprite(23))));
            accept.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    sendAccept();
                    return true;
                }
            });

            ImageButton decline = new ImageButton(new TextureRegionDrawable(new TextureRegion(Assets.inst.getInterSprite(24))));
            decline.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    sendDecline();
                    return true;
                }
            });


            Table rightSide = new Table();
            rightSide.add(new Label("Inventory", labelStyle)).colspan(3);
            rightSide.row();
            rightSide.add(inventoryItems).colspan(3).right();
            rightSide.row();
            rightSide.pad(5);
            acceptedCell = rightSide.add(acceptButton = accept).size(70, 35);
            rightSide.add(otherAcceptedLabel = new Label("", Style.labelBoldRegularW)).size(70, 35);
            rightSide.add(decline).size(70, 35);
            rightSide.pack();

            add(group);
            add(rightSide);
            pack();
        }


        private void sendAccept() {
            Game.outBuffer().newPacket(55);
            Game.outBuffer().finishPacket();
            setAccepted(true);
        }

        private void sendDecline() {
            Game.outBuffer().newPacket(230);
            Game.outBuffer().finishPacket();
            Trade.this.setVisible(false);
        }

        public void setAccepted(boolean accepted) {
            if (accepted) {
                Label label = new Label("Waiting for\nother player", Style.labelBoldRegularW);
                acceptedCell.setActor(label);
            } else {
                acceptedCell.setActor(acceptButton);
            }
        }

        public void setRecipientAccepted(boolean accepted) {
            if (accepted) {
                otherAcceptedLabel.setText("Other player\nhas accepted");
            } else {
                otherAcceptedLabel.setText("");
            }
            otherAcceptedLabel.setSize(50, 30);
        }

        private void removeTradeItem(Item item, int amount) {
            if (amount > ourTradeOffer.countItem(item.getId()))
                amount = ourTradeOffer.countItem(item.getId());

            Item firstItem = ourTradeOffer.getFirstItem(item.getId());
            if (item.getDef().isStackable()) {

                firstItem.setAmount(firstItem.getAmount() - amount);
                ourTradeOffer.set(firstItem.getSlotIndex(), firstItem.getId(), firstItem.getAmount());

                if (firstItem.getAmount() <= 0) {
                    ourTradeOffer.set(firstItem.getSlotIndex(), -1, -1);
                    for (int slot = firstItem.getSlotIndex(); slot < ourTradeOffer.size() - 1; ++slot) {
                        Item nextSlot = ourTradeOffer.get(slot + 1);
                        ourTradeOffer.set(slot, nextSlot.getId(), nextSlot.getAmount());
                    }
                }
            } else {
                for (int removed = 0; removed < amount; removed++) {
                    firstItem = ourTradeOffer.getFirstItem(item.getId());

                    ourTradeOffer.set(firstItem.getSlotIndex(), -1, -1);
                    for (int slot = firstItem.getSlotIndex(); slot < ourTradeOffer.size() - 1; ++slot) {
                        Item nextSlot = ourTradeOffer.get(slot + 1);
                        ourTradeOffer.set(slot, nextSlot.getId(), nextSlot.getAmount());
                    }
                }
            }
            setAccepted(false);
            setRecipientAccepted(false);
            sendTradeItems();
        }

        private void addTradeItem(Item item, int amount) {
            if (amount == 0) {
                return;
            }
            int invCount = inventoryItems.getContainer().countItem(item.getId());
            if (item.getDef().isStackable()) {
                if (ourTradeOffer.containsItem(item)) {
                    Item existingItem = ourTradeOffer.getFirstItem(item.getId());
                    existingItem.setAmount(amount + existingItem.getAmount());
                    if (existingItem.getAmount() >= invCount) {
                        existingItem.setAmount(invCount);
                    }
                    ourTradeOffer.set(existingItem.getSlotIndex(), existingItem.getId(), existingItem.getAmount());
                } else {
                    ourTradeOffer.set(ourTradeOffer.count(), item.getId(), amount);
                }
            } else {
                for (int offered = 0; offered < amount && ourTradeOffer.count() < ourTradeOffer.size() && invCount > ourTradeOffer.countItem(item.getId()); ++offered) {
                    ourTradeOffer.set(ourTradeOffer.count(), item.getId(), 1);
                }
            }
            setAccepted(false);
            setRecipientAccepted(false);
            sendTradeItems();
        }

        private void sendTradeItems() {
            Game.outBuffer().newPacket(46);
            Game.outBuffer().putByte(ourTradeOffer.size());
            for (Item i : (ArrayList<Item>) ourTradeOffer.getItems()) {
                Game.outBuffer().putShort(i.getId());
                Game.outBuffer().putInt(i.getAmount());
            }
            Game.outBuffer().finishPacket();
        }

        public void setRecipientName(String recipient) {

        }

        public void reset() {
            getOurTradeOffer().reset();
            getRecipientTradeOffer().reset();
            setAccepted(false);
            setRecipientAccepted(false);
        }
    }

    public static final String formatItemCount(int count) {
        String str = "" + count;

        for (int i = str.length() - 3; i > 0; i -= 3) {
            str = str.substring(0, i) + "," + str.substring(i);
        }

        if (str.length() > 8) {
            str = "@gre@" + str.substring(0, str.length() - 8) + " million @whi@(" + str + ")";
        } else if (str.length() > 4) {
            str = "@cya@" + str.substring(0, str.length() - 4) + "K @whi@(" + str + ")";
        }

        return str;
    }
}
