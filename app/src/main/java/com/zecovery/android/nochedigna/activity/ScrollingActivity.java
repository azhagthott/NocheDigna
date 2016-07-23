package com.zecovery.android.nochedigna.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.albergue.Albergue;
import com.zecovery.android.nochedigna.data.LocalDataBaseHelper;

public class ScrollingActivity extends AppCompatActivity {

    private static final String LOG_TAG = ScrollingActivity.class.getName();
    private static final int PERMISSION_REQUEST_CALL = 1;

    private TextView textViewTipo;
    private TextView textViewCobertura;
    private TextView textViewCamasDisponibles;
    private TextView textViewDireccion;
    private TextView textViewTelefono;
    private TextView textViewEmail;
    private TextView textViewEjecutor;

    private Albergue albergue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarsScrolling);
        setSupportActionBar(toolbar);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.setBackground(getResources().getDrawable(R.drawable.albergue_1));

        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        LocalDataBaseHelper localDataBaseHelper = new LocalDataBaseHelper(this);

        textViewTipo = (TextView) findViewById(R.id.textViewTipo);
        textViewCobertura = (TextView) findViewById(R.id.textViewCobertura);
        textViewCamasDisponibles = (TextView) findViewById(R.id.textViewCamasDisponibles);
        textViewDireccion = (TextView) findViewById(R.id.textViewDireccion);
        textViewTelefono = (TextView) findViewById(R.id.textViewTelefono);
        textViewEmail = (TextView) findViewById(R.id.textViewEmail);
        textViewEjecutor = (TextView) findViewById(R.id.textViewEjecutor);

        Bundle extra = getIntent().getExtras();

        if (extra != null) {

            String idAlbergue = extra.getString("ID_ALBERGUE");

            albergue = localDataBaseHelper.getAlbergue(Integer.valueOf(idAlbergue));

            textViewTipo.setText(albergue.getTipo());
            textViewCobertura.setText(albergue.getCobertura());
            textViewCamasDisponibles.setText(albergue.getCamasDisponibles());
            textViewEjecutor.setText(albergue.getEjecutor());

            if (Integer.valueOf(albergue.getCamasDisponibles()) > 0) {
                textViewCamasDisponibles.setTextColor(getResources().getColor(R.color.green_400));
            } else {
                textViewCamasDisponibles.setTextColor(getResources().getColor(R.color.red_400));
            }

            textViewDireccion.setText(albergue.getDireccion() + ", " + albergue.getComuna() + ", " + albergue.getRegion());
            textViewTelefono.setText(albergue.getTelefonos());
            textViewEmail.setText(albergue.getEmail());
            toolbarLayout.setTitle(albergue.getEjecutor());
        }

        FloatingActionButton fabCall = (FloatingActionButton) findViewById(R.id.fabCall);
        fabCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getResources().getString(R.string.make_call), Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.confirm_call), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                callIt();
                            }
                        })
                        .setDuration(Snackbar.LENGTH_LONG)
                        .setActionTextColor(getResources().getColor(R.color.red_400))
                        .show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void callIt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(ScrollingActivity.this, Manifest.permission.CALL_PHONE)) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_call_phone_require), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_call_phone_require), Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(ScrollingActivity.this,
                            new String[]{
                                    Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_CALL);
                }
            } else {
                try {
                    String phone = "+56225223830";
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phone));
                    startActivity(callIntent);
                } catch (Exception e) {
                    Toast.makeText(ScrollingActivity.this,
                            getResources().getString(R.string.error_calling), Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "Exception: " + e);
                    FirebaseCrash.log("Exception" + e);
                }
            }
        } else {
            try {
                String phone = "+56225223830";
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phone));
                startActivity(callIntent);
            } catch (Exception e) {
                Toast.makeText(ScrollingActivity.this,
                        getResources().getString(R.string.error_calling), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Exception: " + e);
                FirebaseCrash.log("Exception" + e);
            }
        }
    }
}
