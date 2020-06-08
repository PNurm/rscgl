package com.rscgl.ui.onscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.assets.Assets;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.util.Style;
import com.rscgl.util.MessageType;
import com.rscgl.ui.util.ColorUtil;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.util.StringEncryption;

import java.util.ArrayList;

public class Chat extends Table implements PacketHandler {
    private final Table chatTabChat;
    private final Table buttonsTable;
    private final ButtonGroup buttonGroup;

    private Table chatTabAll;
    private final Table chatDialogue;
    private final Table chatPrivate;


    private ScrollPane chatScrollPane;
    private TextField chatInput;
    private Label userLabel;
    private int globalChannel = 1;

    private int chatHistoryCursor = 0;
    private ArrayList<String> chatHistory = new ArrayList<String>();

    public Chat() {
        Game.inst.registerPacketHandler(this);

        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.focusedFontColor = Color.WHITE;
        style.font = Fonts.Font14B;
        style.fontColor = Color.WHITE;
        style.cursor = Backgrounds.create(Color.WHITE, 1, 1);
//        style.background = Backgrounds.create(new Color(0.4f, 0.4f, 0.4f, 0.4f), 128, 128);


        chatInput = new TextField("", style);
        chatInput.setMessageText("<Click to enter a message>");
        chatInput.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {

                return super.keyUp(event, keycode);
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                String text = chatInput.getText();
                if(event.getKeyCode() == Input.Keys.PAGE_DOWN) {
                    chatHistoryCursor++;
                    if (chatHistoryCursor >= chatHistory.size()) {
                        chatHistoryCursor = chatHistory.size() - 1;
                        return true;
                    }
                    chatInput.setText(chatHistory.get(chatHistoryCursor));
                    chatInput.setCursorPosition(chatInput.getText().length());
                    return true;
                } else if(event.getKeyCode() == Input.Keys.PAGE_UP) {
                    chatHistoryCursor--;
                    if (chatHistoryCursor < 0) {
                        chatHistoryCursor = 0;
                        return true;
                    }
                    chatInput.setText(chatHistory.get(chatHistoryCursor));
                    chatInput.setCursorPosition(chatInput.getText().length());
                    return true;
                }
                if (event.getKeyCode() == Input.Keys.ENTER && text.length() > 0) {
                    if (chatHistory.size() == 0 || !chatHistory.get(chatHistory.size() - 1).equalsIgnoreCase(text)) {
                        chatHistory.add(text);
                        chatHistoryCursor = chatHistory.size();
                    } else {
                        chatHistoryCursor = chatHistory.size();
                    }
                    if (text.startsWith("::")) {
                        sendCommand(text.substring(2));
                    } else {
                        if (buttonGroup.getCheckedIndex() == 3) {
                            sendCommand(globalChannel + " " + text);
                        } else {
                            sendChat(text);
                        }
                    }
                    chatInput.setText("");
                    getStage().setKeyboardFocus(null);
                    return true;
                }
                return super.keyDown(event, keycode);
            }
        });

        chatTabAll = new Table();
        chatTabChat = new Table();
        chatDialogue = new Table();
        chatPrivate = new Table();

        buttonGroup = new ButtonGroup();
        buttonGroup.setMaxCheckCount(1);


        buttonsTable = new Table();
        buttonsTable.add(createButton(chatTabAll, "All Messages")).width(120).height(10);
        buttonsTable.pad(5);
        buttonsTable.add(createButton(chatTabChat, "Chat history")).width(120).height(10);
        buttonsTable.pad(5);
        buttonsTable.add(createButton(chatDialogue, "Quest history")).width(120).height(10);
        buttonsTable.pad(5);
        buttonsTable.add(createButton(chatPrivate, "Private history")).width(120).height(10);
        buttonsTable.pack();

        Table table = new Table();
        table.center().add(buttonsTable);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScroll = Backgrounds.drawVerticalGradient(12, 80, Color.valueOf("#0e0e3e"), Color.valueOf("#7272b0"));
        scrollStyle.vScrollKnob = Backgrounds.create(Color.valueOf("#6081b8"), 11, 11);

        chatScrollPane = new ScrollPane(chatTabAll, scrollStyle);
        chatScrollPane.setScrollBarTouch(false);
        chatScrollPane.setSmoothScrolling(false);
        chatScrollPane.setScrollbarsOnTop(false);
        chatScrollPane.setFlickScroll(false);
        chatScrollPane.setOverscroll(false, false);
        chatScrollPane.setFadeScrollBars(false);
        chatScrollPane.setForceScroll(false, true);

        padLeft(8).add(chatScrollPane).expand().fill().size(Gdx.graphics.getWidth() - 8, 80);
        row().height(10);
        padLeft(5).add(chatInput).expandX().fillX();
        row().space(5);
        add(table).expandX().fillX();
        pack();

        align(Align.center);
    }

    public void sendCommand(String str) {
        Game.outBuffer().newPacket(38);
        Game.outBuffer().putString(str);
        Game.outBuffer().finishPacket();
    }

    public void sendChat(String str) {
        Game.outBuffer().newPacket(216);
        StringEncryption.putEncryptedString(Game.outBuffer(), str);
        Game.outBuffer().finishPacket();
    }

    public void addMessage(String string) {
        string = ColorUtil.parse(string);
        Label label = new Label(string, Style.labelBoldRegularW);
        label.setWrap(true);
        chatTabAll.row();
        chatTabAll.add(label).expandX().fillX();
        chatTabAll.invalidate();

        chatScrollPane.layout();
        chatScrollPane.setScrollY(chatScrollPane.getMaxY());
    }


    private TextButton createButton(final Table t, String text) {
        final TextButton btn = new TextButton(text, Style.smallTextButton);
        btn.align(Align.center);
        buttonGroup.add(btn);
        btn.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                btn.setChecked(true);
                chatScrollPane.setActor(t);
                chatScrollPane.layout();

                chatScrollPane.setSmoothScrolling(false);
                chatScrollPane.setFlickScroll(false);
                chatScrollPane.setScrollY(chatScrollPane.getMaxY());
                chatScrollPane.act(Gdx.graphics.getDeltaTime());
                chatScrollPane.setSmoothScrolling(true);
                chatScrollPane.setFlickScroll(true);

                return super.touchDown(event, x, y, pointer, button);
            }
        });
        return btn;
    }

    public void addMessage(String message, MessageType type) {
        message = ColorUtil.parse(type.color + message);

        Label label = new Label(message, Style.labelBoldRegularW);
        label.setWrap(true);
        label.invalidate();

        Table t = getTabForMesageType(type);
        t.row();
        t.add(label).expandX().fillX();
        t.invalidate();
        if (t != chatTabAll) {
            label = new Label(message, Style.labelBoldBigW);
            label.setWrap(true);
            label.invalidate();
            chatTabAll.row();
            chatTabAll.add(label).expandX().fillX();
            chatTabAll.invalidate();
        }
        chatScrollPane.layout();

        if(chatScrollPane.getMaxY() - chatScrollPane.getScrollY() <= 20*4) {
            chatScrollPane.setScrollY(chatScrollPane.getMaxY());
        }
    }

    public Table getTabForMesageType(MessageType t) {
        if (t == MessageType.CHAT) {
            return chatTabChat;
        }
        if (t == MessageType.QUEST) {
            return chatDialogue;
        }

        if (t == MessageType.PRIVATE_RECIEVE
                || t == MessageType.PRIVATE_SEND
                || t == MessageType.FRIEND_STATUS) {
            return chatPrivate;
        }
        return chatTabAll;
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 120) {
            String sender = packet.readString();
            String formerName = packet.readString();
            int icon = packet.readUnsignedByte();
            String message = StringEncryption.getEncryptedString(packet);
            addMessage(sender + " tells you: " + message, MessageType.PRIVATE_RECIEVE);
        } else if (opcode == 87) {
            String var13 = packet.readString();
            String var14 = StringEncryption.getEncryptedString(packet);
            addMessage("You tell " + var13 + ": " + var14, MessageType.PRIVATE_SEND);
        }
        if (opcode == 131) {
            int crown = packet.readUnsignedByte();
            MessageType type = MessageType.lookup(packet.readUnsignedByte());
            int var5 = packet.readUnsignedByte();
            String message = packet.readString();
            String sender = null;
            String clan = null;
            String color = null;
            if ((var5 & 1) != 0) {
                sender = packet.readString();
            }

            if ((1 & var5) != 0) {
                clan = packet.readString();
            }

            if ((var5 & 2) != 0) {
                color = packet.readString();
            }
            addMessage(message, type);
        }
    }

    @Override
    public int[] opcodes() {
        return new int[]{120, 87, 131};
    }

    public TextField getChatInput() {
        return chatInput;
    }
}
