package com.example.projectbreakout;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer{
    private PlayingArea playingArea;
    private Observable playingAreaObservable;
    private Button left;
    private Button right;
    private boolean pressed = false;

    //preferences
    private int bricks;
    private int hits;
    private int balls;

    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playingArea = findViewById(R.id.playingArea);
        playingAreaObservable = playingArea.getObservable();
        playingAreaObservable.addObserver(this);

        //set onTouchListener for left button
        left = findViewById(R.id.leftButton);
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    playingArea.setPaddleDirection("left");
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    playingArea.setPaddleDirection("none");
                }
                return false;
            }
        });

        //set onTouchListener for right button
        right = findViewById(R.id.rightButton);
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                   playingArea.setPaddleDirection("right");
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    playingArea.setPaddleDirection("none");
                }
                return false;
            }

        });
        


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onAbout(MenuItem item){
        Toast.makeText(this,
                "ProjectBreakOut, Spring 2020, Kayla Rubin",
                Toast.LENGTH_SHORT).show();
    }

    public void onPlayerAreaClick(View view){
        playingArea.setPause();
    }

    public void onSettings(MenuItem item){
        Intent i = new Intent(this,SettingsActivity.class);
        startActivity(i);
    }

    @Override
    public void update(Observable o, Object arg) {
        //update Scoreboard here
        TextView scoreboard = (TextView) findViewById(R.id.score);
        int level = playingArea.getLevel();
        int balls = playingArea.getBalls();
        int score = playingArea.getScore();
        int bricks = playingArea.getBricks();
        scoreboard.setText("Level="+level+" Balls="+balls+" Score="+score+" Bricks="+bricks);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Gets a SharedPreferences instance that points to the default file that is used by the preference framework in the given context.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ///now read the preferences
        //initial number of bricks
        int bricks = Integer.parseInt(prefs.getString("initialBrickCount", "5"));
        this.bricks = bricks;
        playingArea.setStartingBricks(bricks);

        //hits to remove brick
        int hits = prefs.getInt("hitsToRemoveBrick",1);
        this.hits = hits;
        playingArea.setBrickHits(hits);

        //ball count
        int balls = prefs.getInt("ballsPerLevel",3);
        this.balls = balls;
        playingArea.setBallCount(balls);


    }

    @Override
    protected void onPause() {
        playingArea.pauseGame();
        super.onPause();
    }


}
