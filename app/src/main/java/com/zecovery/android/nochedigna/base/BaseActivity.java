package com.zecovery.android.nochedigna.base;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by francisco on 26-07-16.
 */

public class BaseActivity extends AppCompatActivity{

    public FirebaseAnalytics mFirebaseAnalytics;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);



    }
}
