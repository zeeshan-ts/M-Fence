package com.ts.zeeshanafzal.m_fence.map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

interface MapListener {
    void setTVLocation(double latitude, double longitude);

    void startLocationUpdates();

    void stopLocationUpdates();

    void showMarkerAt(Location location);

    void moveCameraTo(LatLng latLng);

    void showGeoFenceMarkerAt(LatLng latLng);

    void addGeoFence(LatLng geoFenceLatLng);

    void showSnackbar(String msg);

    void addCircleAt(LatLng latLng);

    void clearFences();

    void zoomCamera(float v);

    void startPlacesSearchActivity();
}
