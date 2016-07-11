package com.zecovery.android.nochedigna;

import android.Manifest;
import android.app.Activity;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zecovery.android.nochedigna.albergue.Albergue;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final String LOG_TAG = MapsActivity.class.getName();
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;

    private GoogleMap mMap;
    private String provider;
    private LocationManager locationManager;
    private Location location;
    private LatLng mLatLng;
    private double latitude;
    private double longitude;
    private Albergue albergue;
    private ArrayList<Albergue> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_CODE_LOCATION);

                } else {
                    Toast.makeText(MapsActivity.this, "GPS requerido", Toast.LENGTH_SHORT).show();
                }
            }
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);//true if required
        criteria.setBearingRequired(false);//true if required
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        provider = locationManager.getBestProvider(criteria, true);

        location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        if (location != null) {
            Log.d(LOG_TAG, "Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            Log.d(LOG_TAG, "No provider has been selected.");
        }
        //setUpMapIfNeeded();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocationData();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

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

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title(direccionMarker)
                            .snippet(idAlbergue)
                    );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "databaseError: " + databaseError);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_CODE_LOCATION);
                } else {
                    Toast.makeText(MapsActivity.this, "GPS requerido", Toast.LENGTH_SHORT).show();
                }
            }
        }

        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        try {
            mLatLng = new LatLng(location.getLatitude(), location.getAltitude());
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception: " + e);
        }

        // UI Config
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        if (mLatLng != null) {

            mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude, longitude), 17.0f));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0, 0)));
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(0, 0), 17.0f));
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Log.d(LOG_TAG, "onMarkerClick: marker " + marker.getSnippet());

                Intent intent = new Intent(MapsActivity.this, ScrollingActivity.class);
                intent.putExtra("ID_ALBERGUE", marker.getSnippet());
                startActivity(intent);
                return false;
            }
        });

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

    public void requestPermission(String strPermission, int perCode, Context _c, Activity _a) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(_a, strPermission)) {
            Toast.makeText(this,
                    "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(_a, new String[]{strPermission}, perCode);
        }
    }

    private void fetchLocationData() {
        //code to use the granted permission (location)
    }
}
