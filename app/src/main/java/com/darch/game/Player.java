package com.darch.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Ryan on 11/26/2015.
 */
public class Player extends GameObject implements UpdateInterface, DrawableInterface{

    private Bitmap spritesheet;
    private int score;
    private boolean up;
    private boolean playing;
    private Animation animation = new Animation();
    private long startTime;
    public int spriteFrame = 0;

    public Player(Bitmap res, int w, int h, int numFrames) {

        x = 100;
        y = (GamePanel.HEIGHT / 2)+64;
        dy = 0;
        score = 0;
        height = h;
        width = w;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = spriteFrame; i < image.length; i++)
        {
            image[i] = Bitmap.createBitmap(spritesheet, 0, i*height, width, height);
        }

        height=height-50;
        width=width-28;

        animation.setFrames(image);
        animation.setDelay(1000);
        startTime = System.nanoTime();

    }

    public void setUp(boolean b){up = b;}

    public void Update()
    {
        long elapsed = (System.nanoTime()-startTime)/1000000;
        if(elapsed>100)
        {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

        if(up){
            dy += 8;

        }
        else{
            dy =- 8;
        }

        if(dy>8)dy = 10;
        if(dy<-8)dy = -10;

        y += dy*2;
    }

    public void Draw(Canvas canvas)
    {
        canvas.drawBitmap(animation.getImage(),x,y,null);
    }
    public int getScore(){return score;}
    public boolean getPlaying(){return playing;}
    public void setPlaying(boolean b){playing = b;}
    public void resetDY(){dy = 0;}
    public void resetScore(){score = 0;}
}

