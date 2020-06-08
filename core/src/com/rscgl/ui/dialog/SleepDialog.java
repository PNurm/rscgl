package com.rscgl.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Style;

public class SleepDialog extends Dialog implements PacketHandler {

    private final Label fatiguePercent;
    private final Image captchaImage;
    private final Label statusText;
    private DialogAction chooseAction;
    private TextField inputText;
    private DialogInputAction action;

    public SleepDialog() {
        super("", Style.dialogStyle);
        Game.inst.registerPacketHandler(this);
        inputText = new TextField("", Style.textFieldStyle);
        inputText.setAlignment(Align.center);
        inputText.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(event.getKeyCode() == Input.Keys.ENTER) {

                    Game.outBuffer().newPacket(45);
                    Game.outBuffer().putByte(0);
                    Game.outBuffer().putString(inputText.getText());
                    Game.outBuffer().finishPacket();
                    return true;
                }
                return super.keyDown(event, keycode);
            }
        });
        debug(Debug.none);

        getContentTable().add(new Label("You are sleeping", Style.labelBoldBigW)).padTop(5).row();
        getContentTable().add(fatiguePercent = new Label("Fatigue: 0%", Style.labelBoldBigW)).padTop(5).row();

        Label lbl = new Label("When you want to wake up just use your \n keyboard to type the word in the box below", Style.labelBoldRegularW);
        lbl.setWrap(true);
        getContentTable().add(lbl).left().padTop(5).row();

        getContentTable().row();
        getContentTable().add(inputText).width(200).height(20).padBottom(25).row();
        getContentTable().add(captchaImage = new Image()).center().row();
        getContentTable().add(statusText = new Label("", Style.labelBoldRegularW));

        TextButton button = new TextButton("Click here for a new word", Style.textButton);
        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                Game.outBuffer().newPacket(45);
                Game.outBuffer().putByte(0);
                Game.outBuffer().putString("-null-");
                Game.outBuffer().finishPacket();
                return true;
            }
        });
        getButtonTable().add(button).padBottom(25);

        background(Backgrounds.create(new Color(0, 0,0,1f), Color.WHITE, 350, 250));
        pack();
    }

    @Override
    protected void result(Object object) {
        if(object instanceof Boolean) {
            if ((Boolean) object == false) {

                Game.outBuffer().newPacket(45);
                Game.outBuffer().putByte(0);
                Game.outBuffer().putString("-null-");
                Game.outBuffer().finishPacket();

                //new word
            }
        }
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        if (opcode == 244) {
            int fatigue = packet.readShort();
            fatiguePercent.setText("Fatigue: " + (fatigue* 100 / 750) + "%");
            return;
        }

        if (opcode == 117) {
            if (!isVisible()) {
                fatiguePercent.setText("Fatigue: " + Game.ui().getStatMenu().getSkills().getFatigue()* 100 / 750 + "%");
            }
            Pixmap pixmap = new Pixmap(packet.dataBuffer, 0, length);
            captchaImage.setDrawable(new TextureRegionDrawable(new Texture(pixmap)));
            statusText.setText("");
            inputText.setText("");
            show(Game.ui().getStage());
            Game.ui().getStage().setKeyboardFocus(inputText);
            return;
        }

        if (opcode == 84) {
            hide();
            return;
        }

        if (opcode == 194) {
            statusText.setText("Incorrect - Please wait...");
            inputText.setText("");
            return;
        }
    }

    @Override
    public int[] opcodes() {
        return new int[]{117, 84, 194, 244};
    }
}
