package com.ts.zeeshanafzal.m_fence.map;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;

public class MapPresenter {
    MapListener mapListener;
    GoogleMap map;
    Location currLocation = null;

    public MapPresenter(MapListener mapListener) {
        this.mapListener = mapListener;
    }

    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
    }

    public void onAddFenceClicked() {

    }

    public void onClearFenceClicked() {

    }

    public void onLocationChanged(Location currLocation) {
        if (currLocation == null)
            return;

        this.currLocation = currLocation;

        mapListener.setTVLocation(currLocation.getLatitude(), currLocation.getLongitude());
    }

    public void onStart() {
        mapListener.startLocationUpdates();
    }

    public void onStop() {
        mapListener.stopLocationUpdates();
    }
}
