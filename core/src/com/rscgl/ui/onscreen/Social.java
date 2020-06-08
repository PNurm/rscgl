package com.rscgl.ui.onscreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.dialog.SocialPopup;
import com.rscgl.ui.dialog.DialogInputAction;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Style;
import com.rscgl.util.MessageType;
import com.rscgl.util.StringEncryption;
import com.rscgl.util.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;

public class Social extends Table implements PacketHandler {

    private final TextButton addListing;
    private ArrayList<Friend> friends = new ArrayList<Friend>();
    private ArrayList<String> ignoreList = new ArrayList<String>();

    private final ScrollPane scrollPane;
    private final Table ignoreListTable;
    private final Table friendListTable;

    public Social() {
        background(Backgrounds.create(Colors.BG_WHITE2, 10, 10));

        Game.inst.registerPacketHandler(this);

        friendListTable = new Table();
        ignoreListTable = new Table();

        scrollPane = new ScrollPane(friendListTable, Style.scrollStyle);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setFlickScroll(true);
        scrollPane.setOverscroll(false, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setForceScroll(false, true);



        final TextButton spellButton = new TextButton("Friends", Style.bigButtonWhite);
        spellButton.align(Align.center);
        spellButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                scrollPane.setActor(friendListTable);
                addListing.setText("Click here to add friend");
                scrollPane.invalidate();
                spellButton.setChecked(true);
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        final TextButton prayersButton = new TextButton("Ignore", Style.bigButtonWhite);
        prayersButton.align(Align.center);
        prayersButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                scrollPane.setActor(ignoreListTable);
                addListing.setText("Click here to add ignore");
                scrollPane.invalidate();
                prayersButton.setChecked(true);
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.add(spellButton, prayersButton);

        Table btn = new Table();
        btn.add(spellButton).width(98).height(24);
        btn.add(prayersButton).width(98).height(24);
        add(btn);
        row();
        add(scrollPane).expand().fill().width(196).height(150);// well if ur certain it will work ok
        row();
        addListing = new TextButton("Click here to add friend", Style.textButton);
        addListing.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                final String listType = (scrollPane.getActor() == friendListTable ? "Friend list" : "Ignore list");
                new SocialPopup("Enter a name to add to " + listType, new DialogInputAction() {
                    @Override
                    public void action(String result) {
                        if (scrollPane.getActor() == friendListTable) {
                            if (ignoreList.contains(result)) {
                                Game.ui().getChat().addMessage("Please remove " + result + " from your ignore list first");
                                return;
                            }
                            if (friends.contains(result)) {
                                Game.ui().getChat().addMessage(result + " is already on your friend list");
                                return;
                            }
                            if (friends.contains(result)) {
                                Game.ui().getChat().addMessage("You can't add yourself to your own " + listType);
                                return;
                            }
                            Game.outBuffer().newPacket(195);
                            Game.outBuffer().putString(result);
                            Game.outBuffer().finishPacket();

                        } else if (scrollPane.getActor() == ignoreListTable) {
                            if (friends.contains(result)) {
                                Game.ui().getChat().addMessage("Please remove " + result + " from your ignore list first");
                                return;
                            }
                            if (ignoreList.contains(result)) {
                                Game.ui().getChat().addMessage(result + " is already on your friend list");
                                return;
                            }
                            if (ignoreList.contains(result)) {
                                Game.ui().getChat().addMessage("You can't add yourself to your own " + listType);
                                return;
                            }
                            Game.outBuffer().newPacket(132);
                            Game.outBuffer().putString(result);
                            Game.outBuffer().finishPacket();
                        }
                    }
                });
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        add(addListing);
        row();
        pack();
        setVisible(false);
    }

    public void rebuildFriends() {
        friendListTable.clear();
        friends.sort(new Comparator<Friend>() {
            @Override
            public int compare(Friend o1, Friend o2) {
                int o = o1.isOnline() ? 1 : 0;
                int o3 = o2.isOnline() ? 1 : 0;
                return o3 - o;
            }
        });

        for (final Friend f : friends) {
            String col = Color.RED.toString();
            if (f.isOnline()) {
                col = Color.GREEN.toString();
            }
            Label friend = new Label("[#" + col + "]" + f.getUsername() + "", Style.labelBoldRegularW);
            Label remove = new Label("Remove", Style.labelBoldRegularW);
            friend.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (f.isOnline()) {
                        new SocialPopup("Enter message to send to " + f.getUsername() + "", new DialogInputAction() {
                            @Override
                            public void action(String result) {

                                Game.outBuffer().newPacket(218);
                                Game.outBuffer().putString(f.getUsername());
                                StringEncryption.putEncryptedString(Game.outBuffer(), result);
                                Game.outBuffer().finishPacket();
                            }
                        });
                    }
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            remove.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    friends.remove(f);

                    Game.outBuffer().newPacket(167);
                    Game.outBuffer().putString(f.getUsername());
                    Game.outBuffer().finishPacket();

                    rebuildFriends();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            friendListTable.add(friend).left().width(100);
            friendListTable.add(remove).right();
            friendListTable.row();
        }
        friendListTable.pack();
        scrollPane.layout();
    }

    public ArrayList<Friend> getFriends() {
        return friends;
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 149) {
            String username = StringUtil.hashToUsername(packet.readLong());
            int onlineStatus = packet.readByte();
            Friend friend = new Friend(onlineStatus != 0, username);
            int index = friends.indexOf(friend);
            if(index >= 0) {
                Friend f = friends.get(index);
                if(f.isOnline() && onlineStatus == 0) {
                    Game.ui().getChat().addMessage(f.getUsername() + " has logged out", MessageType.FRIEND_STATUS);
                } else if(!f.isOnline() && onlineStatus != 0) {
                    Game.ui().getChat().addMessage(f.getUsername() + " has logged in", MessageType.FRIEND_STATUS);
                }
                f.setOnline(onlineStatus != 0);
            } else {
                friends.add(friend);
            }
            rebuildFriends();
        } else if (opcode == 109) {
            ignoreList.clear();
            int ignoreCount = packet.readUnsignedByte();
            for (int var4 = 0; var4 < ignoreCount; ++var4) {
                ignoreList.add(StringUtil.hashToUsername(packet.readLong()));
            }
        } else if (opcode == 110) {
            friends.clear();
            int friendCount = packet.readUnsignedByte();
            for (int friend = 0; friend < friendCount; ++friend) {
                String friendName = StringUtil.hashToUsername(packet.readLong());
                boolean online = packet.readByte() != 0;
                friends.add(new Friend(online, friendName));
            }
            rebuildFriends();
        }
    }

    @Override
    public int[] opcodes() {
        return new int[]{149, 109, 110};
    }
}