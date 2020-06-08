package com.rscgl.ui.menu;

import com.rscgl.Game;
import com.rscgl.UI;
import com.rscgl.net.Connection;
import com.rscgl.GameWorld;

public abstract class GameAction {

    protected static final GameWorld world = Game.world();
    protected static final Connection net = Game.net();
    protected static final UI ui = Game.ui();

    public abstract void execute();


}
