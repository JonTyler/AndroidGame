package com.darch.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
/**
 * Created by Jon on 2015-12-01.
 * Spawned in the center of another game object. Its sprite is a damaging hitbox.
 */
public class Explosion implements DrawableInterface, UpdateInterface {
    private int x;
    private int y;
    private int width;
    private int height;
    private int row;
    private Animation animation = new Animation();
    private Bitmap spritesheet;
    public int spriteFrame = 0;

    public Explosion(Bitmap res, int x, int y, int w, int h, int numFrames) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = spriteFrame; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, i * width, 0, width, height);
        }
        animation.setFrames(image);
        animation.setDelay(10);
    }

    public void Draw(Canvas canvas) {
        if (!animation.playedOnce()) {
            canvas.drawBitmap(animation.getImage(), x, y, null);
        }

    }

    public void Update() {
        if (!animation.playedOnce()) {
            animation.update();
        }
    }

    public int getHeight() {
        return height;
    }
}
