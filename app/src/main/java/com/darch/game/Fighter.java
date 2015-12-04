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

    public Fighter(int startX, int startY, Player player, Bitmap spritesheet, GamePanel gamePanel) {
        GetPlayer(player);
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

    }

    @Override
    public void Draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(),x,y,null);
    }
}
