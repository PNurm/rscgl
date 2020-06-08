package com.rscgl.ui.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.rscgl.ui.menu.option.MenuOption;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.util.Style;

public class ActionMenuGUI extends Table {

    private final TextButton.TextButtonStyle buttonStyle;

    ActionMenuGUI() {
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.checkedOverFontColor = Color.RED;
        buttonStyle.overFontColor = Color.YELLOW;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.font = Fonts.Font12B;
        setVisible(false);
    }

    public void init() {
        hide();

        Label label = new Label("[#" + Color.CYAN.toString() + "]" + "Choose option", Style.labelBoldRegularW);
        label.setAlignment(Align.center);
        add(label).fillX().padRight(5).padLeft(5);
        row();
    }

    public TextButton addMenuItem(final MenuOption item) {

        TextButton textButton = new TextButton(item.getAction() + " " + (item.getSubject() != null ? "[#" + item.getSubjectColor() + "]" + item.getSubject() : ""), buttonStyle);
        textButton.getLabel().setAlignment(Align.left);
        add(textButton).left().fillX().padRight(5).padLeft(5);
        textButton.invalidate();
        // add(text);
        row();
        textButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                item.getOptionAction().execute();
                hide();
                return true;
            }
        });
        return textButton;
    }
    public void finish() {
        pack();
        background(Backgrounds.create(Colors.BG_WHITE,(int) getWidth(), (int) getHeight()));
        clearListeners();
        addListener(new InputListener() {
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (toActor == null) {
                    //hide();
                    //System.out.println("hey");
                }
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                return super.mouseMoved(event, x, y);
            }
        });
    }

    public void hide() {
        reset();
        setSize(0,0);
        background((Drawable) null);
        pack();
        setVisible(false);
    }
}
