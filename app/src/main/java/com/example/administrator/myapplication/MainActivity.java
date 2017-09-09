package com.example.administrator.myapplication;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.administrator.myapplication.viwe.LineView;
import com.example.administrator.myapplication.viwe.ScanAnimation;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private LineView lineView;
    private int i = 0;
    private RelativeLayout rel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rel = (RelativeLayout) findViewById(R.id.sca);
        ScanAnimation ani = new ScanAnimation(MainActivity.this);
        rel.addView(ani.getContentView());
        ani.startAnimation();
    }
}
