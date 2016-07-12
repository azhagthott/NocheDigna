package com.zecovery.android.nochedigna.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.zecovery.android.nochedigna.R;

import java.util.Timer;
import java.util.TimerTask;

public class LaunchScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = LaunchScreenActivity.class.getName();

    private static final long SPLASH_SCREEN_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        ImageView imageViewZecoveryLogo = (ImageView) findViewById(R.id.imageViewZecoveryLogo);
        imageViewZecoveryLogo.setOnClickListener(this);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent i = new Intent(LaunchScreenActivity.this, PermissionCheckerActivity.class);
                startActivity(i);
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zecovery.com")));
    }
}
