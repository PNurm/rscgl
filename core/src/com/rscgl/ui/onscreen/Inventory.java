package com.rscgl.ui.onscreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.assets.RSCache;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.container.*;
import com.rscgl.ui.menu.GameAction;
import com.rscgl.ui.menu.option.OptionPriority;
import com.rscgl.ui.menu.ActionMenu;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Style;

public class Inventory extends Table implements PacketHandler {

    private final Cell<?> currentMenu;
    private final ItemContainerTable inventoryTable;

    private ItemAction castSpellOptions = new ItemAction() {
        @Override
        public String getOption(Item itemSlot) {
            int spell = Game.ui().getSpellMenu().getSelectedSpell();
            if (spell == -1)
                return "";

            return "Cast " + RSCache.SPELLS[spell].getName() + " on " + itemSlot.getDef().getName();
        }

        @Override
        public boolean visibleFor(Item itemSlot) {
            return true;
        }

        @Override
        public void action(Item itemSlot) {
            int spell = Game.ui().getSpellMenu().getSelectedSpell();

            Game.outBuffer().newPacket(4);
            Game.outBuffer().putShort(spell);
            Game.outBuffer().putShort(itemSlot.getSlotIndex());
            Game.outBuffer().finishPacket();

            Game.ui().getSpellMenu().setSelectedSpell(-1);
        }
    };

    public ItemContainer getContainer() {
        return inventoryContainer;
    }

    private ItemContainer inventoryContainer;

    private ItemSlot[] equipment = new ItemSlot[11];

    private ItemAction useWithOption = new ItemAction() {
        @Override
        public String getOption(Item itemSlot) {
            if (selectedSlot == -1)
                return "";

            return "Use " + inventoryContainer.get(selectedSlot).getDef().getName() + " with";
        }

        @Override
        public boolean visibleFor(Item itemSlot) {
            return true;
        }

        @Override
        public void action(Item itemSlot) {

        }
    };

    private ItemAction[] defaultItemOptions = new ItemAction[]{
            new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Equip";
                }

                @Override
                public boolean visibleFor(Item itemSlot) {
                    return itemSlot.getDef().isWieldable();
                }

                @Override
                public void action(Item itemSlot) {

                    Game.outBuffer().newPacket(169);
                    Game.outBuffer().putShort(itemSlot.getSlotIndex());
                    Game.outBuffer().finishPacket();

                }
            },
            new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return itemSlot.getDef().getCommand();
                }

                @Override
                public boolean visibleFor(Item itemSlot) {
                    return !itemSlot.getDef().getCommand().equals("")
                            && itemSlot.getDef().getNotedFormOf() == 0;
                }

                @Override
                public void action(Item itemSlot) {
                    Game.outBuffer().newPacket(90);
                    Game.outBuffer().putShort(itemSlot.getSlotIndex());
                    Game.outBuffer().finishPacket();
                }
            },
            new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Use";
                }

                @Override
                public boolean visibleFor(Item itemSlot) {
                    return true;
                }

                @Override
                public void action(Item itemSlot) {
                    selectedSlot = itemSlot.getSlotIndex();
                }
            },
            new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Drop";
                }

                @Override
                public boolean visibleFor(Item itemSlot) {
                    return true;
                }

                @Override
                public void action(Item itemSlot) {
                    int amount = 1;
                    if (itemSlot.getDef().isStackable())
                        amount = itemSlot.getAmount();

                    Game.outBuffer().newPacket(169);
                    Game.outBuffer().putShort(itemSlot.getSlotIndex());
                    Game.outBuffer().putInt(amount);
                    Game.outBuffer().finishPacket();
                }
            },
            new ItemAction() {
                @Override
                public String getOption(Item itemSlot) {
                    return "Examine";
                }

                @Override
                public boolean visibleFor(Item itemSlot) {
                    return true;
                }

                @Override
                public void action(Item itemSlot) {
                    Game.ui().getChat().addMessage(itemSlot.getDef().getDescription());
                }
            }
    };

    public int getSelectedSlot() {
        return selectedSlot;
    }

    private int selectedSlot = -1;

    public Inventory() {
        Game.inst.registerPacketHandler(this);
        setName("Inventory_Slot_Table");
        background(Backgrounds.create(Colors.BG_WHITE2, 10, 10));

        inventoryContainer = new ItemContainer(30);
        inventoryTable = new ItemContainerTable(5, false, inventoryContainer);
        inventoryTable.setItemOptions(defaultItemOptions);

        /**
         * I don't like the way I did this.. but sacrifices must be made..
         * I mean this ain't that bad approach, right?? it could be worse.
         */
        inventoryTable.addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                if (Game.ui().getSpellMenu().getSelectedSpell() != -1) {
                    inventoryTable.setItemOptions(castSpellOptions, new ItemAction() {
                        @Override
                        public String getOption(Item itemSlot) {
                            return "Cancel";
                        }

                        @Override
                        public void action(Item itemSlot) {
                            Game.ui().getSpellMenu().setSelectedSpell(-1);
                            selectedSlot = -1;
                        }
                    });
                } else if (selectedSlot != -1) {
                    inventoryTable.setItemOptions(useWithOption, new ItemAction() {
                        @Override
                        public String getOption(Item itemSlot) {
                            return "Cancel";
                        }

                        @Override
                        public void action(Item itemSlot) {
                            selectedSlot = -1;
                        }
                    });
                } else {
                    inventoryTable.setItemOptions(defaultItemOptions);
                }
                return false;
            }
        });

        inventoryTable.pack();
        currentMenu = add(inventoryTable);
        pack();
        setVisible(false);
    }

    public void setSelectedSlot(int i) {
        this.selectedSlot = i;
    }

    public Item get(int selectedSlot) {
        return getContainer().get(selectedSlot);
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {

        if (opcode == 53) {
            int inventoryItemCount = packet.readUnsignedByte();
            ItemContainer inventory = getContainer();
            inventory.reset();
            for (int i = 0; i < inventoryItemCount; ++i) {
                int itemID = packet.readShort();
                int equipped = packet.readByte();
                int amount = 1;
                if (itemID < RSCache.ITEM_COUNT && RSCache.ITEMS[itemID].isStackable()) {
                    amount = packet.readInt();
                }
                inventory.set(i, itemID, amount);
            }
            return;
        }
        if (opcode == 90) {
            int slot = packet.readUnsignedByte();
            int itemID = packet.readShort();
            int stackSize = 1;
            if (RSCache.ITEMS[itemID & 32767].isStackable()) {
                stackSize = packet.readInt();
            }
            getContainer().set(slot, itemID & 32767, stackSize);
            return;
        }
        if (opcode == 123) {
            int slot = packet.readUnsignedByte();
            for (int index = slot; 29 > index; ++index) {
                Item itemSlot2 = get(index + 1);
                getContainer().set(index, itemSlot2.getId(), itemSlot2.getAmount());
            }
            return;
        }

    }

    @Override
    public int[] opcodes() {
        return new int[]{53, 90, 123, 102};
    }

    public boolean isEquipped(int id) {
        for (int i = 0; equipment.length > i; ++i) {
            if (equipment[i].getItem() != null && id == equipment[i].getItem().getId() && equipment[i].getItem().getAmount() == 1) {
                return true;
            }
        }
        return false;
    }
}
