package com.rscgl.ui.onscreen;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.rscgl.assets.Assets;

public class TopMenu extends Table {

    public final int MENU = 0;
    public final int MENU_INVENTORY = 1;
    public final int MENU_MAP = 2;
    public final int MENU_STATS = 3;
    public final int MENU_SPELLS = 4;
    public final int MENU_FRIENDS = 5;
    public final int MENU_WRENCH = 6;

    public Image[] icons = new Image[]{
            new Image(Assets.inst.getInterSprite(MENU)),
            new Image(Assets.inst.getInterSprite(MENU_INVENTORY)),
            new Image(Assets.inst.getInterSprite(MENU_MAP)),
            new Image(Assets.inst.getInterSprite(MENU_STATS)),
            new Image(Assets.inst.getInterSprite(MENU_SPELLS)),
            new Image(Assets.inst.getInterSprite(MENU_FRIENDS)),
            new Image(Assets.inst.getInterSprite(MENU_WRENCH))};

    private Actor[] menus;

    public TopMenu(Actor... actors) {
        stack(icons).width(197);
        menus = actors;
        for (int x = 1; x < icons.length; x++) {
            icons[x].setScaling(Scaling.none);
            icons[x].setVisible(false);
        }
        pack();
        icons[1].setPosition(-24, 0);
    }

    private int menuVisible = -1;

    public InputListener inputAdapter = new InputListener() {
        Vector2 v = new Vector2();

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if(mouseMoved(event, x, y)) {
                return true;
            }
            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public boolean mouseMoved(InputEvent event, float x, float y) {
            boolean hitS = false;
            menuVisible = -1;

            for (int i = 0; i < menus.length; i++) {
                Actor menu = menus[i];
                boolean hit = hit(menu, v.set(x, y));
                if(i == MENU_MAP - 1) {
                    hit = ((Minimap) menu).hit2(x,y);
                }
                v = stageToLocalCoordinates(v.set(x, y));
                if (!hit && v.x >= 0 && v.y >= 0 && v.x <= getWidth() && v.y <= getHeight()) {

                    float minX = getWidth() - (i + 1) * 32F;
                    float maxX = minX + 32F;
                    if(i == 0) {
                        /* map and inv are bigger.. */
                        minX = getWidth() - (i + 1) * 34F;
                        maxX = minX + 34F;
                    }
                    if (v.x > minX && v.x < maxX) {
                        hit = true;
                        menuVisible = i;
                    }
                }
                icons[2].setPosition(21, 0);
                icons[i + 1].setVisible(hit);
                v = TopMenu.this.localToStageCoordinates(new Vector2());
                show(menu, v, hit);
                if(hit) {
                    hitS = true;
                }
            }
            return hitS;
        }

        void show(Actor actor, Vector2 v, boolean b) {
            actor.setPosition(v.x + (getWidth() - actor.getWidth()), v.y - actor.getHeight());
            actor.setVisible(b);
        }

        boolean hit(Actor r, Vector2 v) {
            Vector2 v2 = r.stageToLocalCoordinates(v);
            return r.hit(v2.x, v2.y, false) != null && r.isVisible();
        }
    };

}
