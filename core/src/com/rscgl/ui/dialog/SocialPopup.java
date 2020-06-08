package com.rscgl.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.ui.util.Style;

public class SocialPopup extends Dialog {


    private final TextField inputText;
    private final DialogInputAction action;

    public SocialPopup(String title, final DialogInputAction action) {
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
        show(Game.ui().getStage());
        Game.ui().getStage().setKeyboardFocus(inputText);
    }


    @Override
    protected void result(Object object) {
        if((Boolean) object == true) {
            String text = inputText.getText();
            action.action(text);
            hide();
        }
        super.result(object);
    }
}
