package com.zecovery.android.nochedigna.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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

public class LaunchScreenActivity extends BaseActivity implements View.OnClickListener {

    private static final long SPLASH_SCREEN_DELAY = 1000;
    private static final String LOG_TAG = LaunchScreenActivity.class.getName();

    private SharedPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        try {
            FirebaseDataBaseHelper firebaseDataBaseHelper = new FirebaseDataBaseHelper();
            firebaseDataBaseHelper.getDataForLaunch(this);
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "NPE caught");
            FirebaseCrash.report(e);
        }

        preferences = getSharedPreferences("com.zecovery.android.nochedigna", MODE_PRIVATE);
        final Boolean firstRun = preferences.getBoolean("first_run", true);

        ImageView imageViewZecoveryLogo = (ImageView) findViewById(R.id.imageViewZecoveryLogo);
        imageViewZecoveryLogo.setOnClickListener(this);

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

    @Override
    public void onClick(View view) {

        String id = "1";
        String name = "click en logo Ze";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zecovery.com"));
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        startActivity(intent);
    }
}
