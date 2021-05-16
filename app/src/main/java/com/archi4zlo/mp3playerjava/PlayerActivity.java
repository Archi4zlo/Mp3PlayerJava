package com.archi4zlo.mp3playerjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button btnPlay, btnNext, btnPrev, btnForward, btnRewind;
    TextView textViewSongName, txtStartTime, txtStopTime;
    SeekBar seekMusic;
    BarVisualizer visualizer;
    ImageView imageView;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    Thread updateSeekBar;


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer!=null){
            visualizer.release();
        }
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        imageView = findViewById(R.id.imageViewSongMain);
        btnPlay = findViewById(R.id.playBtn);
        btnNext = findViewById(R.id.nextBtn);
        btnForward = findViewById(R.id.forwardBtn);
        btnPrev = findViewById(R.id.previousBtn);
        btnRewind = findViewById(R.id.rewindBtn);
        textViewSongName = findViewById(R.id.txtSongMain);
        txtStartTime = findViewById(R.id.txtStartSong);
        txtStopTime = findViewById(R.id.txtStopSong);
        seekMusic = findViewById(R.id.seekBar);
        visualizer = findViewById(R.id.blast);


        if (mediaPlayer !=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songName");
        position = bundle.getInt("position",0);

        textViewSongName.setSelected(true);

        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        textViewSongName.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        updateSeekBar();

        seekMusic.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekMusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.MULTIPLY);
        seekMusic.getThumb().setColorFilter(getResources().getColor(R.color.mainColor),PorterDuff.Mode.SRC_IN);


        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        updateHandlerCurrentTime();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    btnPlay.setBackgroundResource(R.drawable.ic_play_white);
                    mediaPlayer.pause();
                }
                else{
                    btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_white);
                    mediaPlayer.start();
                }
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position+1)%mySongs.size();
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                sname = mySongs.get(position).getName();
                textViewSongName.setText(sname);
                updateSeekBar();
                updateHandlerCurrentTime();
                mediaPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_white);
                startAnimation(imageView);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                sname = mySongs.get(position).getName();
                textViewSongName.setText(sname);
                updateSeekBar();
                updateHandlerCurrentTime();
                mediaPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_white);
                startAnimation(imageView);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });
        btnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });

        //nest listener
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.performClick();
            }
        });

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1){
            visualizer.setAudioSessionId(audioSessionId);
        }

    }
    public void updateHandlerCurrentTime(){
        String endTime =  createTime(mediaPlayer.getDuration());
        txtStopTime.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtStartTime.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        }, delay);
    }
    public void startAnimation(View view){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView,"rotation", 0f,360f);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }

    public void updateSeekBar(){
        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while(currentPosition<totalDuration){
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusic.setProgress(currentPosition);
                    } catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
    }
    public String createTime(int duration){

        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+= min+":";

        if (sec<10){
            time +="0";
        }
        time += sec;
        return time;
    }
}