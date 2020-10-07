package com.example.projectbreakout;

import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Random;

public class PlayingArea extends View implements TimeAnimator.TimeListener{

    private final int maxLevel = 6;

    private MediaPlayer mp;
    private int mClipID;

    //fields for onSaveInstance
    private int[][] brickWall = new int[10][10];
    private int brickLife = 1;
    private int startingBricks = 5;
    private int brickCount = startingBricks;
    private int currentLevel = 1;
    private int maxBalls = 3;
    private int balls = maxBalls;
    private int score = 0;
    private boolean paused = true;
    private int[] paddleCoordinates = {120,170,180,175};
    private String paddleDirection = "none";
    private float[] ballEdgeCoordinates = {149,125,154,130,149,135,144,130};
    private float[] ballCenterCoordinates = {149,130}; //x,y
    private float velocitySpeed = 2;
    private float velocityX = 0 *velocitySpeed;
    private float velocityY = 1 *velocitySpeed;
    private int paddleSpeed = 4;
    private long gameTimer = 0;

    private final long MAXTIME = 50;
    private TimeAnimator mTimer;
    private Bitmap screen;

    private int theWidth;
    private int theHeight;
    private ScoreBoardObservable playingObs = new ScoreBoardObservable();

    private final int XTOP = 0, YTOP = 1, XRIGHT = 2, YRIGHT = 3, XBOTTOM = 4, YBOTTOM = 5, XLEFT = 6, YLEFT = 7;


    //Design preview window calls this one
    public PlayingArea(Context context) {
        super(context);
        initializeTimer();
        initializeBrickWall();
        initializeBalls();
    }


    //This will be called when the view is inflated from an XML file
    public PlayingArea(Context context, AttributeSet attrs){
        super(context, attrs);
        initializeTimer();
        initializeBrickWall();
        initializeBalls();
    }

    private void playClip(int id) {
        if(mp != null && id == mClipID){
            mp.pause();
            mp.seekTo(0);
            mp.start();
        }
        else{
            if(mp!= null) mp.release();
            mClipID = id;
            mp = MediaPlayer.create(getContext(), id);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer amp) {
                    amp.release();
                    mp = null;
                }
            });
            mp.setVolume(0.5f,0.5f);
            mp.start();
        }
    }

    private void initializeBalls() {
        this.balls = maxBalls;
    }

    public void setBallVelocity(float x, float y){
        velocityX = x;
        velocityY = y;
    }

    private void resetBallCoordinates(){
        float[] ballEdge = {149,125,154,130,149,135,144,130};
        float[] ballCenter = {149,130};
        ballEdgeCoordinates = ballEdge;
        ballCenterCoordinates = ballCenter;
    }

    private void resetPaddleCoordinates(){
        int[] startingPaddleCoordinates = {120,170,180,175};
        paddleCoordinates = startingPaddleCoordinates;
    }

    private void initializeBrickWall() {
        Integer[] temp = new Integer[100];
        for(int i = 0; i < startingBricks; i++){
            temp[i] = brickLife;
        }
        for(int i = startingBricks; i < 100; i++){
            temp[i] = 0;
        }
        List<Integer> intList = Arrays.asList(temp);
        Collections.shuffle(intList);

        int index = 0;
        for(int i = 0; i < brickWall.length; i++){
            for(int j = 0; j < brickWall[0].length; j++){
                brickWall[i][j] = intList.get(index);
                index++;
            }
        }

    }

    private void initializeTimer(){
        mTimer = new TimeAnimator();
        mTimer.setTimeListener(this);
        go();
    }

    private void go(){
        mTimer.start();
    }

    //so that the layout design editor can get a preview of your custom class
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.theWidth = w;
        this.theHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int aspectRatioWidth = 30;
        int aspectRatioHeight = 19;

        int widthOriginal = MeasureSpec.getSize(widthMeasureSpec);
        int heightOriginal = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedHeight = (aspectRatioHeight * widthOriginal)/aspectRatioWidth; //convert height to aspect ratio 30/19
        int calculatedWidth = heightOriginal * aspectRatioWidth/aspectRatioHeight;

        int finalWidth, finalHeight;
        if(calculatedHeight > heightOriginal){//if height ends up being larger than original do width
            finalWidth = calculatedWidth;
            finalHeight = heightOriginal;
        }
        else{
            finalHeight = calculatedHeight;//if width ends up being larger than original do height
            finalWidth = widthOriginal;
        }
        setMeasuredDimension(finalWidth,finalHeight);
    }

    @Override
    protected void onDraw(Canvas original) {
        super.onDraw(original);

        //create bitmap
        screen = Bitmap.createBitmap(500,300, Bitmap.Config.ARGB_8888);

        //create temporary canvas and setBitmap for it to draw on
        Canvas canvas = new Canvas();
        canvas.setBitmap(screen);

        //scale original
        float width = (float)getWidth()/300;
        float height = (float)getHeight()/190;
        original.scale(width,height);

        //set Color
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);

        //draw paddle
        updatePaddleCoordinates();
        canvas.save();
        p.setColor(Color.BLACK);
        canvas.drawRect(paddleCoordinates[0], paddleCoordinates[1], paddleCoordinates[2], paddleCoordinates[3],p);
        canvas.restore();

        //draw bricks
        int yTransform = 0;
        for(int i = 0; i < 10; i++){
            int xTransform = 0;
            for(int j = 0; j < 10; j++) {
                canvas.save();
                if((brickWall[i][j] > 1 && brickLife > 1) || (brickWall[i][j]==1 && brickLife == 1)){
                    p.setColor(Color.BLUE);
                }
                else if(brickWall[i][j] == 1 && brickLife > 1){
                    p.setColor(Color.RED);
                }
                else{
                    p.setColor(Color.argb(0,255,255,255));
                }
                canvas.translate(xTransform, yTransform);
                canvas.drawRect(1, 1, 30, 10, p);
                canvas.restore();
                xTransform += 30;

            }
            yTransform += 10;
        }

        //draw ball
        updateBallCoordinates();
        original.save();
        p.setColor(Color.YELLOW);
        original.drawCircle(ballCenterCoordinates[0],ballCenterCoordinates[1],5,p);
        original.restore();

        //draw finished bitmap
        original.drawBitmap(screen,0,0,p);
    }

    private void updateBallCoordinates() {
        int xCoords = 0, yCoords = 1;

        //update center coordinate
        ballCenterCoordinates[yCoords]+=velocityY;
        ballCenterCoordinates[xCoords]+=velocityX;
        //update surrounding coordinates
        ballEdgeCoordinates[YTOP]+=velocityY;
        ballEdgeCoordinates[XTOP]+=velocityX;

        ballEdgeCoordinates[YLEFT]+=velocityY;
        ballEdgeCoordinates[XLEFT]+=velocityX;

        ballEdgeCoordinates[YRIGHT]+=velocityY;
        ballEdgeCoordinates[XRIGHT]+=velocityX;

        ballEdgeCoordinates[YBOTTOM]+=velocityY;
        ballEdgeCoordinates[XBOTTOM]+=velocityX;

    }
    public void updatePaddleCoordinates() {
        if(paddleDirection.equals("left")){
            if(paddleCoordinates[0] <= -30){
                return;
            }
            paddleCoordinates[0] = paddleCoordinates[0] - paddleSpeed;
            paddleCoordinates[2] = paddleCoordinates[2] - paddleSpeed;
        }
        else if(paddleDirection.equals("right")){
            if(paddleCoordinates[2]>=330){
                return;
            }
            paddleCoordinates[0] = paddleCoordinates[0] + paddleSpeed;
            paddleCoordinates[2] = paddleCoordinates[2] + paddleSpeed;
        }
    }

    public void pauseGame() {
        this.paused = true;
    }

    public void setPause(){
        if(this.paused == true){
            this.paused = false;
        }else{
            this.paused = true;
        }
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        playingObs.setLevel(this.currentLevel);
        playingObs.setBalls(this.balls);
        playingObs.setScore(this.score);
        playingObs.setBricks(this.brickCount);
        if(this.paused == false){
            gameTimer+=deltaTime;
            if(gameTimer >= MAXTIME){
                invalidate();//schedule re-draw
            }
            checkBallCoordinates();

            boolean bricksExist = checkRemainingBricks();
            if(bricksExist == false && this.currentLevel < maxLevel){
                //next Level
                startNextLevel();
                toastMsg("LEVEL "+currentLevel+"");
            }

            else if(bricksExist == false && this.currentLevel == maxLevel){
                //you win
                toastMsg("YOU WON!");
                restartGame();
            }

            else if(this.balls <= 0){
                //you Lose
                toastMsg("YOU LOSE!");
                restartGame();
            }
        }

    }

    private void toastMsg(String msg) {
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void setBrickHits(int hits) {
        if(hits != this.brickLife){
            this.brickLife = hits;
            restartGame();
        }

    }

    public void setStartingBricks(int bricks) {
        if(bricks != this.startingBricks){
            this.startingBricks = bricks;
            restartGame();
        }

    }

    public void setBallCount(int balls) {
        if(balls != this.maxBalls){
            this.maxBalls = balls;
            restartGame();
        }

    }

    private void restartGame() {
        //For now just start game over at level1 and reset everything
        this.gameTimer = 0;
        this.currentLevel = 1;
        this.balls = maxBalls;
        resetBallCoordinates();
        resetPaddleCoordinates();
        initializeBrickWall();
        resetVelocityDown();
        resetScore();
        this.velocitySpeed = 2;
        this.velocityX = 0 *this.velocitySpeed;
        this.velocityY = 1 *this.velocitySpeed;
        this.brickCount = startingBricks;
        this.balls = maxBalls;
        this.paused = true;
    }

    private void resetScore() {
        this.score = 0;
    }

    private void startNextLevel() {
        this.gameTimer = 0;
        this.currentLevel++;
        this.balls = maxBalls;
        resetBallCoordinates();
        resetPaddleCoordinates();
        initializeBrickWall();
        resetVelocityDown();
        increaseLevelSpeed();
        this.brickCount = startingBricks;
        this.balls = maxBalls;
        this.paused = true;
    }

    private void increaseLevelSpeed() {
        if(this.currentLevel == 6){
            this.velocitySpeed = (float) 7.5;
        }
        else{
            this.velocitySpeed = (float) (velocitySpeed *1.33);
        }

    }

    private boolean checkRemainingBricks() {
        for(int i = 0; i < brickWall.length; i++){
            for( int j = 0; j < brickWall[0].length; j++){
                if(brickWall[i][j] > 0){
                    return true;
                }
            }
        }
        return false;
    }

    private void checkBallCoordinates() {
        //if ball fell off screen
        if(ballCenterCoordinates[1] >= 190){
            //play bad sound
            playClip(R.raw.nope);
            //reset ball and pause
            resetBallCoordinates();
            resetVelocityDown();
            resetPaddleCoordinates();
            this.balls--;
            paused = true;
            return;
        }

        //if ball at ceiling
        if(ballEdgeCoordinates[YTOP] <=0){
            playClip(R.raw.hit);
            setBallVelocity(velocityX,velocityY*-1);
            return;
        }

        //if ball at wall
        if(ballEdgeCoordinates[XLEFT] <= 0 || ballCenterCoordinates[0] >= 300){
            playClip(R.raw.hit);
            setBallVelocity(velocityX * -1, velocityY);
            return;
        }


        if(checkTopBall()){
            playClip(R.raw.hit);
            return;
        }
        else if(checkBottomBall()){
            playClip(R.raw.hit);
            return;
        }
        else if(checkLeftSideBall()){
            playClip(R.raw.hit);
            return;
        }
        else if(checkRightSideBall()){
            playClip(R.raw.hit);
            return;
        }

    }

    private void resetVelocityDown() {
        velocityX = 0 *velocitySpeed;
        velocityY = 1 *velocitySpeed;
    }

    private boolean checkLeftSideBall() {
        int pixel;
        pixel = screen.getPixel((int)ballEdgeCoordinates[XLEFT],(int)ballEdgeCoordinates[YLEFT]);
        if(pixel == Color.BLUE || pixel == Color.RED){
            //set brick to new value
            changeBrickValue(ballEdgeCoordinates[XLEFT],ballEdgeCoordinates[YLEFT]);
            //change ball direction
            setBallVelocity(velocityX,velocityY*-1);
            return true;
        }
        return false;
    }

    private boolean checkRightSideBall() {
        int pixel;
        pixel = screen.getPixel((int)ballEdgeCoordinates[XRIGHT],(int)ballEdgeCoordinates[YRIGHT]);
        if(pixel == Color.BLUE || pixel == Color.RED){
            //set brick to new value
            changeBrickValue(ballEdgeCoordinates[XRIGHT],ballEdgeCoordinates[YRIGHT]);
            //change ball direction
            setBallVelocity(velocityX*-1,velocityY*-1);
            return true;
        }
        return false;
    }

    private boolean checkTopBall(){
        int pixel;
        pixel = screen.getPixel((int)ballEdgeCoordinates[XTOP],(int)ballEdgeCoordinates[YTOP]);
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);
        if(pixel == Color.BLUE || pixel == Color.RED){
            //set brick to new value
            changeBrickValue(ballEdgeCoordinates[XTOP],ballEdgeCoordinates[YTOP]);
            //change ball direction
            setBallVelocity(velocityX,velocityY*-1);
            return true;
        }
        return false;
    }

    private boolean checkBottomBall(){
        int pixel;
        pixel = screen.getPixel((int)ballEdgeCoordinates[XBOTTOM],(int) ballEdgeCoordinates[YBOTTOM]);
        if(pixel == Color.BLUE || pixel == Color.RED){
            //set brick to new value
            changeBrickValue(ballEdgeCoordinates[XBOTTOM],ballEdgeCoordinates[YBOTTOM]);
            //change ball direction
            setBallVelocity(velocityX,velocityY*-1);
            return true;
        }
        if(pixel == Color.BLACK){
            changeVelocityBallPaddle();
            return true;
        }
        return false;
    }

    private void changeVelocityBallPaddle() {
        //normalize to -1 to 1
        //returns -1 if hits leftmost side, 1 rightmost side, 0 anywhere else on paddle
        int halfWidthPaddle = (paddleCoordinates[2] - paddleCoordinates[0])/2;
        double norm = ((ballEdgeCoordinates[XBOTTOM]-(paddleCoordinates[0]+halfWidthPaddle))/halfWidthPaddle);

        setBallVelocity((float) (norm*velocityY/3),velocityY*-1);

    }

    private void changeBrickValue(float x, float y) {
        int width = 300;
        int col,row;
        if(y%10 == 0){
            row = (int) ((y/10)-1);
        }
        else{
            row = (int) (y/10);
        }
        col = (int) ((x*10)/width);
        brickWall[row][col]--;
        if(brickWall[row][col] == 0){
            score++;
            brickCount--;
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        //store what superclass wants stored into bundle
        bundle.putParcelable("instanceState",super.onSaveInstanceState());

        for(int i = 0; i < 10; i++){
            bundle.putIntArray("brickWall"+i,brickWall[i]);
        }
        bundle.putInt("brickLife",brickLife);
        bundle.putInt("startingBricks",startingBricks);
        bundle.putInt("brickCount",brickCount);
        bundle.putInt("currentLevel",currentLevel);
        bundle.putInt("balls",balls);
        bundle.putInt("maxBalls",maxBalls);
        bundle.putInt("score",score);
        bundle.putBoolean("paused",paused);
        bundle.putIntArray("paddleCoordinates",paddleCoordinates);
        bundle.putString("paddleDirection",paddleDirection);
        bundle.putFloatArray("ballEdgeCoordinates",ballEdgeCoordinates);
        bundle.putFloatArray("ballCenterCoordinates",ballCenterCoordinates);
        bundle.putFloat("velocitySpeed",velocitySpeed);
        bundle.putFloat("velocityX",velocityX);
        bundle.putFloat("velocityY",velocityY);
        bundle.putInt("paddleSpeed",paddleSpeed);
        bundle.putLong("gameTimer",gameTimer);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        //use bundle to restore state
        for(int i = 0; i < 10; i++){
            this.brickWall[i] = bundle.getIntArray("brickWall"+i);
        }
        this.brickLife = bundle.getInt("brickLife");
        this.startingBricks = bundle.getInt("startingBricks");
        this.brickCount = bundle.getInt("brickCount");
        this.currentLevel = bundle.getInt("currentLevel");
        this.balls = bundle.getInt("balls");
        this.maxBalls = bundle.getInt("maxBalls");
        this.score = bundle.getInt("score");
        this.paused = bundle.getBoolean("paused");
        this.paddleCoordinates = bundle.getIntArray("paddleCoordinates");
        this.paddleDirection = bundle.getString("paddleDirection");
        this.ballEdgeCoordinates = bundle.getFloatArray("ballEdgeCoordinates");
        this.ballCenterCoordinates = bundle.getFloatArray("ballCenterCoordinates");
        this.velocitySpeed = bundle.getFloat("velocitySpeed");
        this.velocityX = bundle.getFloat("velocityX");
        this.velocityY = bundle.getFloat("velocityY");
        this.paddleSpeed = bundle.getInt("paddleSpeed");
        this.gameTimer = bundle.getLong("gameTimer");

        //grab whatever the super class stored and pass it along
        state = bundle.getParcelable("instanceState");
        super.onRestoreInstanceState(state);
    }


    public Observable getObservable() {
        return this.playingObs;
    }


    public void setPaddleDirection(String direction) { paddleDirection = direction; }

    public int getLevel() { return playingObs.getLevel(); }

    public int getBalls() { return playingObs.getBalls(); }

    public int getScore() { return playingObs.getScore(); }

    public int getBricks() { return playingObs.getBricks(); }




    private class ScoreBoardObservable extends Observable{
        private int level;
        private int balls;
        private int score;
        private int bricks;
        ScoreBoardObservable(){

        }

        public void setLevel(int level){
            this.level = level;
            setChanged();
            notifyObservers();
        }

        public void setBalls(int balls){
            this.balls = balls;
            setChanged();
            notifyObservers();
        }

        public void setScore(int score){
            this.score = score;
            setChanged();
            notifyObservers();
        }

        public void setBricks(int bricks){
            this.bricks = bricks;
            setChanged();
            notifyObservers();
        }

        public int getLevel(){return this.level;}
        public int getBalls(){return this.balls;}
        public int getScore(){return this.score;}
        public int getBricks(){return this.bricks;}


    }
}

