package com.darch.game;

/**
 * Created by Jon on 2015-12-01.
 * Extends boolet. Moves in a straight line, but speeds up as it goes.
 */
public class AcceleratingBoolet extends Missile{
    //speeds up.

    @Override
    public void Update() {
        x-=speed*1.2;
        animation.update();
    }
}
