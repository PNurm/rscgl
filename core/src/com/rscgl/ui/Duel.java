package com.rscgl.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.rscgl.Game;
import com.rscgl.assets.Assets;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.container.*;
import com.rscgl.ui.menu.GameAction;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.menu.option.OptionPriority;
import com.rscgl.ui.menu.ActionMenu;
import com.rscgl.ui.onscreen.Inventory;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Style;

import java.util.ArrayList;

public class Duel extends Table implements PacketHandler {

    private final Label.LabelStyle labelStyle;

    private final DuelWindow duelWindow;
    private final DuelConfirmWindow duelConfirmWindow;
    private final Cell<?> currentWindow;

    public Duel() {
        Game.inst.registerPacketHandler(this);
        background(Backgrounds.create(Colors.BG_WHITE2, 5, 5));

        labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.Font14B;
        labelStyle.fontColor = Color.WHITE;

        //setVisible(false);
        duelWindow = new DuelWindow(Game.ui().getInventory());
        duelConfirmWindow = new DuelConfirmWindow();
        currentWindow = add(duelWindow);
        pack();
        setVisible(false);
    }

    public void setDuelWindow(Table t) {
        currentWindow.setActor(t);
    }

    public DuelWindow getFirstWindow() {
        return duelWindow;
    }

    public DuelConfirmWindow getConfirmWindow() {
        return duelConfirmWindow;
    }

    public void show() {
        pack();
        setPosition((getStage().getWidth() - getWidth()) / 2, (getStage().getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 172) {
            Duel.DuelConfirmWindow duel = getConfirmWindow();
            duel.reset();
            duel.setRecipientName(packet.readString());
            setDuelWindow(duel);
            int duelConfirmOpponentItemCount = packet.readUnsignedByte();
            for (int var4 = 0; var4 < duelConfirmOpponentItemCount; ++var4) {
                duel.addTheirOfferItem(new Item().set(packet.readShort(), packet.readInt()));
            }
            int duelConfirmItemCount = packet
                    .readUnsignedByte();
            for (int var4 = 0; duelConfirmItemCount > var4; ++var4) {
                duel.addMyOfferItem(new Item().set(packet.readShort(), packet.readInt()));
            }
            for (int i = 0; i < 6; i++) {
                duel.setRule(i, packet.readByte() == 1);
            }
            for (int i = 0; i < 11; i++) {
                duel.setEquipmentRule(i, packet.readByte() == 1);
            }
            show();
            return;
        }
        if (opcode == 176) {
            String opponent = packet.readString();
            getFirstWindow().reset();
            getFirstWindow().setRecipientName(opponent, packet.readByte());
            setDuelWindow(getFirstWindow());
            show();
            return;
        }
        if (opcode == 225) {
            setVisible(false);
            return;
        }
        if (opcode == 253) {
            getFirstWindow().setRecipientAccepted(packet.readByte() == 1);
            return;
        }
        if (opcode == 210) {
            getFirstWindow().setAccepted(packet.readByte() == 1);
            return;
        }
        if (opcode == 6) {
            int duelRecipientItemsCount = packet.readUnsignedByte();
            getFirstWindow().getRecipientDuelOffer().reset();
            for (int var4 = 0; var4 < duelRecipientItemsCount; ++var4) {
                getFirstWindow().getRecipientDuelOffer().set(var4, packet.readShort(), packet.readInt());
            }
            getFirstWindow().setAccepted(false);
            getFirstWindow().setRecipientAccepted(false);
            return;
        }
        if (opcode == 30) {
            for (int i = 0; i < 6; i++) {
                getFirstWindow().setDuelRule(i, packet.readByte() == 1);
            }
            for (int i = 0; i < 11; i++) {
                getFirstWindow().setEquipmentRule(i, packet.readByte() == 1);
            }
            getFirstWindow().setAccepted(false);
            getFirstWindow().setRecipientAccepted(false);
            return;
        }
    }

    @Override
    public int[] opcodes() {
        return new int[]{172, 176, 225, 253, 210, 6, 30};
    }

    public class DuelConfirmWindow extends Table {
        private final Table myOffer;
        private final Table theirOffer;
        private final Cell<?> acceptedCell;
        private final ImageButton accept;

        private final Label[] duelRules = new Label[6];
        private final Label[] equipmentRules = new Label[11];

        private String[] equipmentSlotNames = new String[]{"Helmet", "Cape", "Amulet", "Weapon", "Body", "Shield", "Legs", "Gloves", "Boots", "Ring", "Arrows"};

        private String[] ruleEnabled = {
                "No retreat is possible!",
                "Magic cannot be used",
                "Prayer cannot be used",
                "Drinks cannot be used",
                "Members items will be unequipped and disabled",
                "RSCR items will be unequipped and disabled",
        };
        private String[] ruleDisabled = {
                "You can retreat from this duel",
                "Magic may be used",
                "Prayer may be used",
                "Drinks may be used",
                "Members items may be used",
                "RSCR items may be used",
        };

        public DuelConfirmWindow() {
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
            left.add(new Label("Your stakes:", Style.labelBoldRegularW));
            left.row();
            left.add(myOffer).size(145, 150);
            add(left);

            Table rules = new Table();
            for (int i = 0; i < duelRules.length; i++) {
                rules.add(duelRules[i] = new Label("", Style.labelBoldRegularW));
                rules.row();
            }
            for (int i = 0; i < equipmentRules.length; i++) {
                rules.add(equipmentRules[i] = new Label("", Style.labelBoldRegularW));
                rules.row();
            }
            add(rules);

            Table right = new Table();
            right.pad(5);
            right.add(new Label("Opponents stake:", Style.labelBoldRegularW));
            right.row();
            right.add(theirOffer).size(145, 150);
            right.row();
            add(right);

            row();

            add(new Label("[#" + Color.CYAN + "]Are you sure you want to do this?", labelStyle)).colspan(3);
            row();
            add(new Label("There is NO WAY to reverse a duel if you change your mind. \n Remember that not all players are trustworthy", labelStyle)).colspan(3);
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

        public void setRule(int i, boolean b) {
            duelRules[i].setText(b ? "[#00FF00]" + ruleDisabled[i] : "[#FF0000]" + ruleEnabled[i]);
        }

        public void setEquipmentRule(int i, boolean b) {
            equipmentRules[i].setColor(b ? Color.RED : Color.WHITE);
            equipmentRules[i].setText(equipmentSlotNames[i] + (b ? " cannot be used" : " may be used"));
        }

        private void sendAccept() {
            Game.outBuffer().newPacket(8);
            Game.outBuffer().putByte(5);
            Game.outBuffer().finishPacket();
        }

        private void sendDecline() {
            Game.outBuffer().newPacket(8);
            Game.outBuffer().putByte(2);
            Game.outBuffer().finishPacket();
            Duel.this.setVisible(false);
        }

        public void setRecipientName(String string) {

        }
    }

    public class DuelWindow extends Table {

        private final Label otherAcceptedLabel;
        private final Cell<?> acceptedCell;
        private final Table equipmentTable;
        private final ItemSlot[] equipment = new ItemSlot[11];
        private final Image[] duelRuleLabels = new Image[6];
        private final String[] duelRuleStr = {
                "No retreating",
                "No magic",
                "No prayer",
                "No drinks",
                "No members items",
                "No RSCR items"
        };

        private String[] equipmentSlotNames = new String[]{"Helmet", "Cape", "Amulet", "Weapon", "Body", "Shield", "Legs", "Gloves", "Boots", "Ring", "Arrows"};
        private final Label hpOffset;
        private final Label opponentName;

        private ItemContainerTable inventoryItems;
        private ImageButton acceptButton;

        public ItemContainer getOurDuelOffer() {
            return ourDuelOffer;
        }

        public ItemContainer getRecipientDuelOffer() {
            return recipientDuelOffer;
        }

        private ItemContainer ourDuelOffer;
        private ItemContainer recipientDuelOffer;

        private final ItemContainerTable ourDuelOfferTable;
        private final ItemContainerTable recipientDuelOfferTable;

        public boolean[] equipmentRules = new boolean[11];
        public boolean[] duelRules = new boolean[6];

        public DuelWindow(Inventory inventory) {

            inventoryItems = new ItemContainerTable(5, false, inventory.getContainer());
            inventoryItems.setItemOptions(new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Offer 1";
                }

                @Override
                public void action(Item itemSlot) {
                    addDuelItem(itemSlot, 1);
                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Offer 5";
                }

                @Override
                public void action(Item itemSlot) {
                    addDuelItem(itemSlot, 5);

                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Offer 10";
                }

                @Override
                public void action(Item itemSlot) {
                    addDuelItem(itemSlot, 10);

                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Offer All";
                }

                @Override
                public void action(Item itemSlot) {
                    addDuelItem(itemSlot, itemSlot.getAmount());
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

            ourDuelOffer = new ItemContainer(8);
            recipientDuelOffer = new ItemContainer(8);

            ourDuelOfferTable = new ItemContainerTable(4, false, ourDuelOffer);
            ourDuelOfferTable.setItemOptions(new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Remove 1";
                }

                @Override
                public void action(Item itemSlot) {
                    removeDuelItem(itemSlot, 1);
                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Remove 5";
                }

                @Override
                public void action(Item itemSlot) {
                    removeDuelItem(itemSlot, 5);

                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Remove 10";
                }

                @Override
                public void action(Item itemSlot) {
                    removeDuelItem(itemSlot, 10);

                }
            }, new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Remove All";
                }
                @Override
                public void action(Item itemSlot) {
                    removeDuelItem(itemSlot, itemSlot.getAmount());
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
            recipientDuelOfferTable = new ItemContainerTable(4, false, recipientDuelOffer);

            acceptButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(Assets.inst.getInterSprite(23))));
            acceptButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    Game.outBuffer().newPacket(8);
                    Game.outBuffer().putByte(1);
                    Game.outBuffer().finishPacket();
                    setAccepted(true);
                }
            });

            ImageButton decline = new ImageButton(new TextureRegionDrawable(new TextureRegion(Assets.inst.getInterSprite(24))));
            decline.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    Game.outBuffer().newPacket(8);
                    Game.outBuffer().putByte(2);
                    Game.outBuffer().finishPacket();
                    Duel.this.setVisible(false);
                    return true;
                }
            });

            /**
             * Table on left side
             */
            Table duelOffers = new Table();

            {
                duelOffers.add(new Label("Your stake:", labelStyle)).space(10);
                duelOffers.row();
                duelOffers.add(ourDuelOfferTable);
                duelOffers.row();

                duelOffers.add(new Label("Opponents stake:", labelStyle));
                duelOffers.row();
                duelOffers.add(recipientDuelOfferTable);
                duelOffers.row();

                duelOffers.add(opponentName = new Label("Opponent:", Style.labelBoldRegularW)).space(0);
                duelOffers.row();

                duelOffers.add(hpOffset = new Label("Hitpoint offset:", Style.labelBoldRegularW));
                duelOffers.row();

                duelOffers.pack();
            }

            /**
             * Middle table
             */
            Table middle = new Table();
            {
                equipmentTable = new Table();
                equipmentTable.top().center();
                equipmentTable.add(equipment[0] = createEquipmentSlot(0)).size(50, 35).colspan(3);
                equipmentTable.row();
                equipmentTable.add(equipment[1] = createEquipmentSlot(1)).size(50, 35);
                equipmentTable.add(equipment[2] = createEquipmentSlot(2)).size(50, 35);
                equipmentTable.add(equipment[10] = createEquipmentSlot(10)).size(50, 35);
                equipmentTable.row();
                equipmentTable.add(equipment[3] = createEquipmentSlot(3)).size(50, 35);
                equipmentTable.add(equipment[4] = createEquipmentSlot(4)).size(50, 35);
                equipmentTable.add(equipment[5] = createEquipmentSlot(5)).size(50, 35);
                equipmentTable.row();
                equipmentTable.add(equipment[6] = createEquipmentSlot(6)).size(50, 35).colspan(3);
                equipmentTable.row();
                equipmentTable.add(equipment[7] = createEquipmentSlot(7)).size(50, 35);
                equipmentTable.add(equipment[8] = createEquipmentSlot(8)).size(50, 35);
                equipmentTable.add(equipment[9] = createEquipmentSlot(9)).size(50, 35);
                equipmentTable.row();
                equipmentTable.add().padBottom(5);
                equipmentTable.pack();
                equipmentTable.setBackground(Backgrounds.create(new Color(0, 0, 0, 0.3F), Color.BLACK, (int) equipmentTable.getWidth(), (int) equipmentTable.getHeight()));

                Table duelRules = new Table();
                for (int r = 0; r < duelRuleStr.length; r++) {
                    duelRules.add(createDuelRule(r)).expandX().fillX();
                    duelRules.row();
                }
                duelRules.pack();
                duelRules.setBackground(Backgrounds.create(new Color(0, 0, 0, 0.3F), Color.BLACK, (int) duelRules.getWidth() + 5, (int) duelRules.getHeight() + 5));
                middle.add(equipmentTable);
                middle.row();
                middle.add(duelRules).pad(5);
            }

            /**
             * Table on Right side
             */
            Table rightSide = new Table();
            {
                rightSide.add(new Label("Inventory", labelStyle)).colspan(3);
                rightSide.row();
                rightSide.add(inventoryItems).colspan(3).right();
                rightSide.row();
                acceptedCell = rightSide.add(acceptButton).size(70, 35).left();
                rightSide.add(otherAcceptedLabel = new Label("", Style.labelBoldRegularW)).size(70, 35);
                rightSide.add(decline).size(70, 35).right();
                rightSide.pack();
            }

            add(duelOffers).pad(5);
            add(middle).pad(5);
            add(rightSide).pad(5);
            pack();
        }

        private Table createDuelRule(final int i) {
            Table t = new Table();
            t.align(Align.left);
            Stack stack = new Stack();
            stack.setSize(15, 15);

            Image img = new Image(Backgrounds.create(new Color(0, 0, 0, 0), Color.BLACK, 15, 15));
            img.setSize(15, 15);
            img.setAlign(Align.left);
            img.setScaling(Scaling.none);

            final Image status = new Image();
            status.setAlign(Align.left);
            status.setScaling(Scaling.none);
            status.setSize(13, 10);

            duelRuleLabels[i] = status;

            stack.add(img);
            stack.add(status);


            Label text = new Label(duelRuleStr[i], Style.labelBoldRegularW);
            text.setAlignment(Align.right);
            t.add(stack).pad(3);
            t.add(text);
            t.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    duelRules[i] = !duelRules[i];
                    if (duelRules[i])
                        status.setDrawable(new TextureRegionDrawable(new TextureRegion(Assets.inst.getInterSprite(25))));
                    else
                        status.setDrawable(null);
                    sendRules();
                    return true;
                }
            });
            return t;
        }

        public void setEquipmentRule(int i, boolean b) {
            equipmentRules[i] = b;
            equipment[i].getBackground().setColor(equipmentRules[i] ? Color.RED : Color.WHITE);
        }

        public void setDuelRule(int i, boolean b) {
            duelRules[i] = b;

            if (duelRules[i])
                duelRuleLabels[i].setDrawable(new TextureRegionDrawable(new TextureRegion(Assets.inst.getInterSprite(25))));
            else
                duelRuleLabels[i].setDrawable(null);
        }

        private ItemSlot createEquipmentSlot(final int i) {
            final ItemSlot itemSlot = new ItemSlot();
            itemSlot.getAmountLabel().setText(equipmentSlotNames[i]);
            itemSlot.getAmountLabel().setAlignment(Align.center);
            itemSlot.addListener(new InputListener() {
                @Override
                public boolean mouseMoved(InputEvent event, float x, float y) {
                    ActionMenu actionMenu = Game.ui().getActionMenu();
                    actionMenu.clearMenu();

                    actionMenu.add("Toggle rule", new GameAction() {
                        @Override
                        public void execute() {
                            equipmentRules[i] = !equipmentRules[i];
                            itemSlot.getBackground().setColor(equipmentRules[i] ? Color.RED : Color.WHITE);
                            sendRules();
                        }
                    }, OptionPriority.ITEM_REMOVE_EQUIPPED);
                    return true;
                }

            });
            return itemSlot;
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

        private void removeDuelItem(Item item, int amount) {
            if (!ourDuelOffer.containsItem(item)) {
                return;
            }
            if (amount > ourDuelOffer.countItem(item.getId()))
                amount = ourDuelOffer.countItem(item.getId());

            Item firstItem = ourDuelOffer.getFirstItem(item.getId());
            if (item.getDef().isStackable()) {

                firstItem.setAmount(firstItem.getAmount() - amount);
                ourDuelOffer.set(firstItem.getSlotIndex(), firstItem.getId(), firstItem.getAmount());

                if (firstItem.getAmount() <= 0) {
                    ourDuelOffer.set(firstItem.getSlotIndex(), -1, -1);
                    for (int slot = firstItem.getSlotIndex(); slot < ourDuelOffer.size() - 1; ++slot) {
                        Item nextSlot = ourDuelOffer.get(slot + 1);
                        ourDuelOffer.set(slot, nextSlot.getId(), nextSlot.getAmount());
                    }
                }
            } else {
                for (int removed = 0; removed < amount; removed++) {
                    firstItem = ourDuelOffer.getFirstItem(item.getId());

                    ourDuelOffer.set(firstItem.getSlotIndex(), -1, -1);
                    for (int slot = firstItem.getSlotIndex(); slot < ourDuelOffer.size() - 1; ++slot) {
                        Item nextSlot = ourDuelOffer.get(slot + 1);
                        ourDuelOffer.set(slot, nextSlot.getId(), nextSlot.getAmount());
                    }
                }
            }
            sendDuelItems();
        }

        private void addDuelItem(Item item, int amount) {
            if (amount == 0) {
                return;
            }
            int invCount = inventoryItems.getContainer().countItem(item.getId());
            if (item.getDef().isStackable()) {
                if (ourDuelOffer.containsItem(item)) {
                    Item existingItem = ourDuelOffer.getFirstItem(item.getId());
                    existingItem.setAmount(amount + existingItem.getAmount());
                    if (existingItem.getAmount() >= invCount) {
                        existingItem.setAmount(invCount);
                    }
                    ourDuelOffer.set(existingItem.getSlotIndex(), existingItem.getId(), existingItem.getAmount());
                } else {
                    ourDuelOffer.set(ourDuelOffer.count(), item.getId(), amount);
                }
            } else {
                for (int offered = 0; offered < amount && ourDuelOffer.count() < ourDuelOffer.size() && invCount > ourDuelOffer.countItem(item.getId()); ++offered) {
                    ourDuelOffer.set(ourDuelOffer.count(), item.getId(), 1);
                }
            }
            sendDuelItems();
        }


        private void sendDuelItems() {
            Game.outBuffer().newPacket(8);
            Game.outBuffer().putByte(3);
            Game.outBuffer().putByte(ourDuelOffer.size());

            for (Item i : (ArrayList<Item>) ourDuelOffer.getItems()) {
                Game.outBuffer().putShort(i.getId());
                Game.outBuffer().putInt(i.getAmount());
            }
            Game.outBuffer().finishPacket();

            setAccepted(false);
            setRecipientAccepted(false);
        }

        public void sendRules() {

            Game.outBuffer().newPacket(8);
            Game.outBuffer().putByte(4);
            Game.outBuffer().putByte(this.duelRules.length);
            Game.outBuffer().putByte(this.equipmentRules.length);
            for (int i = 0; i < duelRules.length; ++i) {
                Game.outBuffer().putByte(duelRules[i] ? 1 : 0);
            }
            for (int i = 0; i < equipmentRules.length; ++i) {
                Game.outBuffer().putByte(equipmentRules[i] ? 1 : 0);
            }
            Game.outBuffer().finishPacket();
            setAccepted(false);
            setRecipientAccepted(false);
        }


        public void setRecipientName(String recipient, int h) {
            opponentName.setText(recipient);
            hpOffset.setText("Hitpoint offset: " + h);
        }

        public void reset() {
            getOurDuelOffer().reset();
            getRecipientDuelOffer().reset();
            for (int i = 0; i < duelRules.length; ++i) {
                setDuelRule(i, false);
            }
            for (int i = 0; i < equipmentRules.length; ++i) {
                setEquipmentRule(i, false);
            }
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
