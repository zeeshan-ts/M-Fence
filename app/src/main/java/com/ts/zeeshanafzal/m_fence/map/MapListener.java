package com.ts.zeeshanafzal.m_fence.map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

interface MapListener {
    void setTVLocation(double latitude, double longitude);

    void startLocationUpdates();

    void stopLocationUpdates();

    void showMarkerAt(Location location);

    void moveCameraTo(Location location);

    void showGeoFenceMarkerAt(LatLng latLng);
}
