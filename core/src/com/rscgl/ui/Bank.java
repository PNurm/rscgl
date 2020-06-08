package com.rscgl.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.container.Item;
import com.rscgl.ui.container.ItemContainer;
import com.rscgl.ui.container.ItemContainerTable;
import com.rscgl.ui.container.ItemAction;
import com.rscgl.ui.dialog.DialogInputAction;
import com.rscgl.ui.dialog.DialogPopup;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.util.Style;

public class Bank extends Table implements PacketHandler {

    private boolean withdrawNoted;

    private ItemContainerTable inventoryTable;
    private ItemContainerTable bankTable;

    private ItemContainer bank;

    private final ScrollPane scrollPane;

    private int lastX = 1;

    public Bank() {
        background(Backgrounds.create(Colors.BG_WHITE2, 5, 5));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.overFontColor = Color.RED;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.font =  Fonts.Font12B;
        buttonStyle.font.getData().markupEnabled = true;


        bank = new ItemContainer(500);
        bankTable = new ItemContainerTable(10, true, bank);
        bankTable.setItemOptions(new ItemAction() {
            @Override
            public String getOption(Item itemSlot) {
                return "Withdraw " + 1;
            }
            @Override
            public void action(Item itemSlot) {
                sendWithdraw(itemSlot, 1);
            }
        },new ItemAction() {
            @Override
            public String getOption(Item itemSlot) {
                return "Withdraw " + 5;
            }
            @Override
            public void action(Item itemSlot) {
                sendWithdraw(itemSlot, 5);
            }
        },new ItemAction() {
            @Override
            public String getOption(Item itemSlot) {
                return "Withdraw X";
            }
            @Override
            public void action(final Item itemSlot) {
                new DialogPopup("Enter amount to withdraw", "Withdraw", new DialogInputAction() {
                    @Override
                    public void action(String result) {
                        int amount = 0;
                        try {
                            amount = Integer.parseInt(result);
                        }catch (Exception e) {}
                        sendWithdraw(itemSlot, amount);
                        lastX = amount;
                    }
                });
            }
        },new ItemAction() {
            @Override
            public String getOption(Item itemSlot) {
                return "Withdraw " + lastX;
            }
            @Override
            public void action(final Item itemSlot) {
                sendWithdraw(itemSlot, lastX);
            }
        });

        scrollPane = new ScrollPane(bankTable, Style.scrollStyle);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setFlickScroll(true);
        scrollPane.setOverscroll(false, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setForceScroll(false, true);


        inventoryTable = new ItemContainerTable(10, false, Game.ui().getInventory().getContainer());
        inventoryTable.setItemOptions(new ItemAction() {
            @Override
            public String getOption(Item itemSlot) {
                return "Deposit " + 1;
            }
            @Override
            public void action(Item itemSlot) {
                sendDeposit(itemSlot, 1);
            }
        },new ItemAction() {
            @Override
            public String getOption(Item itemSlot) {
                return "Deposit " + 5;
            }
            @Override
            public void action(Item itemSlot) {
                sendDeposit(itemSlot, 5);
            }
        },new ItemAction() {
            @Override
            public String getOption(Item itemSlot) {
                return "Deposit X";
            }
            @Override
            public void action(final Item itemSlot) {
                new DialogPopup("Enter amount to deposit", "Deposit", new DialogInputAction() {
                    @Override
                    public void action(String result) {
                        int amount = 0;
                        try {
                            amount = Integer.parseInt(result);
                        }catch (Exception e) {}
                        sendDeposit(itemSlot, amount);
                        lastX = amount;
                    }
                });
            }
        },new ItemAction() {
            @Override
            public String getOption(Item itemSlot) {
                return "Deposit " + lastX;
            }
            @Override
            public void action(final Item itemSlot) {
                sendDeposit(itemSlot, lastX);
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
        final TextButton swapNote = new TextButton("Withdraw noted: " + withdrawNoted, buttonStyle);
        swapNote.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                withdrawNoted = !withdrawNoted;

                Game.outBuffer().newPacket(212);
                Game.outBuffer().putByte(1);
                Game.outBuffer().putByte(withdrawNoted ? 1 : 0);
                Game.outBuffer().finishPacket();

                swapNote.setText("Withdraw noted: " + withdrawNoted);
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        add(new Label("Bank:", Style.labelBoldRegularW)).left();
        add(close).right();
        row();
        add(scrollPane).colspan(2).height(175).pad(5);
        row();
        add(new Label("Inventory:", Style.labelBoldRegularW)).left();
        row();
        add(inventoryTable).colspan(2);
        row();
        add(swapNote).colspan(2);

        pack();
        setVisible(false);
        Game.inst.registerPacketHandler(this);
    }

    public ItemContainer getContainer() {
        return bank;
    }

    private void sendDeposit(Item itemSlot, int amount) {
        int invC = inventoryTable.getContainer().countItem(itemSlot.getId());
        if (amount > invC) {
            amount = invC;
        }

        Game.outBuffer().newPacket(23);
        Game.outBuffer().putShort(itemSlot.getId());
        Game.outBuffer().putInt(amount);
        Game.outBuffer().finishPacket();
    }

    public void sendWithdraw(Item itemSlot, int amount) {
        int invC = itemSlot.getAmount();
        if (amount > invC) {
            amount = invC;
        }
        Game.outBuffer().newPacket(22);
        Game.outBuffer().putShort(itemSlot.getId());
        Game.outBuffer().putInt(amount);
        Game.outBuffer().finishPacket();
    }

    public void updateItem(int slot, int item, int itemCount) {
        getContainer().set(slot, item, itemCount);
    }

    public void show() {
        align(Align.center);
        pack();

        setPosition((getStage().getWidth() - getWidth()) / 2, (getStage().getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 249) {
            int slot = packet.readShort();
            int item = packet.readShort();
            int itemCount = packet.readInt();
            updateItem(slot, item, itemCount);
            return;
        }
        if (opcode == 42) {
            int newBankItemCount = packet.readShort();
            int bankItemsMax = packet.readShort();
            getContainer().reset();
            for (int slot = 0; slot < newBankItemCount; ++slot) {
                getContainer().set(slot, packet.readShort(), packet.readInt());
            }
            show();
            return;
        }
    }

    @Override
    public int[] opcodes() {
        return new int[] {249, 42};
    }
}