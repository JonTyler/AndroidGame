package com.darch.game;

import android.graphics.Rect;

/**
 * Created by Jon on 2015-12-01.
 * Extends OldBoolet. Explodes on contact, creating an Explosion that damages things near it.
 */
public class ExplodingBoolet extends Missile{

    //on removal of this object, spawns an explosion at its x and y coordinates
    //detect if this collided.
    @Override
    public void Update() {
        if(collision(this, player))
        {
            //play some sound I guess?
        }
    }
}
