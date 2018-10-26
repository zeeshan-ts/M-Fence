package com.ts.zeeshanafzal.m_fence.map;

interface MapListener {
    void setTVLocation(double latitude, double longitude);

    void startLocationUpdates();

    void stopLocationUpdates();
}
