package com.zecovery.android.nochedigna.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.albergue.Albergue;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by francisco on 20-07-16.
 */

public class FirebaseDataBaseHelper {

    private static final String LOG_TAG = FirebaseDataBaseHelper.class.getName();

    //settings
    private ProgressDialog loading;

    public FirebaseDataBaseHelper() {
    }

    public List<Albergue> getDataFromFirebase(final GoogleMap map, final Context context) {

        loading = ProgressDialog.show(context,
                context.getResources().getString(R.string.loading_data_dialog_title),
                context.getResources().getString(R.string.loading_data_dialog_message),
                true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(context, context.getResources().getString(R.string.downloading_data_dialog_cancel),
                                Toast.LENGTH_LONG).show();
                    }
                });

        List<Albergue> list = new ArrayList<>();
        final LocalDataBaseHelper localDataBaseHelper = new LocalDataBaseHelper(context);

        String url = "http://alvaro.desa.exec.cl/moe/REST/noche_digna/json/noche-digna-export.json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(LOG_TAG, "onResponse: " + response);

                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "onErrorResponse: " + error);
                    }
                });




        // Conexion a Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("jsonRespuesta");

        // Detecta cambios en db de Firebase

        final List<Albergue> finalList = list;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                loading.dismiss();

                // Recoge los datos de cada albergue
                for (int i = 0; i < dataSnapshot.getChildrenCount(); i++) {

                    Log.d(LOG_TAG, "onDataChange: " + i);

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

                    // creo objeto albergue y lo agrego al arreglo
                    Albergue albergue = new Albergue(id, region, comuna, tipo, cobertura, camasDisponibles, ejecutor, direccion, telefonos, email, lat, lng);
                    finalList.add(albergue);

                    if (localDataBaseHelper.getAlbergue(Integer.valueOf(id)) == null) {
                        localDataBaseHelper.addAlbergue(albergue);
                    }

                    double latitude = Double.valueOf(finalList.get(i).getLat());
                    double longitude = Double.valueOf(finalList.get(i).getLng());

                    // Vaido la cantidad de camas de los albergues
                    // si es mayor a cero, se dibuja verde
                    /*if (latitude != 0 || longitude != 0) {

                        if (Integer.valueOf(camasDisponibles) > 0) {
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude))
                                    .title(ejecutor)
                                    .snippet(id)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            );
                            // si no, se dibuja rojo
                        } else {
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude))
                                    .title(ejecutor)
                                    .snippet(id)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            );
                        }
                    }*/
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // En caso de error lo envio a Firebase
                FirebaseCrash.log("DATABASE ERROR: " + databaseError);
                Log.d(LOG_TAG, "databaseError: " + databaseError);
                loading.dismiss();
            }
        });

        return list;
    }

    public List<Albergue> getDataForLaunch(Context context) {

        List<Albergue> list = new ArrayList<>();
        final LocalDataBaseHelper localDataBaseHelper = new LocalDataBaseHelper(context);

        // Conexion a Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("jsonRespuesta");

        // Detecta cambios en db de Firebase

        final List<Albergue> finalList = list;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Recoge los datos de cada albergue
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

                    // creo objeto albergue y lo agrego al arreglo
                    Albergue albergue = new Albergue(id, region, comuna, tipo, cobertura, camasDisponibles, ejecutor, direccion, telefonos, email, lat, lng);
                    finalList.add(albergue);

                    if (localDataBaseHelper.getAlbergue(Integer.valueOf(id)) == null) {
                        localDataBaseHelper.addAlbergue(albergue);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // En caso de error lo envio a Firebase
                FirebaseCrash.log("DATABASE ERROR: " + databaseError);
            }
        });

        return list;
    }
}

