package com.zecovery.android.nochedigna.intro;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.login.LoginActivity;

/**
 * Created by francisco on 23-07-16.
 */

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(new IntroFragment1());
        addSlide(new IntroFragment2()); // Location
        addSlide(new IntroFragment3()); // Call
        addSlide(new IntroFragment4()); // Share

        askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        askForPermissions(new String[]{Manifest.permission.CALL_PHONE}, 3);
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);

        showStatusBar(false);
        setFadeAnimation(); // OR

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
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        loadMainActivity();
        Toast.makeText(getApplicationContext(), getString(R.string.app_intro_skip), Toast.LENGTH_SHORT).show();
    }

    public void getStarted(View v) {
        loadMainActivity();
    }
}
