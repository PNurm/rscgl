package com.rscgl.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Style;

public class DialogPopup extends Dialog {

    private DialogAction chooseAction;
    private TextField inputText;
    private DialogInputAction action;

    public DialogPopup(String title, String enterButton, final DialogInputAction action) {
        super("", Style.dialogStyle);
        this.action = action;
        Label label = new Label(title, Style.labelBoldBigW);
        label.setAlignment(Align.center);

        inputText = new TextField("", Style.textFieldStyle);
        inputText.setAlignment(Align.center);
        inputText.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(event.getKeyCode() == Input.Keys.ENTER) {
                    String text = inputText.getText();
                    action.action(text);
                    hide();
                    return true;
                }
                return super.keyDown(event, keycode);
            }
        });
        debug(Debug.none);
        text(label).padTop(5);

        getContentTable().row();
        getButtonTable().add(inputText).width(500).height(20);
        getButtonTable().row();

        button("Cancel", false, Style.textButton).pad(5);

        pack();
        background(Backgrounds.create(new Color(0, 0,0,1f), Color.WHITE, (int) getWidth(), (int) getHeight()));
        show(Game.ui().getStage());
        Game.ui().getStage().setKeyboardFocus(inputText);
    }

    public DialogPopup(String title, String[] options, DialogAction action) {
        super("", Style.dialogStyle);
        chooseAction = action;
        Label label = new Label(title, Style.labelBoldRegularW);
        text(label).pad(10);
        getButtonTable().row();
        int opt = 0;
        for(String s : options) {
            button(s, opt, Style.textButton);
            getButtonTable().row();
            opt++;
        }
        button("Cancel", -1, Style.textButton);
        pack();
        background(new Backgrounds().create(new Color(0, 0,0,0.4f), Color.WHITE, (int) getWidth(), (int) getHeight()));
        show(Game.ui().getStage());
    }

    @Override
    protected void result(Object object) {
        if(object instanceof Boolean) {
            if ((Boolean) object == true) {
                String text = inputText.getText();
                action.action(text);
                hide();
            }
        } else if(object instanceof Integer) {
            chooseAction.action((Integer) object);
            hide();
        }
        super.result(object);
    }

}
