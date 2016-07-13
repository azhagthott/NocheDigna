package com.zecovery.android.nochedigna.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.albergue.Albergue;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    private static final String LOG_TAG = MapsActivity.class.getName();
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
    private static final long AUTO_DESTROY = 3000;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private LatLng mLatLng;
    private double currentLatitude;
    private double currentLongitude;
    private Albergue albergue;
    private ArrayList<Albergue> arrayList;
    protected Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            currentLatitude = extras.getDouble("CURRENT_LATITUDE");
            currentLongitude = extras.getDouble("CURRENT_LONGITUDE");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getDataFromFirebase();

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_CODE_LOCATION);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        //mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d(LOG_TAG, "onMarkerClick: marker " + marker.getSnippet());

                Intent intent = new Intent(MapsActivity.this, ScrollingActivity.class);
                intent.putExtra("ID_ALBERGUE", marker.getSnippet());
                startActivity(intent);
            }
        });
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_CODE_LOCATION);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            mMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        FirebaseCrash.log("ERROR - onConnectionFailed: " + connectionResult);
    }

    private void getDataFromFirebase() {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("jsonRespuesta");

        arrayList = new ArrayList<>();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (int i = 0; i < dataSnapshot.getChildrenCount(); i++) {

                    String id = dataSnapshot.child("" + i + "").child("idAlbergue").getValue().toString();
                    String region = dataSnapshot.child("" + i + "").child("region").getValue().toString();
                    String comuna = dataSnapshot.child("" + i + "").child("comuna").getValue().toString();
                    String tipo = dataSnapshot.child("" + i + "").child("tipo").getValue().toString();
                    String cobertura = dataSnapshot.child("" + i + "").child("cobertura").getValue().toString();
                    String camasDisponibles = dataSnapshot.child("" + i + "").child("camasDisponibles").getValue().toString();
                    String ejecutor = dataSnapshot.child("" + i + "").child("ejecutor").getValue().toString();
                    String direccion = dataSnapshot.child("" + i + "").child("direccion").getValue().toString();
                    String telefonos = dataSnapshot.child("" + i + "").child("telefonos").getValue().toString();
                    String email = dataSnapshot.child("" + i + "").child("email").getValue().toString();
                    String lat = dataSnapshot.child("" + i + "").child("lat").getValue().toString();
                    String lng = dataSnapshot.child("" + i + "").child("lng").getValue().toString();

                    albergue = new Albergue(id, region, comuna, tipo, cobertura, camasDisponibles, ejecutor, direccion, telefonos, email, lat, lng);
                    arrayList.add(albergue);

                    double latitude = Double.valueOf(arrayList.get(i).getLat());
                    double longitude = Double.valueOf(arrayList.get(i).getLng());
                    String direccionMarker = arrayList.get(i).getDireccion();
                    String idAlbergue = arrayList.get(i).getIdAlbergue();

                    if (Integer.valueOf(camasDisponibles) > 0) {
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(direccionMarker)
                                .snippet(idAlbergue)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        );
                    } else {
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(direccionMarker)
                                .snippet(idAlbergue)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        );
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "databaseError: " + databaseError);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_LOCATION:

                if (grantResults.length == 1 &&
                        permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try {
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        Log.d(LOG_TAG, "Exception: " + e);
                    }

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
}
