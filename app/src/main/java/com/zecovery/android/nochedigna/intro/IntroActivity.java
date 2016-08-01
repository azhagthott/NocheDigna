package com.zecovery.android.nochedigna.intro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.login.LoginActivity;

/**
 * Created by francisco on 23-07-16.
 */

public class IntroActivity extends AppIntro {

    private static final String LOG_TAG = IntroActivity.class.getName();
    private FirebaseAnalytics mFirebaseAnalytics;
    private String id = "2";
    private String name = "intro App";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        addSlide(new IntroFragment1()); // Init
        addSlide(new IntroFragment2()); // Location
        addSlide(new IntroFragment3()); // Call
        addSlide(new IntroFragment4()); // Share
        addSlide(new IntroFragment5()); // Done

        showSkipButton(false);
        setSwipeLock(true);
        showStatusBar(false);
        setDepthAnimation();
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        loadMainActivity();
        finish();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(IntroActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            }
            if (ContextCompat.checkSelfPermission(IntroActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                askForPermissions(new String[]{Manifest.permission.CALL_PHONE}, 3);
            }
            if (ContextCompat.checkSelfPermission(IntroActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
            }
        }
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        mFirebaseAnalytics.logEvent("INTRO SKIPPED", null);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        loadMainActivity();
        Toast.makeText(getApplicationContext(), getString(R.string.app_intro_skip), Toast.LENGTH_SHORT).show();
    }

    public void getStarted(View v) {
        loadMainActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
