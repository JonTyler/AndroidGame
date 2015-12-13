package com.darch.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by Jon on 2015-12-01.
 * An asteroid with HP. These have a behaviour of moving slowly downward like a mine, but just
 * block boolets until destroyed.
 */
public class Asteroid extends GameObject implements DrawableInterface, UpdateInterface{
    private int score;
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spritesheet;
    public int spriteFrame = 0;
    private boolean isFriendly;

    public Asteroid(Bitmap res, int x, int y, int w, int h, int s, int numFrames)
    {
        super.x = x;
        super.y = y;
        score = s;
        height = h;
        width = w;

        speed = 7 + (int) (rand.nextDouble()*score/30);

        //cap missile speed
        if(speed>40)speed = 40;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = spriteFrame; i < image.length; i++)
        {
            image[i] = Bitmap.createBitmap(spritesheet, i*width, 0, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(100-speed);

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

    @Override
    public int getWidth()
    {
        //offset slightly for more realistic collision detection
        x=width-20;
        return width;
    }
    @Override
    public int getHeight()
    {
        //offset slightly for more realistic collision detection
        y=height-20;
        return height;
    }
}
