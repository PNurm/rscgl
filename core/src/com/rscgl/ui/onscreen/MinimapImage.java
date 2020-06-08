package com.rscgl.ui.onscreen;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;

public class MinimapImage {

    public Pixmap image = new Pixmap(285, 285, Pixmap.Format.RGBA8888);
    public final Rectangle bounds = new Rectangle();
    public final int regionBaseX;
    public final int regionBaseZ;
    public final int plane;
    private boolean disposed;

    public MinimapImage(int regionBaseX, int regionBaseZ, int plane) {
        this.regionBaseX = regionBaseX;
        this.regionBaseZ = regionBaseZ;
        this.plane = plane;
    }

    public void dispose() {
        image.dispose();
        disposed = true;
    }
}
