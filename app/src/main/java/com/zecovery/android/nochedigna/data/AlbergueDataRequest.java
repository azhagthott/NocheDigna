package com.zecovery.android.nochedigna.data;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.crash.FirebaseCrash;
import com.zecovery.android.nochedigna.albergue.Albergue;
import com.zecovery.android.nochedigna.base.BaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by francisco on 17-08-16.
 */

public class AlbergueDataRequest extends BaseActivity {

    private JSONObject jsonObject;
    private JSONArray response;
    private Albergue albergue;

    public AlbergueDataRequest(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public void getDataForMap(GoogleMap map, Context context) {

        LocalDataBaseHelper localDataBaseHelper = new LocalDataBaseHelper(context);

        try {
            response = jsonObject.getJSONArray("response");

            for (int i = 0; i < response.length(); i++) {

                albergue = new Albergue();

                String id = response.getJSONObject(i).getString("id_albergue");
                String ejecutor = response.getJSONObject(i).getString("ejecutor");
                String tipo = response.getJSONObject(i).getString("tipo");
                String cobertura = response.getJSONObject(i).getString("cobertura");
                String camasDisponibles = response.getJSONObject(i).getString("camas_disponibles");
                String region = response.getJSONObject(i).getString("region");
                String comuna = response.getJSONObject(i).getString("comuna");
                String lat = response.getJSONObject(i).getString("lat");
                String lng = response.getJSONObject(i).getString("lng");
                String direccion = response.getJSONObject(i).getString("direccion");
                String telefonos = response.getJSONObject(i).getString("telefonos");
                String email = response.getJSONObject(i).getString("email");

                map.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                Double.valueOf(response.getJSONObject(i).getString("lat")),
                                Double.valueOf(response.getJSONObject(i).getString("lng"))))
                        .title(ejecutor)
                        .snippet(id)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                albergue = new Albergue(id, region, comuna, tipo, cobertura, camasDisponibles, ejecutor, direccion, telefonos, email, lat, lng);

                if (localDataBaseHelper.getAlbergue(Integer.valueOf(id)) == null) {
                    localDataBaseHelper.addAlbergue(albergue);
                }
            }
        } catch (Exception e) {
            FirebaseCrash.log("Exception-getDataForMap: " + e);
        }
    }
}
