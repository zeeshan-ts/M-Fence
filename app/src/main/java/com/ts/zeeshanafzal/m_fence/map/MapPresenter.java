package com.ts.zeeshanafzal.m_fence.map;

import com.google.android.gms.maps.GoogleMap;

public class MapPresenter {
    MapListener mapListener;
    GoogleMap map;

    public MapPresenter(MapListener mapListener){
        this.mapListener=mapListener;
    }

    public void onMapReady(GoogleMap googleMap) {
        this.map=googleMap;
    }

    public void onAddFenceClicked() {

    }

    public void onClearFenceClicked() {

    }
}
