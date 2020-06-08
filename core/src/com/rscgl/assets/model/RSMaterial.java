package com.rscgl.assets.model;

import com.badlogic.gdx.graphics.Color;
import com.rscgl.ui.util.ColorUtil;

public class RSMaterial {

    public static final int TRANSPARENT = 0xbc614e;
    public static final int[] colorToResource = new int[256];

    static {
        int i;
        for (i = 0; i < 64; ++i)
            colorToResource[i] = colorToResource(255 - i * 4,
                    255 - (int) ((double) i * 1.75D), 255 - i * 4);

        for (i = 0; i < 64; ++i)
            colorToResource[64 + i] = colorToResource(i * 3, 144, 0);

        for (i = 0; i < 64; ++i)
            colorToResource[128 + i] = colorToResource(192 - (int) ((double) i * 1.5D),
                    144 - (int) ((double) i * 1.5D), 0);

        for (i = 0; i < 64; ++i)
            colorToResource[192 + i] = colorToResource(96 - (int) ((double) i * 1.5D),
                    (int) ((double) i * 1.5D) + 48, 0);
    }

    public static final int colorToResource(int r, int g, int b) {
        b >>= 3;
        r >>= 3;
        g >>= 3;
        return -(g << 5) - 1 - (r << 10) - b;
    }

    public static Color getColor(int id) {
        return convert(colorToResource[id]);
    }

    public static Color convert(int i) {
        i = -(i + 1);
        int j = i >> 10 & 0x1f;
        int k = i >> 5 & 0x1f;
        int l = i & 0x1f;
        return ColorUtil.toColor((j << 19) + (k << 11) + (l << 3));
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof RSMaterial) {
            RSMaterial RSMaterial = (RSMaterial) o;
            return RSMaterial.colorA == colorA && RSMaterial.colorB == colorB && RSMaterial.type == type;
        }
        return super.equals(o);
    }

    private int colorB;
    private int colorA;
    private int type;

    public RSMaterial(int colorB, int colorA, int type) {
        this.colorB = colorB;
        this.colorA = colorA;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getDecorA() {
        return colorA;
    }

    public int getDecorB() {
        return colorB;
    }

    public Color getColorA() {
        return convert(colorA);
    }
    public Color getColorB() {
        return convert(colorB);
    }
}
