package com.zecovery.android.nochedigna.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.crash.FirebaseCrash;
import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.about.AboutMActivity;
import com.zecovery.android.nochedigna.about.AboutZeActivity;
import com.zecovery.android.nochedigna.albergue.Albergue;
import com.zecovery.android.nochedigna.data.FirebaseDataBaseHelper;
import com.zecovery.android.nochedigna.intro.IntroActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    // TAG para debug ejm: Log.d(LOG_TAG, "onMarkerClick: marker " + marker.getSnippet());
    private static final String LOG_TAG = MapsActivity.class.getName();

    // valor del permiso aceptado
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_CALL = 1;

    private static final String CALL_CENTER_PHONE_NUMBER = "800104777";

    // Tiempo para matar la app
    private static final long AUTO_DESTROY = 3000;

    // Elementos UI
    private FloatingActionsMenu fabMaps;
    private FloatingActionButton fabMapsShare;
    private FloatingActionButton fabMapsCall;

    private ProgressBar mProgressBar;
    private TextView totalAlbergues;
    private int mProgressStatus = 0;

    // Elementos del mapa
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    // Manejo de ubicacion del usuario
    private LatLng mLatLng;
    private double currentLatitude;
    private double currentLongitude;

    // Ultima ubicacion conocida
    protected Location mLastLocation;

    // Albergue
    private Albergue albergue;
    private ArrayList<Albergue> arrayList;

    // data base
    private FirebaseDataBaseHelper firebaseDataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //DB - Llamo a la db
        firebaseDataBaseHelper = new FirebaseDataBaseHelper();

        // Instacia GoogleApiClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            // UI - boton y barra de carga en el mapa
            fabMaps = (FloatingActionsMenu) findViewById(R.id.fabMapsMenu);
            fabMapsShare = (FloatingActionButton) findViewById(R.id.fabShare);
            fabMapsCall = (FloatingActionButton) findViewById(R.id.fabCall);

            totalAlbergues = (TextView) findViewById(R.id.totalAlbergues);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

            // agrega fragment del mapa
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Se habilita la opcion de compartir solo una vez que se ha cargado el mapa
        fabMapsShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareIt();
            }
        });

        // Se habilita la opcion de compartir solo una vez que se ha cargado el mapa
        fabMapsCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callIt();
            }
        });

        if (!isGPSEnabled(this)) {
            alerDialog(this, "GPS");
            Log.d(LOG_TAG, "isGPSEnabled: false");
        } else {
            Log.d(LOG_TAG, "isGPSEnabled: true");
        }
    }

    private boolean isNetworkEnabled(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private boolean isGPSEnabled(Context c) {
        LocationManager lm = (LocationManager) c.getSystemService(c.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private AlertDialog.Builder alerDialog(final Context c, String service) {

        AlertDialog.Builder alert = new AlertDialog.Builder(c);

        if (service.equals("GPS")) {
            alert.setIcon(getResources().getDrawable(R.drawable.ic_location_disabled));
            alert.setTitle(getResources().getString(R.string.alert_gps_title));
            alert.setMessage(getResources().getString(R.string.alert_gps_message));
            alert.setPositiveButton(getResources().getString(R.string.alert_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    c.startActivity(intent);
                }
            });

            alert.setNegativeButton(getResources().getString(R.string.alert_negative), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FirebaseCrash.log("usuario no acepta prender GPS");
                }
            });
            alert.show();
        } else if (service.equals("NETWORK")) {
            alert.setIcon(getResources().getDrawable(R.drawable.ic_signal_cellular_off));
            alert.setTitle(getResources().getString(R.string.alert_network_title));
            alert.setMessage(getResources().getString(R.string.alert_network_message));
            alert.setPositiveButton(getResources().getString(R.string.alert_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    c.startActivity(intent);
                }
            });

            alert.setNegativeButton(getResources().getString(R.string.alert_negative), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FirebaseCrash.log("usuario no acepta prender red de datos");
                }
            });
            alert.show();
        }
        return alert;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // creo objeto mapa
        mMap = googleMap;

        // Tipo de mapa = Normal
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_CODE_LOCATION);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require), Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require), Toast.LENGTH_LONG).show();
                }
            } else {
                mMap.setMyLocationEnabled(true);
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatLng, 8.0f);
                    mMap.animateCamera(cameraUpdate);
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f));
                }
            }
        } else {
            mMap.setMyLocationEnabled(true);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatLng, 8.0f);
                mMap.animateCamera(cameraUpdate);
//                mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f));

            }
        }

        // deshabilito herramientas
        mMap.getUiSettings().setMapToolbarEnabled(false);
        // habilito multitouch y otras funciones
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // Traigo los datos desde Firebase
        firebaseDataBaseHelper.getDataFromFirebase(mMap, this);

        // Muestra detalles de los albergues al presionar en el infoWindows
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                // Llamo ScrollinActivity
                Intent intent = new Intent(MapsActivity.this, ScrollingActivity.class);
                intent.putExtra("ID_ALBERGUE", marker.getSnippet());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if (mLastLocation != null) {
                        mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatLng, 8.0f);
                        mMap.animateCamera(cameraUpdate);
//                        mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f));
                    }

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require), Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    /**
     * Realiza llamado a call center
     */
    private void callIt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.CALL_PHONE)) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_call_phone_require), Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_CALL);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_call_phone_require), Toast.LENGTH_LONG).show();
                }
            } else {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + CALL_CENTER_PHONE_NUMBER));
                    startActivity(callIntent);
                } catch (Exception e) {
                    Toast.makeText(MapsActivity.this,
                            getResources().getString(R.string.error_calling), Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "Exception: " + e);
                    FirebaseCrash.log("Exception" + e);
                }
            }
        } else {
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + CALL_CENTER_PHONE_NUMBER));
                startActivity(callIntent);
            } catch (Exception e) {
                Toast.makeText(MapsActivity.this,
                        getResources().getString(R.string.error_calling), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Exception: " + e);
                FirebaseCrash.log("Exception" + e);
            }
        }
    }

    /**
     * Comparte imagen que se guarda como: temporary_file.jpg, enn : file://sdcard/
     * Texto del que va en el mensaje: shareBody
     */
    private void shareIt() {

        String location;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_write_external_stroge_require), Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE_LOCATION);

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_write_external_stroge_require), Toast.LENGTH_LONG).show();
                }
            } else {

                try {

                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    currentLatitude = mLastLocation.getLatitude();
                    currentLongitude = mLastLocation.getLongitude();
                    location = " https://www.google.com/maps/@" + currentLatitude + "," + currentLongitude + ",18z";

                } catch (Exception e) {
                    Log.d(LOG_TAG, "Exception: " + e);

                    location = "";

                }

                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.drawable.albergue_1);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                File f = new File(Environment.getExternalStorageDirectory()
                        + File.separator + "temporary_file.jpg");
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String shareBody = "" +
                        "\n" + //Salto de linea para que el usuario agregue texto si lo desea
                        "#NocheDigna " +
                        location +
                        "\nZecovery - http://www.zecovery.com" +
                        // Modificar url con la del play store para descargar la app
                        " \nhttps://www.google.cl(url para descargar app)";
                // se usa cuando se comparte por correo electronico
                String shareSubject = "Programa Noche Digna";

                share.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
                share.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
                startActivity(Intent.createChooser(share, "Compartir"));
            }

        } else {

            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                currentLatitude = mLastLocation.getLatitude();
                currentLongitude = mLastLocation.getLongitude();
                location = " https://www.google.com/maps/@" + currentLatitude + "," + currentLongitude + ",18z";

            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception: " + e);
                location = "";
            }

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.albergue_1);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            File f = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "temporary_file.jpg");
            try {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }

            String shareBody = "" +
                    "\n" + //Salto de linea para que el usuario agregue texto si lo desea
                    "#NocheDigna " +
                    "\n\n" +
                    location +
                    "\nZecovery - http://www.zecovery.com" +
                    // Modificar url con la del play store para descargar la app
                    " \n";
            // se usa cuando se comparte por correo electronico
            String shareSubject = "Programa Noche Digna";

            share.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
            share.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
            startActivity(Intent.createChooser(share, "Compartir"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Llama a SettingsActivity
        if (id == R.id.action_settings) {
            startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
        }

        if (id == R.id.action_intro) {
            startActivity(new Intent(MapsActivity.this, IntroActivity.class));
            finish();
        }

        if (id == R.id.about) {
            startActivity(new Intent(MapsActivity.this, AboutMActivity.class));
        }

        if (id == R.id.about_zecovery) {
            startActivity(new Intent(MapsActivity.this, AboutZeActivity.class));
        }

        if (id == R.id.resync) {
            FirebaseDataBaseHelper firebaseDataBaseHelper = new FirebaseDataBaseHelper();
            firebaseDataBaseHelper.getDataForLaunch(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require), Toast.LENGTH_LONG).show();

                } else {

                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_CODE_LOCATION);
                }
            } else {
                mMap.setMyLocationEnabled(true);
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                    mMap.animateCamera(cameraUpdate);
                }
            }
        } else {

            mMap.setMyLocationEnabled(true);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                mMap.animateCamera(cameraUpdate);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Mando WARNING a Firebase
        FirebaseCrash.log("WARNING - onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Mando ERROR a Firebase
        FirebaseCrash.log("ERROR - onConnectionFailed: " + connectionResult);
    }
}