package com.rscgl.ui.onscreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Style;

public class Stats extends Table implements PacketHandler {



    private final Skills skills;
    private final Quests quests;
    private final Cell<?> currentMenu;

    public Quests getQuests() {
        return quests;
    }

    public Skills getSkills() {
        return skills;
    }

    public Stats() {
        Game.inst.registerPacketHandler(this);

        final TextButton skillsButton = new TextButton("Stats", Style.bigButtonWhite);
        skillsButton.align(Align.center);
        skillsButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                currentMenu.setActor(skills);
                skillsButton.setChecked(true);
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        skillsButton.pack();

        final TextButton questsButton = new TextButton("Quests", Style.bigButtonWhite);
        questsButton.align(Align.center);
        questsButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                currentMenu.setActor(quests);
                questsButton.setChecked(true);
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        questsButton.pack();

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.add(skillsButton, questsButton);

        skills = new Skills();
        quests = new Quests();

        Table buttonTable = new Table();
        buttonTable.add(skillsButton).width(98).height(24);
        buttonTable.add(questsButton).width(98).height(24);
        add(buttonTable);
        row();
        currentMenu = add(skills);
        pack();
        background(Backgrounds.create(Colors.BG_WHITE2, 10, 10));
        setVisible(false);
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 5) {
            int updateQuestType = packet.readByte();
            if (updateQuestType == 0) {
                int questCount = packet.readByte();
                for (int i = 0; i < questCount; i++) {
                    int questId = packet.readInt();
                    int questStage = packet.readInt();
                    String questName = packet.readString();
                    getQuests().addQuest(questId, questStage, questName);
                }
            } else if (updateQuestType == 1) {
                int questID = packet.readInt();
                int stage = packet.readInt();
                getQuests().updateQuestStage(questID, stage);
            }
            return;
        }
        if (opcode == 114) {
            Stats.Skills stats = getSkills();
            stats.setFatigue(packet.readShort());
            return;
        }
        if (opcode == 153) {
            Stats.Skills stats = getSkills();
            stats.setEquipmentStats(packet.readUnsignedByte(), packet.readUnsignedByte(), packet.readUnsignedByte(), packet.readUnsignedByte(), packet.readUnsignedByte());
            return;
        }
        if (opcode == 159) {
            int skill = packet.readUnsignedByte();
            //int oldXP = playerExperience[skill];
            getSkills().setCurStat(skill, packet.readUnsignedByte());
            getSkills().setMaxStat(skill, packet.readUnsignedByte());

            getSkills().setCurStat(skill, packet.readInt());
            /*int receivedXP = playerExperience[skill] - oldXP;
            if (receivedXP > 0 || oldXP >= 200000000) {
                xpNotifications.add(new XPNotification(skill, receivedXP));
            }*/
            return;
        }
        if (opcode == 156) {
            Stats.Skills stats = getSkills();
            for (int i = 0; i < 18; ++i) {
                stats.setCurStat(i, packet.readUnsignedByte());
            }
            for (int i = 0; i < 18; ++i) {
                stats.setMaxStat(i, packet.readUnsignedByte());
            }
            for (int i = 0; i < 18; ++i) {
                stats.setExperience(i, packet.readInt());
            }
            stats.setQuestPoints(packet.readUnsignedByte());
            stats.update();
            return;
        }
    }

    @Override
    public int[] opcodes() {
        return new int[]{5, 114, 153, 156, 159};
    }

    public class Skills extends Table {

        private final String[] skillNameLong = new String[]{"Attack", "Defense", "Strength", "Hits", "Ranged", "Prayer",
                "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining",
                "Herblaw", "Agility", "Thieving"};

        private final String[] skillNames = new String[]{"Attack", "Defense", "Strength", "Hits", "Ranged", "Prayer",
                "Magic", "Cooking", "Woodcut", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining",
                "Herblaw", "Agility", "Thieving"};

        private final int[] playerStatBase = new int[18];
        private final int[] playerStatCurrent = new int[18];
        private final int[] playerExperience = new int[18];
        private final int[] experienceArray = new int[99];
        private final TextButton[] skillLabels = new TextButton[18];

        private int selectedSkill = -1;

        private Label questPointLabel;
        private Label fatigueLabel;
        private Label armourLabel;
        private Label magicLabel;
        private Label weaponAimLabel;
        private Label prayerLabel;
        private Label weaponPower;
        private Label selectedSkillLabel;
        private Label line1;
        private Label line2;
        private Label line3;

        public int getFatigue() {
            return fatigue;
        }

        private int fatigue;

        public Skills() {

            int var10 = 0;
            for (int var3 = 0; var3 < 99; ++var3) {
                int var4 = 1 + var3;
                int var5 = (int) (300.0D * Math.pow(2.0D, (double) var4 / 7.0D) + (double) var4);
                var10 += var5;
                experienceArray[var3] = (var10 & 268435452) / 4;
            }

            Table skillTable = new Table();
            for (int i = 0; i < 9; i++) {
                skillTable.add(skillLabels[i] = new TextButton(skillNames[i] + ":[#FFFF00] -1", Style.smallTextButton)).left().padLeft(5);
                skillTable.add(skillLabels[i + 9] = new TextButton(skillNames[i + 9] + ":[#FFFF00] -1", Style.smallTextButton)).left().padLeft(5);
                skillTable.row();
            }
            for (int i = 0; i < 18; i++) {
                final int finalI = i;
                skillLabels[i].addListener(new InputListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        selectedSkill = finalI;
                        update();
                        super.enter(event, x, y, pointer, fromActor);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        for (int i = 0; i < 18; i++) {
                            if (toActor == skillLabels[i])
                                return;
                        }
                        selectedSkill = -1;
                        update();
                        super.exit(event, x, y, pointer, toActor);
                    }
                });
            }
            skillTable.add(fatigueLabel = new Label("Fatigue: -1", Style.labelBoldRegularW)).left().padLeft(5);
            skillTable.add(questPointLabel = new Label("Quest points: -1", Style.labelBoldRegularW)).left().padLeft(5);
            row();

            Table eqStats = new Table();
            eqStats.add(armourLabel = new Label("Armour: ", Style.labelBoldRegularW)).left().padLeft(5);
            eqStats.add(magicLabel = new Label("Magic: ", Style.labelBoldRegularW)).left().padLeft(5);
            eqStats.row();
            eqStats.add(weaponAimLabel = new Label("WeaponAim: ", Style.labelBoldRegularW)).left().padLeft(5);
            eqStats.add(prayerLabel = new Label("Prayer: ", Style.labelBoldRegularW)).left().padLeft(5);
            eqStats.row();
            eqStats.add(weaponPower = new Label("WeaponAim: ", Style.labelBoldRegularW)).colspan(2).left().padLeft(5);
            eqStats.row();

            Table t2 = new Table();
            t2.add(selectedSkillLabel = new Label("Overall levels: ", Style.labelBoldRegularW)).left().padLeft(5);
            t2.row();
            t2.add(line1 = new Label("Skill total:", Style.labelBoldRegularW)).left().padLeft(5);
            t2.row();
            t2.add(line2 = new Label("Combat level", Style.labelBoldRegularW)).left().padLeft(5);
            t2.row();
            t2.add(line3 = new Label("", Style.labelBoldRegularW)).left().padLeft(5);
            t2.row();


            add(new Label("[#FFFF00]Stats", Style.labelBoldRegularW)).left().padLeft(5);
            row();
            add(skillTable);
            row();
            add(new Label("[#FFFF00]Equipment Status", Style.labelBoldRegularW)).colspan(2).left().padLeft(5);
            row();
            add(eqStats);
            row();
            add(t2).padBottom(10).padTop(10);
            pack();
        }

        public void update() {
            for (int i = 0; i < 18; i++) {
                skillLabels[i].setText(skillNames[i] + ":[#FFFF00]" + playerStatCurrent[i] + "/" + playerStatBase[i]);
            }
            if (selectedSkill >= 0) {
                int nextLevel = this.experienceArray[0];
                for (int i = 0; i < 98; ++i) {
                    if (this.experienceArray[i] <= this.playerExperience[selectedSkill]) {
                        nextLevel = this.experienceArray[i + 1];
                    }
                }
                selectedSkillLabel.setText("[#FFFF00]" + skillNameLong[selectedSkill] + " skill");
                line1.setText("Total xp: " + playerExperience[selectedSkill]);
                line2.setText("Next level at: " + nextLevel);
                line3.setText("Remaining XP: " + (nextLevel - this.playerExperience[selectedSkill]));
            } else {
                int totalLevel = 0;
                for (int var12 = 0; var12 < 18; ++var12) {
                    totalLevel += this.playerStatBase[var12];
                }
                selectedSkillLabel.setText("Overall levels");
                line1.setText("Skill total: " + totalLevel);
                line2.setText("Combat level: " + Game.inst.localPlayer.combatLevel);
                line3.setText("");
            }
        }

        public void setQuestPoints(int questPoints) {
            this.questPointLabel.setText("Quest points:[#FFFF00] " + questPoints);
        }

        public void setExperience(int id, int exp) {
            this.playerExperience[id] = exp;
        }

        public void setMaxStat(int id, int lvl) {
            this.playerStatBase[id] = lvl;
        }

        public void setCurStat(int id, int lvl) {
            this.playerStatCurrent[id] = lvl;
        }

        public void setFatigue(int fat) {
            this.fatigue = fat;
            this.fatigueLabel.setText("Fatigue: [#FFFF00]" + ((fat * 1000 / 750) / 10));
        }

        public void setEquipmentStats(int a, int wa, int wp, int mg, int pr) {
            armourLabel.setText("Armour:[#FFFF00] " + a);
            magicLabel.setText("Magic: [#FFFF00]" + mg);
            weaponAimLabel.setText("WeaponAim:[#FFFF00] " + wa);
            prayerLabel.setText("Prayer: [#FFFF00]" + pr);
            weaponPower.setText("WeaponAim: [#FFFF00]" + wp);
        }

        public int getMaxStat(int i) {
            return playerStatBase[i];
        }


        public int getCurStat(int i) {
            return playerStatCurrent[i];
        }
    }

    public class Quests extends Table {

        private final ScrollPane scrollPane;
        private Table originalQuests;

        public Quests() {

            TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.fontColor = Color.WHITE;
            buttonStyle.up = buttonStyle.over = Backgrounds.create(Colors.BG_WHITE2, Color.BLACK, 98, 24);
            buttonStyle.checked = buttonStyle.checkedOver = Backgrounds.create(Colors.BG_WHITE, Color.BLACK, 98, 24);
            buttonStyle.font = Fonts.Font14B;
            buttonStyle.font.getData().markupEnabled = true;

            originalQuests = new Table();
            for (int i = 0; i < 100; i++) {
                originalQuests.add();
                originalQuests.row();
            }

            scrollPane = new ScrollPane(originalQuests, Style.scrollStyle);
            scrollPane.setForceScroll(true, true);
            scrollPane.setScrollbarsOnTop(true);
            scrollPane.setFlickScroll(true);
            scrollPane.setSmoothScrolling(true);
            scrollPane.setOverscroll(false, false);
            scrollPane.setFadeScrollBars(false);

            final TextButton originalButton = new TextButton("Original", buttonStyle);
            originalButton.align(Align.center);
            originalButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    scrollPane.setActor(originalQuests);
                    originalButton.setChecked(true);
                    scrollPane.invalidate();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });

            row();
            add(scrollPane);
            row();
            pack();
            background(Backgrounds.create(Colors.BG_WHITE, 10, 10));
        }

        public void addQuest(int id, int stage, String name) {
            Label label = new Label(name, Style.labelBoldRegularW);
            label.setColor(getStageColor(stage));
            originalQuests.getCells().get(id).setActor(label);
        }

        public void updateQuestStage(int questID, int stage) {
            Label label = (Label) originalQuests.getCells().get(questID).getActor();
            label.setColor(getStageColor(stage));
        }

        public Color getStageColor(int stage) {
            if (stage > 0) {
                return Color.YELLOW;
            }
            if (stage < 0) {
                return Color.GREEN;
            }
            return Color.RED;
        }
    }
}
