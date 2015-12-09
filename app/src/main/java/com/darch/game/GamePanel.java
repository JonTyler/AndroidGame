package com.darch.game;

        import android.annotation.SuppressLint;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Matrix;
        import android.graphics.Paint;
        import android.graphics.Rect;
        import android.graphics.Typeface;
        import android.graphics.drawable.Drawable;
        import android.media.MediaPlayer;
        import android.provider.MediaStore;
        import android.util.DisplayMetrics;
        import android.util.Log;
        import android.view.MotionEvent;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.app.Activity;

        import com.firebase.client.Firebase;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Map;
        import java.util.Random;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 4080;
    public static final int HEIGHT = 1024;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long asteroidStartTime;
    private long playerShotStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private Smokepuff  smokePuff;
    private ArrayList<Asteroid> asteroids;
    private Random rand = new Random();
    private boolean newGameCreated;
    private Game game;
    private ArrayList<StraightawayBoolet> allStraightBullets;
    private ArrayList<Enemy> allEnemies;

    float lastXAxis = 0f;
    float lastYAxis = 0f;

    DisplayMetrics display = getContext().getResources().getDisplayMetrics();
    float dpHeight = display.heightPixels / display.density;
    float dpWidth = display.widthPixels / display.density;

    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best;

    public MediaPlayer gameMusic;
    public MediaPlayer expolosionSound;

    public GamePanel(Context context)
    {
        super(context);

        gameMusic = MediaPlayer.create(context, R.raw.human_music);
        expolosionSound = MediaPlayer.create(context, R.raw.human_music);
        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        //thread = new MainThread(getHolder(), this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;

            }catch(InterruptedException e){e.printStackTrace();}
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.full_back_ground));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.ship1), 128, 128, 64);
        smoke = new ArrayList<Smokepuff>();
        asteroids = new ArrayList<Asteroid>();
        smokeStartTime = System.nanoTime();
        asteroidStartTime = System.nanoTime();
        allStraightBullets = new ArrayList<StraightawayBoolet>();

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        final int actionPerformed = event.getAction();
        boolean startGame = false;

        switch(actionPerformed)
        {
            case MotionEvent.ACTION_DOWN:{
                final float x = event.getX();
                final float y = event.getY();

                lastXAxis = x;
                lastYAxis = y;

                if(!player.getPlaying() && newGameCreated && reset)
                {
                    player.setPlaying(true);
                    player.spriteFrame = 18;
                    gameMusic.start();
                }
                if(player.getPlaying())
                {
                    if(!started)started = true;
                    reset = false;
                }
            }
        }

        if (lastYAxis<=player.getY()) {
            player.setUp(false);
            Log.d("onTouch", "Down");
        }

        if (lastYAxis>=player.getY()) {
            player.setUp(true);
            Log.d("onTouch", "Up");
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            Log.d("onTouch", "MotionEvent.ACTION_CANCEL");
        }
        //return false;
        return super.onTouchEvent(event);
    }

    public void update()
    {
        if(player.getPlaying()) {
            bg.update();
            player.Update();


            if (player.y <= (GamePanel.HEIGHT * (1 / 9))) {
                player.setY(GamePanel.HEIGHT * (1 / 9));
                Log.d("onTouch", "Border");
            }

            if (player.y >= (GamePanel.HEIGHT) - (GamePanel.HEIGHT / 9)) {
                player.setY((GamePanel.HEIGHT) - (GamePanel.HEIGHT / 9));
                Log.d("onTouch", "Border");
            }

            //player shoots boolets on a timer
            long shotElapsed = (System.nanoTime()-playerShotStartTime)/1000000;
            if (shotElapsed > 500)
            {
                allStraightBullets.add(new StraightawayBoolet(BitmapFactory.decodeResource(getResources(), R.drawable.bullet_strip)
                        , player.x, player.y, 64, 64, player.getScore(), 16, true, player));

                playerShotStartTime = System.nanoTime();
            }
            //get all straight bullets and remove them if they've gone too far offscreen
            for (int i=0; i<allStraightBullets.size();i++)
            {
                allStraightBullets.get(i).Update();

                if(allStraightBullets.get(i).getX()<-100 || allStraightBullets.get(i).getX()> dpWidth)
                {
                    allStraightBullets.remove(i);
                    break;
                }
            }

            //add asteroids on timer
            long asteroidElapsed = (System.nanoTime()-asteroidStartTime)/1000000;
            if(asteroidElapsed > 2000){

                //first Asteroid always goes down the middle
                if(asteroids.size()==0)
                {
                    asteroids.add(new Asteroid(BitmapFactory.decodeResource(getResources(), R.drawable.strip_rock_type_a)
                            , WIDTH + 10, HEIGHT / 2, 64, 64, player.getScore(), 16));
                    rotate(asteroids,90);
                }
                else
                {

                    asteroids.add(new Asteroid(BitmapFactory.decodeResource(getResources(),R.drawable.strip_rock_type_a),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT)),64,64, player.getScore(),16));
                }

                //reset timer
                asteroidStartTime = System.nanoTime();

            }
            //loop through every Asteroid and check collision and remove
            for(int i = 0; i<asteroids.size();i++)
            {
                //update Asteroid
                asteroids.get(i).Update();

                if(collision(asteroids.get(i),player)) {
                    asteroids.remove(i);
                    player.setPlaying(false);
                    gameMusic.stop();
                    gameMusic.reset();
                    break;
                }
                //remove Asteroid if it is way off the screen
                if(asteroids.get(i).getX()<-100)
                {
                    asteroids.remove(i);
                    break;
                }
            }

            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120){
                smoke.add(new Smokepuff(player.getX(), player.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i<smoke.size();i++)
            {
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }
        }
        else {
            player.resetDY();

            if (!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.exp_type_a), player.getX(),
                        player.getY() - 30, 128, 128, 40);
            }
            explosion.Update();
            long resetElapsed = (System.nanoTime() - startReset) / 1000000;


            if (resetElapsed > 2500 && !newGameCreated) {
                newGame();
            }
            Firebase joryRef = new Firebase("https://jory-impulse.firebaseio.com/");
            Map<String, String> highScorePost = new HashMap<String, String>();
            highScorePost.put("Name", "Testing");
            String score = Integer.toString(player.getScore());
            highScorePost.put("High Score", score);
        }
    }

    public void rotate(ArrayList source, float rotation)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        //return Bitmap.createBitmap(source,0,0,);
    }

    public boolean collision(GameObject a, GameObject b)
    {
        if (Rect.intersects(a.getRectangle(),b.getRectangle()))
        {
            return true;
        }
        return false;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas!=null)
        {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX * 2, scaleFactorY);
            bg.draw(canvas);
            if(!dissapear) {
                player.Draw(canvas);
            }

            for (Smokepuff sp : smoke) {
                sp.draw(canvas);
            }

            for (Asteroid m : asteroids) {
                m.Draw(canvas);
            }
            //draw explosion
            if(started)
            {
                explosion.Draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void newGame()
    {
        dissapear = false;
        asteroids.clear();
        smoke.clear();
        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);
        if(player.getScore()>best)
        {
            best = player.getScore();
        }
        newGameCreated = true;
    }

    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(WIDTH/90);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore()*3), WIDTH*(4/5), HEIGHT - HEIGHT*(9/10), paint);
        canvas.drawText("BEST: " + best, (WIDTH/2) - (WIDTH/10), HEIGHT - HEIGHT*(9/10), paint);

        if(!player.getPlaying()&&newGameCreated&&reset)
        {
            Paint paint1 = new Paint();
            paint1.setTextSize(WIDTH / 90);
            paint1.setColor(Color.RED);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            float myWidth=paint1.measureText("PRESS TO START");
            canvas.drawText("PRESS TO START", WIDTH/4 - (myWidth/2), HEIGHT / 3, paint1);

            paint1.setTextSize(WIDTH / 90);
            paint1.setColor(Color.RED);
            myWidth=paint1.measureText("Press Below Ship to Go Down");
            canvas.drawText("Press Below Ship to Go Down", WIDTH/4 - (myWidth/2), HEIGHT / 2 + WIDTH/90, paint1);
            myWidth=paint1.measureText("Press Above Ship to Go Up");
            canvas.drawText("Press Above Ship to Go Up", WIDTH/4 - (myWidth/2), HEIGHT / 2, paint1);
        }
    }
}