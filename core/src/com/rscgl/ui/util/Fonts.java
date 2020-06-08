package com.rscgl.ui.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import java.io.File;

public class Fonts {


    public static BitmapFont Font11P;
    public static BitmapFont Font12B;
    public static BitmapFont Font12P;
    public static BitmapFont Font13B;
    public static BitmapFont Font14B;
    public static BitmapFont Font16B;
    public static BitmapFont Font20B;
    public static BitmapFont Font24B;

    public static final BitmapFont Font12B_NOSHADOW;
    public static BitmapFont Font14B_NOSHADOW;

    static {
        Font11P = loadFont("f11p.fnt");
        Font12B = loadFont("f12b.fnt");
        Font12B_NOSHADOW = loadFont("f12b-noshadow.fnt");
        Font12P = loadFont("f12p.fnt");
        Font13B = loadFont("f13b.fnt");
        Font14B = loadFont("f14b.fnt");
        Font14B_NOSHADOW = loadFont("f14b-noshadow.fnt");
        Font16B = loadFont("f16b.fnt");
        Font20B = loadFont("f16b.fnt");
        Font24B = loadFont("f16b.fnt");
    }

    public static BitmapFont loadFont(String file) {
        FileHandle f = Gdx.files.internal("fonts" + File.separator + file);
        if (!f.exists()) {
            System.err.println(file + " not found at " + f.path());
            return new BitmapFont();
        }
        BitmapFont fnt = new BitmapFont(f);
        fnt.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fnt.getData().markupEnabled = true;
        return fnt;
    }
}
