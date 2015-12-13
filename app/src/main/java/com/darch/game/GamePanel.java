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
        import android.widget.Toast;

        import com.firebase.client.ChildEventListener;
        import com.firebase.client.DataSnapshot;
        import com.firebase.client.Firebase;
        import com.firebase.client.FirebaseError;
        import com.firebase.client.Query;

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
    private long fighterStartTime;
    private long fighterFireDelayTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private Smokepuff  smokePuff;
    private ArrayList<Asteroid> asteroids;
    private ArrayList<otherFighter> fighters;
    private Random rand = new Random();
    private boolean newGameCreated;
    private Game game;
    private Enemy enemy;
    private ArrayList<StraightawayBoolet> allStraightBullets;
    private String Highscore;

    float lastXAxis = 0f;
    float lastYAxis = 0f;

    DisplayMetrics display = getContext().getResources().getDisplayMetrics();
    public float dpHeight = display.heightPixels / display.density;
    public float dpWidth = display.widthPixels;

    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;
    private long shotElapsed;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;

    private String previousScore;
    private String currentScore;

    public MediaPlayer gameMusic;
    public MediaPlayer expolosionSound;

    public GamePanel(Context context)
    {
        super(context);

        gameMusic = MediaPlayer.create(context, R.raw.human_music);
        expolosionSound = MediaPlayer.create(context, R.raw.explosion_5);
        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);

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
                gameMusic.pause();

            }catch(InterruptedException e){e.printStackTrace();}
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.full_back_ground));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.ship1), 128, 128, 64);
        getFireBaseHighScore();
        smoke = new ArrayList<Smokepuff>();
        asteroids = new ArrayList<Asteroid>();
        fighters = new ArrayList<otherFighter>();
        fighterStartTime = System.nanoTime();
        smokeStartTime = System.nanoTime();
        asteroidStartTime = System.nanoTime();
        allStraightBullets = new ArrayList<StraightawayBoolet>();
        previousScore = "0";
        currentScore = "0";

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
                    gameMusic = MediaPlayer.create(this.getContext(), R.raw.human_music);
                    gameMusic.setLooping(true);
                    gameMusic.start();
                }
                if(player.getPlaying())
                {
                    if(!started)started = true;
                    reset = false;
                }
            }
        }

        if (lastYAxis<=player.getY()&&lastXAxis<=dpWidth/3) {
            player.setUp(false);
            Log.d("onTouch", "Down");
        }

        if (lastYAxis>=player.getY()&&lastXAxis<=dpWidth/3) {
            player.setUp(true);
            Log.d("onTouch", "Up");
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            Log.d("onTouch", "MotionEvent.ACTION_CANCEL");
        }
        if (lastXAxis >= dpWidth / 3)
        {
            if (shotElapsed > 500)
            {
                allStraightBullets.add(new StraightawayBoolet(BitmapFactory.decodeResource(getResources(), R.drawable.single_frame_bullet)
                        , player.getX()+(player.width/2)+16, player.getY()+(player.height/2)-16, 32, 32, player.getScore(), 1, true, player));

                playerShotStartTime = System.nanoTime();
            }
        }
        //return false;
        return super.onTouchEvent(event);
    }

    public void addScoreToFireBase()
    {
        Firebase joryRef = new Firebase("https://jory-impulse.firebaseio.com/");
        Firebase playerRef = joryRef.child("Players");
        Map<String, String> highScorePost = new HashMap<String, String>();
        highScorePost.put("Name", "Testing");
        String score = Integer.toString(player.getScore());
        highScorePost.put("High Score", score);
        playerRef.push().setValue(highScorePost);
        previousScore = score;
        if(Integer.parseInt(previousScore)>Integer.parseInt(currentScore))
        {
            currentScore = previousScore;
        }
        previousScore = score;
        getFireBaseHighScore();
    }
    public void getFireBaseHighScore()
    {
        Firebase joryRef = new Firebase("https://jory-impulse.firebaseio.com/Players");
        Query joryQueer = joryRef.orderByChild("High Score").limitToLast(1);
        joryQueer.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                Highscore = String.valueOf(dataSnapshot.getValue());
                Highscore = Highscore.substring(26, Highscore.length()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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
            shotElapsed = (System.nanoTime()-playerShotStartTime)/1000000;

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

            //add fighters on timer
            long fighterElapsed = (System.nanoTime()-fighterStartTime)/1000000;
            if(fighterElapsed > 2000)
            {
                fighters.add(new otherFighter(BitmapFactory.decodeResource(getResources(),R.drawable.strip_fighter),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT)),128,128, player.getScore(),1));
                //reset timer
                fighterStartTime = System.nanoTime();
            }

            //add asteroids on timer
            long asteroidElapsed = (System.nanoTime()-asteroidStartTime)/1000000;
            if(asteroidElapsed > 2000){
                //first Asteroid always goes down the middle
                if(asteroids.size()==0)
                {
                    asteroids.add(new Asteroid(BitmapFactory.decodeResource(getResources(), R.drawable.strip_rock_type_a)
                            , WIDTH + 10, HEIGHT / 2, 64, 64, player.getScore(), 1));
                    rotate(asteroids,90);
                }
                else
                {
                    asteroids.add(new Asteroid(BitmapFactory.decodeResource(getResources(),R.drawable.strip_rock_type_a),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT)),64,64, player.getScore(),1));
                }
                //reset timer
                asteroidStartTime = System.nanoTime();
            }
            //loop through every straightaway boolet and check collision and remove
            for(int i = 0; i<allStraightBullets.size();i++) {
                allStraightBullets.get(i).Update();
                if (collision(allStraightBullets.get(i), player) && !allStraightBullets.get(i).isFriendly) {
                    allStraightBullets.remove(i);
                    player.setPlaying(false);
                    expolosionSound.reset();
                    expolosionSound = MediaPlayer.create(this.getContext(), R.raw.explosion_4);
                    expolosionSound.start();
                    gameMusic.reset();
                    addScoreToFireBase();
                    break;
                }
            }
            long fighterBulletElapsed = (System.nanoTime()-fighterFireDelayTime)/1000000;

            // loop through every Fighter and check collision and remove
            for(int i = 0; i<fighters.size();i++)
            {
                //update fighters
                fighters.get(i).Update();
                //bullet collision already takes care of fighters
                //they spawn unfriendly bullets instead.
                if(fighterBulletElapsed > 1500)
                {
                    allStraightBullets.add(new StraightawayBoolet(BitmapFactory.decodeResource(getResources(), R.drawable.bullet_strip)
                            , fighters.get(i).getX()-64+16, fighters.get(i).getY()-16+64, 32, 32, player.getScore(), 1, false, player));
                }

                for(int j = 0; j<allStraightBullets.size();j++)
                {
                    if (collision(allStraightBullets.get(j), fighters.get(i)) && allStraightBullets.get(j).isFriendly) {
                        allStraightBullets.remove(j);
                        explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.exp_type_a), fighters.get(i).getX() -64,
                                fighters.get(i).getY() - 64, 128, 128, 40);
                        expolosionSound.reset();
                        expolosionSound = MediaPlayer.create(this.getContext(), R.raw.explosion_5);
                        expolosionSound.start();
                        fighters.remove(i);
                    }
                    explosion.Update();
                }
            }
            //this is outside the entire fighter for loop. god help me.
            if(fighterBulletElapsed > 1500)
            {
                fighterFireDelayTime = System.nanoTime();
            }
            //loop through every Asteroid and check collision and remove
            for(int i = 0; i<asteroids.size();i++)
            {
                //update Asteroid
                asteroids.get(i).Update();

                if(collision(asteroids.get(i),player)) {
                    player.setPlaying(false);
                    gameMusic.reset();
                    expolosionSound.reset();
                    expolosionSound = MediaPlayer.create(this.getContext(), R.raw.explosion_5);
                    expolosionSound.start();
                    addScoreToFireBase();
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
                smoke.add(new Smokepuff(player.getX()-10, player.getY()+68));
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

            for(Missile mi : allStraightBullets)
            {
                mi.Draw(canvas);
            }

            for (otherFighter f : fighters)
            {
                f.Draw(canvas);
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
        fighters.clear();
        allStraightBullets.clear();
        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);
        newGameCreated = true;
    }

    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(WIDTH / 90);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Distance: " + (player.getScore()), WIDTH * (4 / 5), HEIGHT - HEIGHT * (9/10), paint);
        canvas.drawText("Beat It!: " + Highscore, (WIDTH/2) - (WIDTH/10), HEIGHT - HEIGHT*(9/10), paint);

        canvas.drawText("Previous Run: " + previousScore, (WIDTH/4) - (WIDTH/9), HEIGHT - HEIGHT*(9/10), paint);
        canvas.drawText("Current Best: " + currentScore, (WIDTH/3) - (WIDTH/15), HEIGHT - HEIGHT*(9/10), paint);

        if(!player.getPlaying() && newGameCreated&&reset) {
            Paint paint1 = new Paint();
            paint1.setTextSize(WIDTH / 90);
            paint1.setColor(Color.RED);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            float myWidth=paint1.measureText("PRESS TO START");
            canvas.drawText("PRESS TO START", WIDTH / 4 - (myWidth / 2), HEIGHT / 3, paint1);

            myWidth=paint1.measureText("Press Right Side of Screen to Shoot");
            canvas.drawText("Press Right Side of Screen to Shoot", WIDTH / 4 - (myWidth / 2), HEIGHT / 2 - (HEIGHT / 10), paint1);

            myWidth=paint1.measureText("Press Below Ship to Go Down");
            canvas.drawText("Press Below Ship to Go Down", WIDTH / 4 - (myWidth / 2), HEIGHT / 2 + WIDTH / 90, paint1);

            myWidth=paint1.measureText("Press Above Ship to Go Up");
            canvas.drawText("Press Above Ship to Go Up", WIDTH/4 - (myWidth/2), HEIGHT / 2, paint1);

            myWidth=paint1.measureText("Asteroids Don't Break");
            canvas.drawText("Asteroids Don't Break", WIDTH/4 - (myWidth/2), HEIGHT / 2 +(HEIGHT/6), paint1);

            myWidth=paint1.measureText("Your Ship Self-Destructs When You Leave");
            canvas.drawText("Your Ship Self-Destructs When You Leave", WIDTH/4 - (myWidth/2), HEIGHT / 2 + (HEIGHT/4), paint1);
        }
    }
}