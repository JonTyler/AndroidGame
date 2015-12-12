package com.darch.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.util.Random;

/**
 * Created by Jon on 2015-12-01. fighters strafe side to side firing normal boolets.
 */
public class Fighter extends Enemy implements UpdateInterface, BehaviourInterface, DrawableInterface {

    private int targetX;
    private int targetY;
    private Random rand = new Random();
    private boolean defaultDirection;

    public Fighter(Bitmap res, int startingX, int startingY, int w, int h, int s, int numFrames, Player player, int Hitpoints) {
        GetPlayer(player);
        x = startingX;
        y = startingY;
        setHitPoints(Hitpoints);
        height = h;
        width = w;
        score = s;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = spriteFrame; i < image.length; i++)
        {
            image[i] = Bitmap.createBitmap(spritesheet, i*width, 0, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(100-speed);
        this.speed = 60;
        this.defaultDirection = rand.nextBoolean();
    }

    @Override
    public void GetPlayer(Player player) {
        //this guy fires boolets at players
        targetX = player.getX();
        targetY = player.getY();
    }



    @Override
    public void Update() {
        //this guy will bounce from side to side
        x-=40;
        if(defaultDirection)
        {
            y-=speed;
        }
        else{
            y+=speed;
        }
        if(y > GamePanel.HEIGHT)
        {
            defaultDirection = true;
        }
        if(y < 0)
        {
            defaultDirection = false;
        }
        animation.update();

    }

    @Override
    public void Draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(),x,y,null);
    }
}
