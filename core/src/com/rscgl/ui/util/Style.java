package com.rscgl.ui.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class Style {


    public static final Label.LabelStyle labelBoldRegularW = new Label.LabelStyle();
    public static final Label.LabelStyle labelBoldRegularB = new Label.LabelStyle();

    public static final Label.LabelStyle labelBoldBigW = new Label.LabelStyle();
    public static final Label.LabelStyle labelBoldBigB = new Label.LabelStyle();

    public static final Label.LabelStyle labelBigW = new Label.LabelStyle();
    public static final Label.LabelStyle labelBigB = new Label.LabelStyle();

    public static final ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();

    public static final TextButton.TextButtonStyle textButton = new TextButton.TextButtonStyle();
    public static final TextButton.TextButtonStyle smallTextButton = new TextButton.TextButtonStyle();
    public static final TextButton.TextButtonStyle bigButtonWhite = new TextButton.TextButtonStyle();

    public static final Window.WindowStyle dialogStyle = new Window.WindowStyle();

    public static final TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();

    public static final Label.LabelStyle labelSmallW = new Label.LabelStyle();

    static {

        labelSmallW.font = Fonts.Font11P;
        labelSmallW.fontColor = Color.WHITE;

        labelBoldRegularW.font = Fonts.Font12B;
        labelBoldRegularW.fontColor = Color.WHITE;

        labelBoldRegularB.font = Fonts.Font12B;
        labelBoldRegularB.fontColor = Color.BLACK;


        labelBigW.font = Fonts.Font12B_NOSHADOW;
        labelBigW.fontColor = Color.WHITE;

        labelBigB.font = Fonts.Font12B_NOSHADOW;
        labelBigB.fontColor = Color.BLACK;


        labelBoldBigW.font = Fonts.Font14B;
        labelBoldBigW.fontColor = Color.WHITE;

        labelBoldBigB.font = Fonts.Font12P;
        labelBoldBigB.fontColor = Color.BLACK;

        scrollStyle.vScroll = Backgrounds.create(new Color(0.3f, 0.3f, 0.3f, 0.5f), 12, 12);//new TextureRegionDrawable(new TextureRegion(Assets.inst.get("interfaceutil", 0)));
        scrollStyle.vScrollKnob = Backgrounds.create(new Color(0, 0, 0, 0.5f), 12, 12);
        scrollStyle.background = Backgrounds.create(new Color(0.2f, 0.2f, 0.2f, 0.2f), 12, 12);

        textButton.overFontColor = Color.RED;
        textButton.checkedFontColor = Color.YELLOW;
        textButton.fontColor = Color.WHITE;
        textButton.font = Fonts.Font12B;

        smallTextButton.overFontColor = Color.RED;
        smallTextButton.checkedFontColor = Color.YELLOW;
        smallTextButton.fontColor = Color.WHITE;
        smallTextButton.font = Fonts.Font11P;

        bigButtonWhite.overFontColor = Color.RED;
        bigButtonWhite.fontColor = Color.BLACK;
        bigButtonWhite.font = Fonts.Font14B;
        bigButtonWhite.up = bigButtonWhite.over = Backgrounds.create(Colors.BG_WHITE2, Color.BLACK, 98, 24);
        bigButtonWhite.checked = bigButtonWhite.checkedOver = Backgrounds.create(Colors.BG_WHITE, Color.BLACK, 98, 24);


        dialogStyle.titleFont = Fonts.Font12B;
        dialogStyle.background = new Backgrounds().create(new Color(0, 0, 0, 1f), 64, 64);
        dialogStyle.titleFontColor = Color.WHITE;


        textFieldStyle.focusedFontColor = Color.WHITE;
        textFieldStyle.font  = Fonts.Font14B;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.cursor = Backgrounds.create(Color.WHITE, 1, 1);
    }
}
