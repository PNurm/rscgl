package com.rscgl.ui.menu.option;

import com.badlogic.gdx.graphics.Color;
import com.rscgl.Game;
import com.rscgl.ui.menu.GameAction;
import com.rscgl.ui.util.ColorUtil;
import com.rscgl.model.entity.PlayerEntity;

public class PlayerOption extends MenuOption {

    public PlayerOption(String option, PlayerEntity player, GameAction action, OptionPriority priority) {
        super(option, player.username + ColorUtil.getLevelColor(Game.inst.localPlayer.combatLevel, player.combatLevel), Color.WHITE, action, priority);

    }

}
