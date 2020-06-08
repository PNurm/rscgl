package com.rscgl.ui.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.rscgl.Game;
import com.rscgl.net.Buffer;
import com.rscgl.net.PacketHandler;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.ColorUtil;
import com.rscgl.ui.util.Style;

public class ServerMessagePopup extends Dialog implements PacketHandler {

    private final Label text;
    private final Cell<?> textCell;

    public ServerMessagePopup() {
        super("", Style.dialogStyle);
        setWidth(400); setHeight(100);
        textCell = getContentTable().add(this.text = new Label("", Style.labelBoldRegularW));
        text.setAlignment(Align.center);
        text.setWrap(true);
        getButtonTable().row();
        button("Click here to close window", false, Style.textButton).pad(5);
        Game.inst.registerPacketHandler(this);
    }

    @Override
    protected void result(Object object) {
        hide();
        super.result(object);
    }

    @Override
    public void handlePacket(int opcode, int length, Buffer packet) {
        setWidth(400); setHeight(opcode == 222 ? 300 : 100);
        background(Backgrounds.create(new Color(0, 0, 0, 1f), Color.WHITE, (int) getWidth(), (int) getHeight()));
        textCell.width(400);//.height(opcode == 222 ? 300 : 100).width(400);
        text.setText(ColorUtil.parse(packet.readString().replaceAll("%", "\n")));
        pack();
        show(Game.ui().getStage());
    }

    @Override
    public int[] opcodes() {
        return new int[] {222, 89};
    }
}