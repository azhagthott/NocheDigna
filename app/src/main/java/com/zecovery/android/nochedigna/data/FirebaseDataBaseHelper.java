package com.zecovery.android.nochedigna.data;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zecovery.android.nochedigna.albergue.Albergue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco on 20-07-16.
 */

public class FirebaseDataBaseHelper {

    private static final String LOG_TAG = "";

    public FirebaseDataBaseHelper() {
    }

    public List<Albergue> getDataFromFirebase(final GoogleMap map, Context context) {

        final ArrayList<Albergue> list = new ArrayList<>();
        final LocalDataBaseHelper localDataBaseHelper = new LocalDataBaseHelper(context);

        // Conexion a Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("jsonRespuesta");

        // Detecta cambios en db de Firebase
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
                    list.add(albergue);

                    if (localDataBaseHelper.getAlbergue(Integer.valueOf(id)) == null) {
                        localDataBaseHelper.addAlbergue(albergue);
                    }

                    double latitude = Double.valueOf(list.get(i).getLat());
                    double longitude = Double.valueOf(list.get(i).getLng());

                    String direccionMarker = list.get(i).getDireccion();
                    String idAlbergue = list.get(i).getIdAlbergue();

                    // Vaido la cantidad de camas de los albergues
                    // si es mayor a cero, se dibuja verde
                    if (Integer.valueOf(camasDisponibles) > 0) {
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(direccionMarker)
                                .snippet(idAlbergue)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        );
                        // si no, se dibuja rojo
                    } else {
                        map.addMarker(new MarkerOptions()
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
                // En caso de error lo envio a Firebase
                FirebaseCrash.log("DATABASE ERROR: " + databaseError);
                Log.d(LOG_TAG, "databaseError: " + databaseError);
            }
        });

        return list;
    }
}