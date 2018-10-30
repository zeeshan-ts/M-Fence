package com.ts.zeeshanafzal.m_fence.map;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class MapPresenter {
    MapListener mapListener;
    GoogleMap map;
    Location currLocation = null;
    LatLng geoFenceLatLng = null;

    public MapPresenter(MapListener mapListener) {
        this.mapListener = mapListener;
    }

    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
    }

    public void onAddFenceClicked() {
        if (geoFenceLatLng != null) {
            mapListener.addGeoFence(geoFenceLatLng);
        } else {
            mapListener.showSnackbar("Please add geo fence marker first by tapping on map");
        }
    }

    public void onClearFenceClicked() {
        geoFenceLatLng = null;
        mapListener.clearFences();
        mapListener.zoomCamera(0);
    }

    public void onLocationChanged(Location currLocation) {
        if (currLocation == null)
            return;

        this.currLocation = currLocation;

        mapListener.setTVLocation(currLocation.getLatitude(), currLocation.getLongitude());
        mapListener.showMarkerAt(currLocation);
//        mapListener.moveCameraTo(currLocation);
    }

    public void onStart() {
        mapListener.startLocationUpdates();
    }

    public void onStop() {
        mapListener.stopLocationUpdates();
    }

    public void onMapClicked(LatLng latLng) {
        if (geoFenceLatLng == null) {
            this.geoFenceLatLng = latLng;
            mapListener.showGeoFenceMarkerAt(latLng);
        }else {
            mapListener.showSnackbar("Clear fence to change position");
        }
    }

    public void onFenceAdded(LatLng latLng) {
        mapListener.addCircleAt(latLng);
        mapListener.moveCameraTo(latLng);
        mapListener.zoomCamera(14f);
    }
}
