package com.zecovery.android.nochedigna.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.login.LoginActivity;

import java.util.Timer;
import java.util.TimerTask;

public class PermissionCheckerActivity extends AppCompatActivity implements LocationListener {

    private static final String LOG_TAG = PermissionCheckerActivity.class.getName();

    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
    private static final long AUTO_DESTROY = 3000;

    private LocationManager locationManager;
    private Location location;
    private String provider;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_checker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkPermissionForM();

    }

    private void checkPermissionForM() {

        //Reviso los permisos para versiones mayores o iguales a M (Marshmallow - 6.0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //Pregunto si estos permisos fueron aceptados (uso de GPS)
            //<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
            //<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
            if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                    ContextCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                //Si no han sido aceptados, entonces los sugieros
                if (ActivityCompat.shouldShowRequestPermissionRationale(PermissionCheckerActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(PermissionCheckerActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    ActivityCompat.requestPermissions(PermissionCheckerActivity.this,
                            new String[]{
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_CODE_LOCATION);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                gotoMap();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE_LOCATION:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationListener mListener;
                    fetchLocationData();
                    gotoMap();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require),
                            Toast.LENGTH_LONG).show();

                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            finish();
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(task, AUTO_DESTROY);
                }
                break;
        }
    }

    private void gotoMap() {

        // Inicia Activity principal (mapa)
        Intent i = new Intent(PermissionCheckerActivity.this, LoginActivity.class);
        i.putExtra("CURRENT_LATITUDE", latitude);
        i.putExtra("CURRENT_LONGITUDE", longitude);
        startActivity(i);
        finish();
    }

    private void fetchLocationData() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);//true if required
        criteria.setBearingRequired(false);//true if required
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        provider = locationManager.getBestProvider(criteria, true);

        try {
            location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (SecurityException sec) {
            Log.d(LOG_TAG, "SecurityException: " + sec);
        }

        if (location != null) {
            Log.d(LOG_TAG, "Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            Log.d(LOG_TAG, "No provider has been selected.");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
