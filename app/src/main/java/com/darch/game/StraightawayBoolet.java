package com.darch.game;

import android.graphics.Bitmap;

/**
 * Created by Jon on 2015-12-01.
 * Extends missile. Moves in a straight line.
 */
public class StraightawayBoolet extends Missile{
    public StraightawayBoolet(Bitmap res, int x, int y, int w, int h, int s, int numFrames, boolean friendly, Player player) {
        super(res, x, y, w, h, s, numFrames, friendly, player);
    }

    // moves forward.
    @Override
    public void Update()
    {
        if(isFriendly)
        {
            x+=speed;
        }
        else
        {
            x-=speed;
        }

        animation.update();
    }
}
