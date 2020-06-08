package com.rscgl.ui.onscreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rscgl.Config;
import com.rscgl.Game;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Colors;
import com.rscgl.ui.util.Style;

public class Settings extends Table {

    private Label devMode;
    private Label moderatorMode;
    private final Slider.SliderStyle slider;
    private Label fogDistLabel;
    private Label invisMode;
    private Label fogColour;

    public Settings() {
        slider = new Slider.SliderStyle();
        slider.knobDown = Backgrounds.create(Color.RED, 6, 6);
        slider.knob = Backgrounds.create(Color.DARK_GRAY, 6, 6);
        slider.knobOver = Backgrounds.create(Color.GRAY, 6, 6);
        slider.background = Backgrounds.create(Color.valueOf("#0e1111bb"), 12, 12);


        TextButton button = new TextButton("Log out", Style.textButton);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                Game.outBuffer().newPacket(102);
                Game.outBuffer().putInt(0);
                Game.outBuffer().putInt(0);
                Game.outBuffer().finishPacket();

            }
        });

        Table settings = getSettingsTable();

        add(settings).width(200).padTop(5);
        row();
        add(button).padTop(35).padBottom(15);
        row();
        pack();
        background(Backgrounds.create(Colors.BG_WHITE, new Color(0,0,0,1),(int) getWidth(), (int) getHeight()));

        setVisible(false);
    }


    private Table getSettingsTable() {
        Table settings = new Table();
        settings.add(new Label("Settings: ", Style.labelBoldBigW)).pad(5).row();
        settings.add(fogDistLabel = new Label("Fog distance: ", Style.labelBoldRegularW)).row();
        final Slider fogDistance = new Slider(25, 512, 0.5F, false, slider);
        fogDistance.setAnimateDuration(0);
        fogDistance.setValue(Game.cam().far);
        fogDistance.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                Game.cam().far = fogDistance.getValue();
                fogDistLabel.setText("Fog distance: " + Game.cam().far);
            }
        });
        fogDistance.pack();
        settings.add(fogDistance).row();

        settings.add(fogColour = new Label("Fog color: ", Style.labelBoldRegularW)).row();
        settings.add(new Label("Red: ", Style.labelBoldRegularW)).row();
        final Slider fogColorR = new Slider(0, 1, 0.01F, false, slider);
        fogColorR.setAnimateDuration(0);
        fogColorR.setValue(Config.FOG_COLOR.r);
        fogColorR.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                Config.FOG_COLOR.r = fogColorR.getValue();
                fogColour.setText("Fog color: " + Config.FOG_COLOR);
            }
        });
        settings.add(fogColorR).row();
        fogColorR.pack();

        settings.add(new Label("Green: ", Style.labelBoldRegularW)).row();
        final Slider fogColorG = new Slider(0, 1, 0.01F, false, slider);
        fogColorG.setAnimateDuration(0);
        fogColorG.setValue(Config.FOG_COLOR.g);
        fogColorG.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                Config.FOG_COLOR.g = fogColorG.getValue();
                fogColour.setText("Fog color: " + Config.FOG_COLOR);
            }
        });
        settings.add(fogColorG).row();
        fogColorG.pack();

        settings.add(new Label("Blue: ", Style.labelBoldRegularW)).row();
        final Slider fogColorB = new Slider(0, 1, 0.01F, false, slider);
        fogColorB.setAnimateDuration(0);
        fogColorB.setValue(Config.FOG_COLOR.g);
        fogColorB.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                Config.FOG_COLOR.b = fogColorB.getValue();
                fogColour.setText("Fog color: " + Config.FOG_COLOR);
            }
        });
        settings.add(fogColorB).row();
        fogColorB.pack();
        return settings;
    }
}
