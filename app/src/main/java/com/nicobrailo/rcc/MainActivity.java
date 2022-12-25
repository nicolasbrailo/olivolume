package com.nicobrailo.rcc;

import android.content.Intent;
import android.media.AudioManager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.vol_up).setOnClickListener( this);
        findViewById(R.id.vol_down).setOnClickListener((View.OnClickListener) this);

        Intent i = new Intent(this, CommandDispatcher.class);
        i.setAction("START");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.vol_up) {
            Log.i("XXXXXX", "VOL UO");

        } else if (v.getId() == R.id.vol_down) {
            Log.i("XXXXXX", "VOL DN");

        }else {
            Log.i("XXXXXX", "???");
        }
    }
}