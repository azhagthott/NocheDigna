package com.zecovery.android.nochedigna.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zecovery.android.nochedigna.R;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_LOGIN = "pref_key_login";
    public static final String KEY_PREF_DATA = "pref_key_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(KEY_PREF_LOGIN)) {
            Preference login = findPreference(key);
            login.setDefaultValue(sharedPreferences.getBoolean(KEY_PREF_LOGIN, true));
        }

        if (key.equals(KEY_PREF_DATA)) {
            Preference login = findPreference(key);
            login.setDefaultValue(sharedPreferences.getBoolean(KEY_PREF_DATA, true));
        }
    }
}
