package com.rscgl.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rscgl.assets.Assets;

public class MouseClickIndicator {

    private static TextureRegion[] mouseIndicators = new TextureRegion[9];
    private float mouseClickXStep = 0;
    private float x, y;

    public void render(Batch b) {
        if (mouseClickXStep > 0) {
            mouseClickXStep -= Gdx.graphics.getDeltaTime() * 75F;
        } else if (mouseClickXStep < 0) {
            mouseClickXStep += Gdx.graphics.getDeltaTime() * 75F;
        }
        try {
            if ((int) this.mouseClickXStep > 0 && mouseClickXStep < 24) {
                b.draw(mouseIndicators[(int) (this.mouseClickXStep / 6)], x - 6, y - 6);
            } else if ((int) this.mouseClickXStep < 0 && mouseClickXStep > -24) {
                b.draw(mouseIndicators[(int) (4 + (this.mouseClickXStep + 24) / 6)], x - 8, y - 8);
            } else {
                mouseClickXStep = 0;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void set(float x, float y, float mouseClickXStep) {
        this.x = x;
        this.y = y;
        this.mouseClickXStep = mouseClickXStep;
    }

    static {
        int i2 = 0;
        for (int i = 14; i <= 20; i++) {
            mouseIndicators[i2++] = Assets.inst.getInterSprite(i);
        }
    }
}
