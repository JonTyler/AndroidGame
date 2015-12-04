package com.darch.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Ryan on 11/27/2015.
 */
public class Smokepuff extends GameObject{
    public int r;
    private boolean multiLoc;

    public Smokepuff(int x, int y)
    {
        r = 5;
        super.x = x;
        super.y = y;
    }

    public void setMultiLoc(boolean b){multiLoc = b;}
    public boolean getMultiLoc(){return multiLoc;}

    public void update()
    {
        if (multiLoc = true)
        {
            x-=30;
        }
        if (multiLoc = false)
        {
            x-=30;
        }
    }
    public void draw(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x-r, y-r, r, paint);
        canvas.drawCircle(x-r+2, y-r-2,r,paint);
        canvas.drawCircle(x-r+4, y-r+1, r, paint);
    }
}
