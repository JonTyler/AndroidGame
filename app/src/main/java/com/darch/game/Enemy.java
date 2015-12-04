package com.darch.game;

import android.graphics.Canvas;
import android.graphics.Bitmap;
/**
 * Created by Jon on 2015-12-01.
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

}
