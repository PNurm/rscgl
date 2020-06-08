package com.rscgl.ui.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Backgrounds {

    public static Drawable create(Color color, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.setColor(color);
        labelColor.fill();
        return new Image(new Texture(labelColor)).getDrawable();
    }

    public static Drawable create(Color color, Color edge, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.setColor(color);
        labelColor.fill();
        labelColor.setColor(edge);
        labelColor.drawRectangle(0, 0, w, h);
        return new Image(new Texture(labelColor)).getDrawable();
    }

    public static Texture createSolidBackgroundT(Color color, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.setColor(color);
        labelColor.fill();
        return new Texture(labelColor);
    }

    public static Texture createSolidBackgroundT(Color color, Color edge, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.setColor(color);
        labelColor.fill();
        labelColor.setColor(edge);
        labelColor.drawRectangle(0, 0, w, h);
        return new Texture(labelColor);
    }

    public static final Drawable drawVerticalGradient(int width, int height, Color top, Color bottom) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int curY = 0; height > curY; curY++) {
            float r = (bottom.r * (height - curY) + top.r * curY) / height;
            float g = ((top.g * curY + bottom.g * (height - curY)) / height);
            float b = (curY * top.b + bottom.b * (height - curY)) / height;

            Color c = new Color(r, g, b, 1);
            pix.setColor(c);
            for (int xi = -width; xi < 0; ++xi) {
                pix.drawPixel(-xi, curY);
            }
        }


        pix.setColor(buildColor(135, 146, 179));
        drawLineVert(pix,0, 0,  height);//this.colorI,
        drawLineVert(pix,1, 1,  height - 2);//this.colorI,
        drawLineHoriz(pix, 0, 0, width);//, this.colorI
        drawLineHoriz(pix,1,  1, width - 2);//, this.colorI

        pix.setColor(buildColor(97, 112, 151));
        drawLineVert(pix,2, 2,  height - 4);//this.colorJ,
        drawLineHoriz(pix,2, 2 , width - 4);//, this.colorJ

        pix.setColor(buildColor(84, 93, 120));
        drawLineHoriz(pix,0, (height - 1), width);//, this.colorL
        drawLineHoriz(pix,1, (height - 2), width - 2);//, this.colorL
        drawLineVert(pix,width - 1, 0, height);//, this.colorL
        drawLineVert(pix,width - 2, 1, height - 2);//, this.colorL

        pix.setColor(buildColor(88, 102, 136));
        drawLineHoriz(pix,2,height - 3, width - 4);//, this.colorK
        drawLineVert(pix,(width - 3), 2, height - 4);//, this.colorK

        return new Image(new Texture(pix)).getDrawable();
    }

    private static Color buildColor(int r, int g, int b) {
        return new Color(r / 255f, g / 255f, b / 255f, 1);
    }

    private static void drawLineHoriz(Pixmap pix, int x, int y, int width) {
        pix.drawLine(x, y, x + width, y);
    }

    private static void drawLineVert(Pixmap pix, int x, int y, int height) {
        pix.drawLine(x, y, x, y + height);
    }

}