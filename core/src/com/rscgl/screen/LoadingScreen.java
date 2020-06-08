package com.rscgl.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.rscgl.Config;
import com.rscgl.Game;
import com.rscgl.assets.Assets;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Style;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class LoadingScreen implements Screen {

    private Stage stage;
    private Label statusText;
    private LoadingBar progressBar;
    private String statusTextStr;
    private boolean cacheUpdated;

    public LoadingScreen() {
        show();
    }

    @Override
    public void show() {
        stage = new Stage();
        stage.getViewport().setWorldSize(Config.RESOLUTION_WIDTH, Config.RESOLUTION_HEIGHT);
        stage.getViewport().apply(true);

        //public Drawable knobBefore, knobAfter, disabledKnobBefore, disabledKnobAfter;
        Table loading = new Table();
        loading.setFillParent(true);
        loading.add(statusText = new Label("Status: ", Style.labelBoldRegularW));
        loading.row();
        loading.add(progressBar = new LoadingBar());
        loading.pack();

        stage.addActor(loading);
        new Thread() {
            public void run() {
                Assets.init();
                Assets.inst.loadRSCache();
                cacheUpdated = true;
            }
        }.start();
    }

    class LoadingBar extends Actor {
        private Texture background = Backgrounds.createSolidBackgroundT(new Color(Color.GRAY), 250, 20);
        private Texture fill =  Backgrounds.createSolidBackgroundT(new Color(Color.SCARLET), 15, 15);
        private int value;

        public LoadingBar() {
            setSize(background.getWidth(), background.getHeight());
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            int percentage = value * background.getWidth() / 100;
            batch.draw(background, getX(), getY(), background.getWidth(), background.getHeight());
            batch.draw(fill, getX(), getY() + 1 , percentage, background.getHeight() - 2);

        }
        public void setValue(int i) {
            this.value = i;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0,0,0,0);

        stage.act();
        stage.draw();

        statusText.setText(statusTextStr);

        if(cacheUpdated) {
            Game.inst.init();
            Game.inst.initLoginScreen();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }


}
