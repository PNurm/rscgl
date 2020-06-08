package com.rscgl.ui.container;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.rscgl.Game;
import com.rscgl.ui.menu.GameAction;
import com.rscgl.ui.menu.option.OptionPriority;
import com.rscgl.ui.menu.ActionMenu;

import java.util.ArrayList;

public class ItemContainerTable<T extends ItemSlot> extends Table {

    private final boolean forceAmountLabel;
    private final Class<T> type;
    private ItemAction[] itemOptions;
    private ItemContainer itemContainer;
    private ArrayList<T> itemSlots;
    public ItemContainer getContainer() {
        return itemContainer;
    }

    public ItemContainerTable(final int columns, boolean forceAmountLabel, ItemContainer container) {
        this(columns, forceAmountLabel, container, (Class<T>) ItemSlot.class);
    }

    public ItemContainerTable(final int columns, boolean forceAmountLabel, ItemContainer container, Class<T> type) {
        this.type = type;
        this.forceAmountLabel = forceAmountLabel;
        this.itemContainer = container;
        if (itemContainer.size() > 0) {
            itemSlots = new ArrayList<T>();
            for (int i = 1; i <= itemContainer.size(); i++) {
                itemSlots.add(createSlot());
                if (i % columns == 0) {
                    row();
                }
            }
        }
        itemContainer.addListener(new ItemChangeListener() {
            @Override
            public void itemChanged(int slot, Item item) {
                ItemSlot itemSlot = itemSlots.get(slot);
                itemSlot.set(item);
            }

            @Override
            public void itemAdded(Item itemSlot) {
                ItemSlot slot = createSlot();
                slot.set(itemSlot);
            }

            @Override
            public void itemRemoved(int slot) {
                itemSlots.remove(slot);
                getChildren().removeIndex(slot);
            }
        });
        pack();
    }

    private T createSlot() {
        try {
            final T slot = ClassReflection.newInstance(type);;
            slot.setForceAmountLabel(forceAmountLabel);
            slot.set(new Item().set(-1, -1));
            add(slot).size(49, 35);
            slot.addCaptureListener(new InputListener() {
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if (slot.getItem().getDef() == null) {
                        return;
                    }
                    ActionMenu menu = Game.ui().getActionMenu();
                    if (button == 0) {
                        menu.getFirst().getOptionAction().execute();
                    } else if (button == 1) {
                        menu.show();
                        Vector2 v = slot.localToStageCoordinates(new Vector2(x, y));
                        menu.getUI().setPosition(v.x - (menu.getUI().getWidth() / 2), v.y - menu.getUI().getHeight() + 10);
                    }
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public boolean mouseMoved(InputEvent event, float x, float y) {
                    if (slot.getItem().getDef() == null) {
                        return false;
                    }
                    ActionMenu actionMenu = Game.ui().getActionMenu();
                    actionMenu.clearMenu();
                    if (itemOptions == null) {
                        return true;
                    }
                    for (final ItemAction option : itemOptions) {
                        if (option.visibleFor(slot.getItem())) {
                            actionMenu.add(option.getOption(slot.getItem()), slot.getItem().getDef().getName(), Color.valueOf("#FF9040"), new GameAction() {
                                @Override
                                public void execute() {
                                    option.action(slot.getItem());
                                }
                            }, OptionPriority.INSERT_ORDER);
                        }
                    }
                    return true;
                }
            });
            slot.pack();
            return slot;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public T getSlot(int i) {
        return itemSlots.get(i);
    }

    public void setItems(ItemContainer items) {
        this.itemContainer = items;
    }

    public void setItemOptions(ItemAction... options) {
        this.itemOptions = options;
    }

}
