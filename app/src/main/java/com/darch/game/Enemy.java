package com.darch.game;

import android.graphics.Canvas;
import android.graphics.Bitmap;
/**
 * Enemy.java
 * Abstract class for all enemy ships. Instantiates all common attributes a moving game object needs
 *
 * Revision History: Created by Jon on 2015-12-01.
 */
public abstract class Enemy extends GameObject{
    public int getHitPoints() {
        return HitPoints;
    }

    public void setHitPoints(int hitPoints) {
        HitPoints = hitPoints;
    }

    protected int HitPoints;
    protected Animation animation = new Animation();
    protected int speed;
    protected Bitmap spritesheet;
    public int spriteFrame = 0;
    protected int score;

}
