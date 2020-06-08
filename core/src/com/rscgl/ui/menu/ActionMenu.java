package com.rscgl.ui.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.rscgl.ui.menu.option.MenuOption;
import com.rscgl.ui.menu.option.OptionPriority;

import java.util.ArrayList;
import java.util.Comparator;

public class ActionMenu {

    private final ActionMenuGUI ui;
    private ArrayList<MenuOption> menuOptions = new ArrayList<MenuOption>();

    public ActionMenu() {
        this.ui = new ActionMenuGUI();
    }

    public void clearMenu() {
        menuOptions.clear();
        ui.clear();
    }

    public void add(String str1, String str2, Color color2, final GameAction optionAction, OptionPriority priority) {
        menuOptions.add(new MenuOption(str1, str2, color2, optionAction, priority));
    }


    public void add(String str1, final GameAction optionAction, OptionPriority priority) {
        menuOptions.add(new MenuOption(str1, null, null, optionAction, priority));
    }

    public void sort() {
        menuOptions.sort(new Comparator<MenuOption>() {
            @Override
            public int compare(MenuOption o1, MenuOption o2) {
                return o1.getPriority().priority() - o2.getPriority().priority();
            }
        });
    }
    public void show() {
        ui.clear();
        ui.init();

        for(MenuOption item : menuOptions) {
            ui.addMenuItem(item);
        }
        ui.finish();
        ui.setVisible(true);
        ui.toFront();
    }

    public MenuOption getFirst() {
        if(menuOptions.size() == 0)
            return null;
        return menuOptions.get(0);
    }

    public int getSize() {
        return menuOptions.size();
    }

    public ActionMenuGUI getUI() {
        return ui;
    }

    public void add(MenuOption item) {
        menuOptions.add(item);
    }

    Vector2 v = new Vector2();

    public void executeFirstAction() {
        try {
            if (getUI().isVisible()) {
                return;
            }
            if (getFirst() != null) {
                getFirst().getOptionAction().execute();
                return;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void showActionMenu(int screenX, int screenY) {
        show();
        Vector2 showAt = getUI().getStage().screenToStageCoordinates(v.set(screenX, screenY));
        getUI().setPosition(showAt.x - (getUI().getWidth() / 2), showAt.y - getUI().getHeight() + 10);
    }

    public void checkBounds(int screenX, int screenY) {
        if (getUI().isVisible()) {
            v = getUI().screenToLocalCoordinates(v.set(screenX, screenY));
            Actor a = getUI().hit(v.x, v.y, false);
            if (a == null) {
                getUI().hide();
            }
        }
    }
}
