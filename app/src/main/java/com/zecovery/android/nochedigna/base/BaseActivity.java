package com.zecovery.android.nochedigna.base;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

import com.google.firebase.analytics.FirebaseAnalytics;


/**
 * Created by francisco on 26-07-16.
 */

public class BaseActivity extends AppCompatActivity {

    public FirebaseAnalytics mFirebaseAnalytics;
    private Context context;
    public static final String TAG = "log: ";

    public static final int PORTRAIT_MODE = 1;
    public static final int LANDSCAPE_MODE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public int getScreenOrientation(Activity activity) {

        Display getOrient = activity.getWindowManager().getDefaultDisplay();
        int orientation;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }
}
