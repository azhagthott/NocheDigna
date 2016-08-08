package com.zecovery.android.nochedigna.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.storage.StreamDownloadTask;
import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.base.BaseActivity;
import com.zecovery.android.nochedigna.data.FirebaseDataBaseHelper;
import com.zecovery.android.nochedigna.intro.IntroActivity;
import com.zecovery.android.nochedigna.login.LoginActivity;

import java.util.Timer;
import java.util.TimerTask;

public class LaunchScreenActivity extends AppCompatActivity {

    private static final long SPLASH_SCREEN_DELAY = 3000;
    private static final String LOG_TAG = LaunchScreenActivity.class.getName();

    private SharedPreferences preferences = null;
    private ProgressBar progressBarLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        ProgressBar progressBarLauncher = (ProgressBar) findViewById(R.id.progressBarLauncher);
        preferences = getSharedPreferences("com.zecovery.android.nochedigna", MODE_PRIVATE);
        final Boolean firstRun = preferences.getBoolean("first_run", true);

        /*Revisa si Google Play Services está instalado*/
        isGooglePlayServicesAvailable();


        try {
            //FirebaseDataBaseHelper firebaseDataBaseHelper = new FirebaseDataBaseHelper();
            //firebaseDataBaseHelper.getDataForLaunch(this);
            progressBarLauncher.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            //progressBarLauncher.setVisibility(View.GONE);
            //FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "NPE caught");
            //FirebaseCrash.report(e);
        }


        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                if (firstRun) {
                    startActivity(new Intent(LaunchScreenActivity.this, IntroActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(LaunchScreenActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences.getBoolean("first_run", true)) {
            preferences.edit().putBoolean("first_run", false).apply();
        }
    }

    public boolean isGooglePlayServicesAvailable() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }
}
