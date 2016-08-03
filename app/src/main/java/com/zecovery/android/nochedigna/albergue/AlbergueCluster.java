package com.zecovery.android.nochedigna.albergue;

/**
 * Created by fran on 10-07-16.
 */

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class AlbergueCluster implements ClusterItem {

    private final LatLng mPosition;

    public AlbergueCluster(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
