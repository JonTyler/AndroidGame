package com.darch.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Random;
/**
 * Missile.java
 * Superclass for all bullet objects.
 * Revision HIstory: Created by Jon on 2015-12-04.
 */
public class Missile extends GameObject implements UpdateInterface, DrawableInterface, CollisionInterface{
    private int score;
    protected int speed;
    private Random rand = new Random();
    protected Animation animation = new Animation();
    private Bitmap spritesheet;
    public int spriteFrame = 0;
    protected boolean isFriendly;
    protected Player player;


    public Missile(Bitmap res, int x, int y, int w, int h, int s, int numFrames, boolean friendly, Player player)
    {
        super.x = x;
        super.y = y;
        score = s;
        height = h;
        width = w;
        isFriendly = friendly;

        speed = 7 + score/30;

        //cap missile speed
        if(speed>40)speed = 40;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = spriteFrame; i < image.length; i++)
        {
            image[i] = Bitmap.createBitmap(spritesheet, i*width, 0, width, height);
        }

        height=height/5;
        width=width/2;

        animation.setFrames(image);

    }
    public Missile() {

    }
    public void Update()
    {
        x-=speed;
        animation.update();
    }

    public void Draw(Canvas canvas)
    {
        canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    public boolean collision(GameObject a, GameObject b)
    {
        if (Rect.intersects(a.getRectangle(), b.getRectangle()))
        {
            return true;
        }
        return false;
    }
}

