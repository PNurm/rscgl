package com.rscgl.ui.onscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Style;

public class CombatModeSelector extends Table {

    private String[] combatStyleStr = {"Controlled (+1 of each)", "Aggressive (+3 strength)", "Accurate   (+3 attack)", "Defensive  (+3 defense)"};
    private TextButton[] combatStyles = new TextButton[4];

    public CombatModeSelector() {

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.overFontColor = Color.BLACK;
        buttonStyle.fontColor = Color.BLACK;
        buttonStyle.up = buttonStyle.over = Backgrounds.create(Colors.BG_WHITE2, Color.BLACK, 175, 20);
        buttonStyle.checked = buttonStyle.checkedOver = Backgrounds.create(new Color(0.8f, 0, 0, 0.6f), Color.BLACK, 175, 20);
        buttonStyle.font = Fonts.Font12P;

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.setMaxCheckCount(1);

        Label title = new Label("Select combat style", Style.labelBoldBigW);
        add(title).align(Align.center).expandX().fillX();
        row();

        for(int i = 0; i < combatStyleStr.length;i++) {
            final int combatStyle = i;
            final TextButton style = new TextButton(combatStyleStr[i], buttonStyle);
            combatStyles[i] = style;
            buttonGroup.add(style);

            style.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {

                    Game.outBuffer().newPacket(29);
                    Game.outBuffer().putByte(combatStyle);
                    Game.outBuffer().finishPacket();

                    setCombatStyle(combatStyle);
                }
            });
            add(style).expandX().fillX().height(20).width(175);
            row();
        }
        pack();
        background(Backgrounds.create(Colors.BG_WHITE2, (int) getWidth(), (int) getHeight()));

        setPosition(30, Gdx.graphics.getHeight() / 2);
        setVisible(false);
        setCombatStyle(0);
    }

    public void setCombatStyle(int selected) {
       combatStyles[selected].setChecked(true);
    }
}
