package com.rscgl.ui.onscreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.PrayerDef;
import com.rscgl.assets.def.SpellDef;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.container.Item;
import com.rscgl.ui.container.ItemSlot;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Style;

import java.util.Map;

public class Spellbook extends Table implements PacketHandler {

    private final Label selectedEntryDesc2;

    public int getSelectedSpell() {
        return selectedSpell;
    }

    private int selectedSpell = -1;

    private final ScrollPane scrollPane;
    private final Table prayerContainer;
    private final Table spellContainer;
    private final Label selectedEntryName;
    private final Label selectedEntryDesc;
    private final ItemSlot[] runes = new ItemSlot[4];
    private SpellDef spellHover;
    private PrayerDef prayerHover;

    public void setPrayerOn(int id, boolean b) {
        this.prayerOn[id] = b;
    }

    private boolean[] prayerOn = new boolean[50];

    public Spellbook() {
        Game.inst.registerPacketHandler(this);
        background(Backgrounds.create(Colors.BG_WHITE2, 10, 10));


        Label.LabelStyle labelStyle2 = new Label.LabelStyle();
        labelStyle2.font = Fonts.Font11P;
        labelStyle2.fontColor = Color.BLACK;

        spellContainer = new Table();
        for (int spellID = 0; spellID < RSCache.SPELL_COUNT; spellID++) {
            final SpellDef def = RSCache.SPELLS[spellID];

            final Label label = new Label("Level " + def.reqLevel + ": " + def.name, Style.labelBoldRegularW);
            final int finalSpellID = spellID;
            label.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (def.getSpellType() <= 1) {

                        Game.outBuffer().newPacket(137);
                        Game.outBuffer().putShort(finalSpellID);
                        Game.outBuffer().finishPacket();

                    }
                    int runeCount = 0;
                    for (Map.Entry<Integer, Integer> e : def.getRunesRequired()) {
                        if (!hasRunes(e.getKey(), e.getValue())) {
                            Game.ui().getChat().addMessage( "You don\'t have all the reagents you need for this spell");
                            runeCount = -1;
                            break;
                        }
                        runeCount++;
                    }
                    if (runeCount == def.getRuneCount()) {
                        selectedSpell = finalSpellID;
                        Game.ui().getInventory().setSelectedSlot(-1);
                    }
                    return true;
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    spellHover = def;
                    selectedEntryName.setText(spellHover.name);
                    selectedEntryDesc.setText(spellHover.description);
                    for (int i = 0; i < runes.length; i++) {
                        runes[i].set(null);
                    }
                    int rune = 0;
                    for (Map.Entry<Integer, Integer> e : spellHover.requiredRunes.entrySet()) {
                        int id = e.getKey();
                        int amt = e.getValue();
                        runes[rune].set(new Item().set(id, amt));

                        int invCount = Game.ui().getInventory().getContainer().countItem(id);
                        if (invCount >= amt) {
                            runes[rune].getAmountLabel().setText("[#00FF00]" + invCount + "/" + amt);
                        } else {
                            runes[rune].getAmountLabel().setText("[#FF0000]" + invCount + "/" + amt);
                        }
                        rune++;
                    }
                    super.enter(event, x, y, pointer, fromActor);
                }
            });

            spellContainer.add(label).padLeft(5).align(Align.left);
            spellContainer.row();
        }

        prayerContainer = new Table();
        for (int prayerID = 0; prayerID < RSCache.PRAYER_COUNT; prayerID++) {
            final int finalPrayerID = prayerID;
            final PrayerDef def = RSCache.PRAYERS[prayerID];

            Label prayerEntry = new Label("Level " + def.reqLevel + ": " + def.name, Style.labelBoldRegularW);
            prayerEntry.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                    if(Game.ui().getStatMenu().getSkills().getMaxStat(5) <= def.getReqLevel()) {
                        Game.ui().getChat().addMessage("Your prayer ability is not high enough for this prayer");
                        return true;
                    }
                    if(Game.ui().getStatMenu().getSkills().getCurStat(5) <= 0) {
                        Game.ui().getChat().addMessage("You have run out of prayer points. Return to a church to recharge");
                        return true;
                    }

                    if (!prayerOn[finalPrayerID]) {
                        Game.outBuffer().newPacket(60);
                        Game.outBuffer().putShort(finalPrayerID);
                        Game.outBuffer().finishPacket();

                        prayerOn[finalPrayerID] = true;
                        //Main.connection.playSoundFile("prayeron");
                    } else {
                        Game.outBuffer().newPacket(254);
                        Game.outBuffer().putShort(finalPrayerID);
                        Game.outBuffer().finishPacket();

                        prayerOn[finalPrayerID] = false;
                        //this.playSoundFile("prayeroff");
                    }
                    return true;
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    prayerHover = def;
                    selectedEntryName.setText(prayerHover.name);
                    selectedEntryDesc.setText(prayerHover.description);
                    selectedEntryDesc2.setText("Drain rate: " + prayerHover.drainRate);
                    super.enter(event, x, y, pointer, fromActor);
                }
            });
            prayerContainer.add(prayerEntry).padLeft(5).align(Align.left);
            prayerContainer.row();
        }


        scrollPane = new ScrollPane(spellContainer, Style.scrollStyle);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setFlickScroll(true);
        scrollPane.setOverscroll(false, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setForceScroll(false, true);

        final TextButton spellButton = new TextButton("Spells", Style.bigButtonWhite);
        spellButton.align(Align.center);
        spellButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                scrollPane.setActor(spellContainer);
                scrollPane.invalidate();
                spellButton.setChecked(true);

                selectedEntryDesc.setText("");
                selectedEntryName.setText("");
                selectedEntryDesc2.setText("");
                prayerHover = null;

                return super.touchDown(event, x, y, pointer, button);
            }
        });

        final TextButton prayersButton = new TextButton("Prayers", Style.bigButtonWhite);
        prayersButton.align(Align.center);
        prayersButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                scrollPane.setActor(prayerContainer);
                scrollPane.invalidate();

                for (int i = 0; i < 4; i++) {
                    runes[i].set(null);
                }

                selectedEntryDesc.setText("");
                selectedEntryName.setText("");
                selectedEntryDesc2.setText("");
                spellHover = null;

                prayersButton.setChecked(true);
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.add(spellButton, prayersButton);

        Table btn = new Table();
        btn.add(spellButton).width(98).height(24).colspan(2);
        btn.add(prayersButton).width(98).height(24).colspan(2);
        add(btn);
        row();
        add(scrollPane).expand().fill().width(200).height(100);// well if ur certain it will work ok
        row();
        add(selectedEntryName = new Label("Point at a thing for description", Style.labelBoldRegularB));
        row();
        add(selectedEntryDesc = new Label("", labelStyle2));
        row();
        add(selectedEntryDesc2 = new Label("", labelStyle2)).padBottom(0);
        row();
        Table rns = new Table();
        for (int i = 0; i < 4; i++) {
            runes[i] = new ItemSlot();
            runes[i].setBackground(null);
            rns.add(runes[i]).size(50, 35).padBottom(5).padTop(5);
        }
        add(rns).align(Align.center);
        row();
        pack();
        setVisible(false);
    }


    public void update() {
        for (int i = 0; i < RSCache.SPELL_COUNT; i++) {
            SpellDef def = RSCache.SPELLS[i];
            Label label = (Label) spellContainer.getChildren().get(i);
            label.setText("[#FFFF00]Level " + def.reqLevel + ": " + def.name);
            for (Map.Entry<?, ?> e : RSCache.SPELLS[i].getRunesRequired()) {
                if (hasRunes((Integer) e.getKey(), (Integer) e.getValue())) {
                    continue;
                }
                label.setText("[#FFFFFF]Level " + def.reqLevel + ": " + def.name);
                break;
            }
            if(Game.ui().getStatMenu().getSkills().getMaxStat(6) <= def.getReqLevel()) {
                label.setText("[#000000]Level " + def.reqLevel + ": " + def.name);
            }
        }
        for (int i = 0; i < RSCache.PRAYER_COUNT; i++) {
            PrayerDef def = RSCache.PRAYERS[i];

            Label label = (Label) prayerContainer.getChildren().get(i);
            if (prayerOn[i]) {
                label.setColor(Color.GREEN);
            } else {
                label.setColor(Color.WHITE);
            }
            if(Game.ui().getStatMenu().getSkills().getMaxStat(5) < def.getReqLevel()) {
                label.setColor(Color.BLACK);
            }
        }
    }

    public void setSelectedSpell(int i) {
        this.selectedSpell = i;
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 206) {
            for (int i = 0; length - 1 > i; ++i) {
                boolean enabled = packet.readByte() == 1;
                setPrayerOn(i, enabled);
            }
            return;
        }
    }

    private boolean hasRunes(int rune, int count) {
        Inventory inv = Game.ui().getInventory();
        if (rune == 31) {// fire
            if (inv.isEquipped(197) || inv.isEquipped(615) || inv.isEquipped(682))
                return true;
        } else if (rune == 32) { // water
            if (inv.isEquipped(102) || inv.isEquipped(616) || inv.isEquipped(683))
                return true;
        } else if (rune == 33) { // air
            if (inv.isEquipped(101) || inv.isEquipped(617) || inv.isEquipped(684))
                return true;
        } else if (rune == 34) { // earth
            if (inv.isEquipped(103) || inv.isEquipped(618) || inv.isEquipped(685))
                return true;
        }
        return inv.getContainer().countItem(rune) >= count;
    }


    @Override
    public int[] opcodes() {
        return new int[]{206};
    }
}
