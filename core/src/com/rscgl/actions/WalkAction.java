package com.rscgl.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.rscgl.Game;
import com.rscgl.ui.menu.GameAction;
import com.rscgl.model.Path;

import static com.rscgl.actions.WalkAction.Type.WALK_TO_ENTITY;
import static com.rscgl.actions.WalkAction.Type.WALK_TO_POINT;

public abstract class WalkAction
        extends GameAction {

    protected int startX;
    protected int startZ;

    protected int minX;
    protected int maxX;
    protected int minY;
    protected int maxY;

    protected boolean reachBorder;

    protected WalkAction.Type type = WALK_TO_POINT;


    public WalkAction(WalkAction.Type type, int minX, int minY, int maxX, int maxY, boolean reachBorder) {
        this.type = type;
        this.startX = Game.inst.localPlayerX;
        this.startZ = Game.inst.localPlayerY;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.reachBorder = reachBorder;
    }

    public WalkAction(WalkAction.Type type, int destX, int destY, boolean reachBorder) {
        this(type, destX, destY, destX, destY, reachBorder);
    }

    public abstract void action();

    private void sendPath(Path path) {
        Game.outBuffer().newPacket(type == WALK_TO_POINT ? 187 : 16);
        Game.outBuffer().putShort(path.getLastStepX());
        Game.outBuffer().putShort(path.getLastStepY());
        if (type == WALK_TO_ENTITY && path.getWaypoints().length == 1 && (startX ) % 5 == 0) {
            Game.outBuffer().finishPacket();
            return;
        }
        for (Path.Waypoint waypoint : path.getWaypoints()) {
            Game.outBuffer().putByte(waypoint.x);
            Game.outBuffer().putByte(waypoint.y);
        }
        Game.outBuffer().finishPacket();
    }

    @Override
    public void execute() {
        Path path = Path.generate(Game.inst.localPlayerX, Game.inst.localPlayerY, minX, maxX, minY, maxY, reachBorder);
        if (path == null) {
            /**
             * Not sure what this is for yet..
             */
            if(type == WALK_TO_POINT) {
                action();
                return;
            }

            path = new Path(startX, startZ, new Path.Waypoint[]{new Path.Waypoint(minX, minY)});
        }

        sendPath(path);
        setMouseClickIndicator();

        action();
    }


    protected enum Type {
        WALK_TO_POINT,
        WALK_TO_ENTITY
    }

    private void setMouseClickIndicator() {
        Vector2 v = ui.getStage().screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        ui.getMouseClickIndicator().set(v.x, v.y, type == WALK_TO_POINT ? 24 : -24);
    }

}
