package com.example.clara.accelerometer;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

/* Tutorial used:
https://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
 */

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
    private static final int WAIT_TIME = 150;
    private int steps;
    private TextView stepcount;
    private TextView bpmView;
    private TextView currentlyPlaying;
    private Pedometer pedometer = new Pedometer();
    int bpm = 100;
    private MediaPlayer mediaPlayer;
    private MusicLib musicLib = new MusicLib();
    private int playingSongNo;
    private ArrayList<String> songList;
    private TextToSpeech t1;
    private boolean checkStart = false;
    private String songName;
    private String songBPM;

    //Installation failed with message INSTALL_FAILED_UPDATE_INCOMPATIBLE.
    //It is possible that this issue is resolved by uninstalling an existing version of the apk if it is present, and then re-installing.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stepcount = (TextView) findViewById(R.id.stepcount);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(senAccelerometer != null) {
            senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, "The accelerometer is available", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,"Accelerometer is not available", Toast.LENGTH_LONG).show();
        }
        //addSongs();
//        musicLib.newSong(80, "activity_unproductive");
//        musicLib.newSong(80, "appeal_2_humanity");
//        musicLib.newSong(70,"icicle_breathingagainfeaturingproximarender001");
//        musicLib.newSong(78,"common_afilmcalledpimprender001");

        //paul-trial addsongs claras songs zaten niet in de github
        musicLib.newSong(85, "all_over__chiddy_bang");
        musicLib.newSong(120, "checka_var_teknik__broderjohn_friman");
        musicLib.newSong(91,"heatwave__mac_miller");
        musicLib.newSong(140, "got_money__lil_wayne");
        musicLib.newSong(128, "love_lockdown__lmfao");
        musicLib.newSong(78,"under_the_sheets__chiddy_bang");
        musicLib.newSong(108,"zemmastyle__bart");
        musicLib.newSong(170,"too_much_soul__chiddy_bang");
        musicLib.newSong(1000,"defaultsong");




//        musicLib.newSong(140,"Lil Wayne ft. T-Pain - Got Money");
//        musicLib.newSong(128,"Love Lockdown - LMFAO Electro Remix (Clean)");
//        musicLib.newSong(78,"Under The Sheets");
//        musicLib.newSong(108,"Zemmastyle");


        currentlyPlaying = (TextView) findViewById(R.id.currentlyPlayed);
    }

//    private void addSongs() {
//        Field[] fields=R.raw.class.getFields();
//        for(int count=0; count < fields.length; count++){
//            Log.i("Raw Asset: ", fields[count].getName());
//        }
//    }





    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > WAIT_TIME) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    stepRegistered();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    public void stepRegistered() {
        steps = steps + 1;
        stepcount.setText(String.valueOf(steps));
        int oldbpm = bpm;
        bpm = pedometer.getBPM();
        bpmView = (TextView) findViewById(R.id.bpm);
        bpmView.setText(String.valueOf(bpm));
        Log.d("bpmFromPed",String.valueOf(bpm));
        if(bpm != oldbpm) {
            differentBPM();
        }
    }

    public void differentBPM() {
        //toegevoegd zodat het vorige nummer wordt gestopt als differentbpm wordt aangeroepen.
        // If statement zodat die niet errored als de mediaplayer nog niet geiniieerd is
        if(checkStart){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        informUser();
        songList = musicLib.getSonglist(bpm);
        if(songList.size() > 0) {
            String songBPMName = songList.get(0);
            songBPM = songBPMName.substring(0,songBPMName.indexOf(' ')); // "72"
            songName = songBPMName.substring(songBPMName.indexOf(' ')+1); // "tocirah sneab"
            playingSongNo = 0;
            playSound(songName);
        } else {
            Log.d("sd","NO SONGS FOR THIS BPM");
            playSound("defaultsong");
        }
    }

    //userinform stukje
    public void informUser(){
        String newBPM ="";
        if(bpm!=0) {

            newBPM = String.valueOf(bpm);
        } else{
            newBPM = "not yet registered";
        }
        Log.d("speechbpm",newBPM);
        String toSpeak = "New pace is, "+ newBPM + "beats per minute";
        Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();

        // deze onzin hashmap is blijkbaar nodig om de volume van de speech aan te passen
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0.4");

        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params);
    }

    public void playSound(String songName) {
        Resources res = this.getResources();
        int soundId = res.getIdentifier(songName, "raw", this.getPackageName());
        Log.d("playing from: ", String.valueOf(soundId));
        mediaPlayer = MediaPlayer.create(this, soundId);
        checkStart = true;
        setOnSongCompletionListener();
        displayPlayingSong();
        mediaPlayer.start();
    }

    public void displayPlayingSong() {
        char[] songNameChars = songName.toCharArray();

        for (int i = 0; i < songNameChars.length; i++){
            char c = songNameChars[i];
            if(c == '_') {
                char c1 = songNameChars[i+1];
                if(c1 == '_') {
                    songNameChars[i] = ':';
                    songNameChars[i+1] = ' ';
                } else {
                    songNameChars[i] = ' ';
                }
            }
        }
        songName = String.valueOf(songNameChars);
        currentlyPlaying.setText(songName + " (BPM: " + songBPM + ")");
    }

    public void setOnSongCompletionListener() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                if(songList.size() > 0) {
                    nextSong();
                }

            }
        });
        //mediaPlayer.start();
    }

    public void nextSong() {
        playingSongNo = playingSongNo + 1;
        if(songList.size() <= playingSongNo) {
            playingSongNo = 0;
        }
        String songName = songList.get(playingSongNo);
        playSound(songName);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void resetCount(View view) {
        steps = 0;
        stepcount.setText(String.valueOf(steps));

        // zodat ook de muziek stopt bij reset
        if(checkStart){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

    }

    public void simulateStep(View view) {
        stepRegistered();
    }

}
