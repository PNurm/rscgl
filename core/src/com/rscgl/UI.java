package com.rscgl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rscgl.actions.WalkToPointAction;
import com.rscgl.actions.gameobject.ItemOnObjectAction;
import com.rscgl.actions.gameobject.ObjectAction;
import com.rscgl.actions.grounditem.CastOnGroundItemAction;
import com.rscgl.actions.grounditem.TakeGroundItemAction;
import com.rscgl.actions.grounditem.UseOnGroundItemAction;
import com.rscgl.actions.npc.*;
import com.rscgl.actions.player.*;
import com.rscgl.actions.wallobject.ItemOnWallAction;
import com.rscgl.actions.wallobject.WallAction;
import com.rscgl.assets.RSCache;
import com.rscgl.assets.def.ItemDef;
import com.rscgl.assets.def.SpellDef;
import com.rscgl.model.Point2D;
import com.rscgl.model.entity.*;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.Bank;
import com.rscgl.ui.Duel;
import com.rscgl.ui.MouseClickIndicator;
import com.rscgl.ui.Trade;
import com.rscgl.ui.container.Item;
import com.rscgl.ui.dialog.DialogAction;
import com.rscgl.ui.dialog.DialogPopup;
import com.rscgl.ui.dialog.ServerMessagePopup;
import com.rscgl.ui.dialog.SleepDialog;
import com.rscgl.ui.menu.ActionMenu;
import com.rscgl.ui.menu.GameAction;
import com.rscgl.ui.menu.option.MenuOption;
import com.rscgl.ui.menu.option.OptionPriority;
import com.rscgl.ui.menu.option.PlayerOption;
import com.rscgl.ui.onscreen.*;
import com.rscgl.ui.shop.Shop;
import com.rscgl.ui.util.ColorUtil;
import com.rscgl.ui.util.Style;

public class UI implements Disposable, PacketHandler {

    private final Stage stage;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;

    private Shop shop;
    private Bank bank;
    private Trade trade;
    private Duel duel;
    private Chat chat;
    private TopMenu topMenu;
    private ActionMenu actionMenu;

    private Stats stats;
    private Social social;
    private Spellbook spellMenu;
    private Inventory inventory;
    private Settings wrench;
    private Minimap minimap;

    private MouseClickIndicator mouseClickIndicator;
    private CombatModeSelector combatModeSelector;
    private ScreenText screenText;
    private ServerMessagePopup serverMessagePopup;
    private SleepDialog sleepDialog;

    public UI() {
        this.camera = new OrthographicCamera();
        this.viewport = new ScalingViewport(Scaling.stretch, Config.RESOLUTION_WIDTH, Config.RESOLUTION_HEIGHT, camera);
        this.batch = new SpriteBatch();
        this.stage = new Stage(viewport, batch);
    }

    public UI setup() {
        Game.inst.registerPacketHandler(this);
        actionMenu = new ActionMenu();

        stage.addActor(inventory = new Inventory());
        stage.addActor(minimap = new Minimap());
        stage.addActor(stats = new Stats());
        stage.addActor(spellMenu = new Spellbook());
        stage.addActor(social = new Social());
        stage.addActor(wrench = new Settings());

        stage.addActor(topMenu = new TopMenu(inventory, minimap, stats, spellMenu, social, wrench));
        stage.addListener(topMenu.inputAdapter);

        topMenu.setPosition(stage.getWidth() - 199, stage.getHeight() - topMenu.getHeight() - 3);


        stage.addActor(chat = new Chat());
        stage.addActor(bank = new Bank());
        stage.addActor(shop = new Shop());
        stage.addActor(trade = new Trade());
        stage.addActor(duel = new Duel());
        stage.addActor(combatModeSelector = new CombatModeSelector());
        stage.addActor(actionMenu.getUI());
        stage.addActor(screenText = new ScreenText());
        serverMessagePopup = new ServerMessagePopup();
        sleepDialog = new SleepDialog();
        screenText.setFillParent(true);
        mouseClickIndicator = new MouseClickIndicator();

        stage.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (stage.getKeyboardFocus() == null) {
                    stage.setKeyboardFocus(chat.getChatInput());
                    return true;
                }
                return super.keyUp(event, keycode);
            }
        });
        return this;
    }

    public void refreshActionMenu() {
        ActionMenu actions = getActionMenu();
        if (actions.getUI().isVisible()) {
            return;
        }
        actions.clearMenu();

        Point2D pickedTile = Game.inst.lastPickedTile;

        final Spellbook spells = getSpellMenu();
        final Inventory inventory = getInventory();
        final int selectedSpell = spells.getSelectedSpell();
        final int selectedSlot = inventory.getSelectedSlot();
        final SpellDef spellDef = selectedSpell < 0 ? null : RSCache.SPELLS[selectedSpell];
        ItemDef selectedItem = null;
        if (selectedSlot >= 0) {
            selectedItem = RSCache.ITEMS[selectedSlot];//Cache.getItemDef(selectedSlot);
        }

        if (selectedSpell > -1 || selectedSlot > -1) {
            actions.add("Cancel ", new GameAction() {
                @Override
                public void execute() {
                    inventory.setSelectedSlot(-1);
                    spells.setSelectedSpell(-1);
                }
            }, OptionPriority.CANCEL);
        } else {
            if (pickedTile != null) {
                actions.add("Walk here", new WalkToPointAction(pickedTile.getX(), pickedTile.getY()), OptionPriority.LANDSCAPE_WALK_HERE);
            }
        }

        for (Entity playerEntity : Game.inst.getPicked(EntityType.PLAYER)) {
            final PlayerEntity player = (PlayerEntity) playerEntity;

            final int localX = player.getGameX();
            final int localY = player.getGameZ();

            int levelDelta = Game.inst.localPlayer.combatLevel - player.combatLevel;

            if (spellDef != null) {
                if (spellDef.getSpellType() == 1 || spellDef.getSpellType() == 2) {
                    actions.add(new PlayerOption("Cast " + RSCache.SPELLS[selectedSpell].name + " on", player, new CastOnPlayerAction(localX, localY, player.getServerIndex(), selectedSpell)
                            , OptionPriority.PLAYER_CAST_SPELL));
                }
            } else if (selectedItem != null) {
                Item selected = inventory.get(inventory.getSelectedSlot());
                actions.add(new PlayerOption("Use " + selected.getDef().getName() + " with", player, new ItemOnPlayerAction(localX, localY, player.getServerIndex(), inventory.getSelectedSlot())
                        , OptionPriority.PLAYER_USE_ITEM));
            } else {
                if (Game.inst.wildernessLevel() > 0) {
                    actions.add(new PlayerOption("Attack", player, new AttackPlayerAction(localX, localY, player.getServerIndex()),
                            levelDelta >= 0 && levelDelta < 5 ? OptionPriority.PLAYER_ATTACK_SIMILAR : OptionPriority.PLAYER_ATTACK_DIVERGENT));
                } else {
                    actions.add(new PlayerOption("Duel with", player, new DuelPlayerAction(player.getServerIndex()), OptionPriority.PLAYER_DUEL));
                }
                actions.add(new PlayerOption("Trade with", player, new TradePlayerAction(player.getServerIndex()), OptionPriority.PLAYER_TRADE));
                actions.add(new PlayerOption("Follow", player, new FollowPlayerAction(player.getServerIndex()), OptionPriority.PLAYER_FOLLOW));
            }
        }

        /**
         * Game objects
         */
        for (Entity entity : Game.inst.getPicked(EntityType.OBJECT)) {
            final ObjectEntity objectEntity = (ObjectEntity) entity;
            if (selectedItem != null) {
                actions.add("Use " + selectedItem.getName() + " with", objectEntity.getDef().getName(), Color.CYAN,
                        new ItemOnObjectAction(objectEntity, selectedSlot), OptionPriority.OBJECT_USE_ITEM);

            } else if (selectedSpell > 0) {

            } else {
                if (!objectEntity.getDef().getOption1().equalsIgnoreCase("WalkTo")) {
                    actions.add(objectEntity.getDef().getOption1(), objectEntity.getDef().getName(), Color.CYAN,
                            new ObjectAction(objectEntity, 0), OptionPriority.OBJECT_COMMAND1);
                }
                if (!objectEntity.getDef().getOption2().equalsIgnoreCase("Examine")) {
                    actions.add(objectEntity.getDef().getOption2(), objectEntity.getDef().getName(), Color.CYAN,
                            new ObjectAction(objectEntity, 1), OptionPriority.OBJECT_COMMAND2);
                }
                actions.add("Examine", objectEntity.getDef().getName() + "[" + objectEntity.getTileX() + ", " + objectEntity.getTileY() + "]", Color.CYAN, new GameAction() {
                    @Override
                    public void execute() {
                        ui.getChat().addMessage(objectEntity.getDef().description);
                    }
                }, OptionPriority.OBJECT_EXAMINE);
            }
        }

        /**
         * Wall objects
         */

        for (Entity entity : Game.inst.getPicked(EntityType.WALL)) {
            final WallEntity wallEntity = (WallEntity) entity;
            if (inventory.getSelectedSlot() > 0) {
                Item selected = inventory.get(inventory.getSelectedSlot());
                actions.add("Use " + selected.getDef().getName() + " with", wallEntity.getDef().getName(), Color.CYAN, new ItemOnWallAction(wallEntity, selectedSlot), OptionPriority.WALL_USE_ITEM);
            } else {
                if (!wallEntity.getDef().getOption1().equalsIgnoreCase("WalkTo")) {
                    actions.add(wallEntity.getDef().getOption1(), wallEntity.getDef().getName(), Color.CYAN, new WallAction(wallEntity, 0), OptionPriority.WALL_COMMAND1);
                }
                if (!wallEntity.getDef().getOption2().equalsIgnoreCase("WalkTo")) {
                    actions.add(wallEntity.getDef().getOption2(), wallEntity.getDef().getName(), Color.CYAN, new WallAction(wallEntity, 1), OptionPriority.WALL_COMMAND2);
                }
                actions.add("Examine [dir: " + wallEntity.getDirection() + "]", wallEntity.getDef().getName(), Color.CYAN, new GameAction() {
                    @Override
                    public void execute() {
                        ui.getChat().addMessage(wallEntity.getDef().description);
                    }
                }, OptionPriority.WALL_EXAMINE);
            }
        }
        /**
         * Ground Items
         */
        for (Entity entity : Game.inst.getPicked(EntityType.ITEM)) {
            final ItemEntity g = (ItemEntity) entity;

            final int localX = g.getTileX();
            final int localY = g.getTileY();

            if (selectedSpell > 0) {
                if (RSCache.SPELLS[selectedSpell].getSpellType() == 3) {
                    actions.add("Cast " + RSCache.SPELLS[selectedSpell].name + " on", g.getDef().getName(), Color.valueOf("#FF9040"),
                            new CastOnGroundItemAction(g.getType(), localX, localY, selectedSpell), OptionPriority.GROUND_ITEM_CAST_SPELL);
                }
            } else if (inventory.getSelectedSlot() > 0) {
                Item selected = inventory.get(inventory.getSelectedSlot());
                actions.add("Use " + selected.getDef().getName() + " with", g.getDef().getName(), Color.valueOf("#FF9040"), new UseOnGroundItemAction(localX, localY, g.getType(), selectedSlot),
                        OptionPriority.GROUND_ITEM_USE_ITEM);
            } else {
                actions.add("Take ", g.getDef().getName(), Color.valueOf("#FF9040"), new TakeGroundItemAction(localX, localY, g.getType()), OptionPriority.GROUND_ITEM_TAKE);
                actions.add("Examine ", g.getDef().getName(), Color.valueOf("#FF9040"), new GameAction() {
                    @Override
                    public void execute() {
                        ui.getChat().addMessage(g.getDef().getDescription());
                    }
                }, OptionPriority.GROUND_ITEM_EXAMINE);
            }
        }

        /**
         * NPCS
         */
        for (Entity entity : Game.inst.getPicked(EntityType.NPC)) {

            final NpcEntity npc = (NpcEntity) entity;
            final int localX = npc.getGameX();
            final int localY = npc.getGameZ();
            String level = ColorUtil.getLevelColor(Game.inst.localPlayer.combatLevel, npc.combatLevel);
            if (!npc.getDef().isAttackable())
                level = "";

            if (spellDef != null) {
                if (spellDef.getSpellType() == 2) {
                    actions.add("Cast " + spellDef.name + " on", npc.getDef().getName() + level, Color.YELLOW,
                            new CastOnNpcAction(localX, localY, npc.getServerIndex(), selectedSpell), OptionPriority.NPC_CAST_SPELL);
                }
            } else if (selectedItem != null) {
                actions.add("Use " + selectedItem.getName() + " with", npc.getDef().getName(), Color.YELLOW,
                        new ItemOnNpcAction(localX, localY, npc.getServerIndex(), selectedSlot), OptionPriority.NPC_USE_ITEM);
            } else {
                if (npc.getDef().isAttackable()) {
                    actions.add("Attack", npc.getDef().getName() + level, Color.YELLOW, new AttackNpcAction(localX, localY, npc.getServerIndex()), OptionPriority.NPC_ATTACK1);
                }
                actions.add("Talk to", npc.getDef().getName() + level, Color.YELLOW, new TalkToNpcAction(localX, localY, npc.getServerIndex()), OptionPriority.NPC_TALK_TO);

                if (npc.getDef().command != null && npc.getDef().command.length() > 0) {
                    actions.add(npc.getDef().command, npc.getDef().getName() + level, Color.YELLOW, new NpcCommandAction(localX, localY, npc.getServerIndex()), OptionPriority.NPC_COMMAND);
                }
                actions.add("Examine", npc.getDef().getName(), Color.YELLOW, new GameAction() {
                    @Override
                    public void execute() {
                        ui.getChat().addMessage(npc.getDef().getDescription());
                    }
                }, OptionPriority.NPC_EXAMINE);


            }
        }
        actions.sort();
    }

    public void render() {
        stage.getBatch().enableBlending();
        stage.act();
        stage.draw();
        renderOverlays();
        spellMenu.update();

        getCombatModeSelector().setVisible(Game.inst.localPlayer.getDirection() == MobEntity.Direction.COMBAT_A
                || Game.inst.localPlayer.getDirection() == MobEntity.Direction.COMBAT_B);
    }

    public void renderOverlays() {
        viewport.getCamera().update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        getMouseClickIndicator().render(batch);

        MenuOption action = actionMenu.getFirst();
        if (action != null) {
            String str = "";
            if ((getInventory().getSelectedSlot() >= 0 || getSpellMenu().getSelectedSpell() >= 0) && actionMenu.getSize() == 1) {
                str = "Choose a target";
            } else {
                str += actionMenu.getFirst().getAction();
                if (action.getSubject() != null) {
                    str += "[#" + action.getSubjectColor().toString() + "] " + action.getSubject();
                }
            }
            if (actionMenu.getSize() == 2) {
                str += "[#FFFFFF] / 1 more menu";
            } else if (actionMenu.getSize() > 2) {
                str += "[#FFFFFF] / " + actionMenu.getSize() + " more options";
            }
            screenText.menuStatus.setText(str);
        }
        screenText.fps.setText("FPS:" + Gdx.graphics.getFramesPerSecond());
        screenText.draws.setText("Draws:" + Game.inst.glProfiler.getDrawCalls());
        screenText.textBinds.setText("TextBinds:" + Game.inst.glProfiler.getTextureBindings());
        screenText.shaderSwitches.setText("ShaderSwitches:" + Game.inst.glProfiler.getShaderSwitches());
        screenText.viewportUI.setText("Location: [" + Game.inst.localPlayerX + ", " + Game.inst.localPlayerY + "]");

        screenText.viewportGame.setText("Game: " + Game.inst.getViewport().getWorldWidth() + "x" + Game.inst.getViewport().getWorldHeight());

        batch.end();
    }

    public Settings getSettings() {
        return wrench;
    }

    class ScreenText extends Table {

        public final Label fps;
        public final Label draws;
        public final Label textBinds;
        public final Label shaderSwitches;
        public final Label viewportGame;
        public final Label viewportUI;
        public final Label menuStatus;

        public ScreenText() {
            align(Align.topLeft);

            add(menuStatus = new Label("", Style.labelBoldBigW)).left();
            row();
            row();
            add(fps = new Label("FPS:", Style.labelBoldRegularW)).left();
            row();
            add(draws = new Label("Draws:", Style.labelBoldRegularW)).left();
            row();
            add(textBinds = new Label("TextBinds:", Style.labelBoldRegularW)).left();
            row();
            add(shaderSwitches = new Label("TextBinds:", Style.labelBoldRegularW)).left();
            row();
            add(viewportUI = new Label("Location: [" + Game.inst.localPlayerX + ", " + Game.inst.localPlayerY + "]", Style.labelBoldRegularW)).left();
            row();
            add(viewportGame = new Label("", Style.labelBoldRegularW)).left();
            pack();
        }

    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public MouseClickIndicator getMouseClickIndicator() {
        return mouseClickIndicator;
    }

    public Social getSocial() {
        return social;
    }

    public Bank getBank() {
        return bank;
    }

    public Duel getDuel() {
        return duel;
    }

    public Trade getTrade() {
        return trade;
    }

    public CombatModeSelector getCombatModeSelector() {
        return combatModeSelector;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Minimap getMinimap() {
        return minimap;
    }

    public Spellbook getSpellMenu() {
        return spellMenu;
    }

    public Stats getStatMenu() {
        return stats;
    }

    public Chat getChat() {
        return chat;
    }

    public Stage getStage() {
        return stage;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ActionMenu getActionMenu() {
        return actionMenu;
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 245) {
            int count = packet.readUnsignedByte();
            String[] options = new String[count];
            for (int i = 0; count > i; ++i) {
                options[i] = packet.readString();
            }
            new DialogPopup("Choose an menu", options, new DialogAction() {
                @Override
                public void action(int option) {
                    Game.outBuffer().newPacket(116);
                    Game.outBuffer().putByte(option);
                    Game.outBuffer().finishPacket();
                }
            });
            return;
        }
    }


    @Override
    public int[] opcodes() {
        return new int[]{245};
    }
}

