package com.zecovery.android.nochedigna.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.crash.FirebaseCrash;
import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.albergue.Albergue;
import com.zecovery.android.nochedigna.data.FirebaseDataBaseHelper;
import com.zecovery.android.nochedigna.data.LocalDataBaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    //FragmentActivity

    // TAG para debug ejm: Log.d(LOG_TAG, "onMarkerClick: marker " + marker.getSnippet());
    private static final String LOG_TAG = MapsActivity.class.getName();

    // valor del permiso aceptado
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;

    // Tiempo para matar la app
    private static final long AUTO_DESTROY = 3000;

    // Elementos UI
    private FloatingActionButton fabMapsSharing;
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
    private LocalDataBaseHelper localDataBaseHelper;
    private FirebaseDataBaseHelper firebaseDataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //DB - Llamo a la db
        localDataBaseHelper = new LocalDataBaseHelper(this);
        firebaseDataBaseHelper = new FirebaseDataBaseHelper();

        // UI - boton y barra de carga en el mapa
        fabMapsSharing = (FloatingActionButton) findViewById(R.id.fabMapsSharing);
        totalAlbergues = (TextView) findViewById(R.id.totalAlbergues);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // agrega fragment del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Instacia GoogleApiClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Se habilita la opcion de compartir solo una vez que se ha cargado el mapa
        fabMapsSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareIt();
            }
        });

        if (localDataBaseHelper != null) {
            totalAlbergues.setText(String.valueOf(localDataBaseHelper.countAlbergues()));
        }
    }

    /*
    * Comparte imagen que se guarda como: temporary_file.jpg, enn : file://sdcard/
    * Texto del que va en el mensaje: shareBody
    *
    * */
    private void shareIt() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE_LOCATION);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_write_external_stroge_require), Toast.LENGTH_LONG).show();

                }
            } else {

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                double lat = mLastLocation.getLatitude();
                double lng = mLastLocation.getLongitude();

                String location = " http://maps.google.com/maps?q=loc:" + lat / 1E6 + "," + lng / 1E6;

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

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();
            String location = " http://maps.google.com/maps?q=loc:" + lat / 1E6 + "," + lng / 1E6;

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
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Llama a SettingsActivity
        if (id == R.id.action_settings) {
            startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // creo objeto mapa
        mMap = googleMap;

        // Tipo de mapa = Normal
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_CODE_LOCATION);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require), Toast.LENGTH_LONG).show();
                }
            } else {
                mMap.setMyLocationEnabled(true);
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        } else {
            mMap.setMyLocationEnabled(true);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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
                Log.d(LOG_TAG, "onMarkerClick: marker " + marker.getSnippet());

                // Llamo ScrollinActivity
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
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_CODE_LOCATION);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_require), Toast.LENGTH_LONG).show();
                }
            } else {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                    mMap.animateCamera(cameraUpdate);
                }
            }
        } else {
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
