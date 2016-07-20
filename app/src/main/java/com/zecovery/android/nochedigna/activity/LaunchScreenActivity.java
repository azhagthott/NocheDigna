package com.zecovery.android.nochedigna.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.albergue.Albergue;
import com.zecovery.android.nochedigna.data.LocalDataBaseHelper;
import com.zecovery.android.nochedigna.login.LoginActivity;

import java.util.Timer;
import java.util.TimerTask;

public class LaunchScreenActivity extends AppCompatActivity implements View.OnClickListener {

    // Tiempo en milisegundos que se muestra el LaunchScreen
    private static final long SPLASH_SCREEN_DELAY = 1000;
    private static final String TAG = "db: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);



        // Logo de Zecovery
        ImageView imageViewZecoveryLogo = (ImageView) findViewById(R.id.imageViewZecoveryLogo);
        // Se puede cambiar setOnClickListener -> setOnLongClickListener
        imageViewZecoveryLogo.setOnClickListener(this);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(LaunchScreenActivity.this, LoginActivity.class));
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    @Override
    public void onClick(View view) {
        //OPCIONAL: Si el usuario toca el logo de Zecovery lo lleva a la pagina
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zecovery.com")));
    }
}
