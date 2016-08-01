package com.zecovery.android.nochedigna.about;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.base.BaseActivity;

public class AboutMActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_m);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
