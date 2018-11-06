package com.ts.zeeshanafzal.m_fence.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ts.zeeshanafzal.m_fence.R;
import com.ts.zeeshanafzal.m_fence.service.GeoFenceTransitionService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapActivity extends AppCompatActivity implements MapListener,
        OnMapReadyCallback {

    private static float FENCE_RADIUS = 500;    //meters
    private final String TAG = "MapActivity";
    private final long MIN_TIME = 3 * 1000;     //3 Secs
    private final float MIN_DISTANCE = 10;    //meters

    final int REQ_CODE_PERM_LOCATION = 4433;

    @BindView(R.id.tv_location)
    TextView tvLocation;

    @BindView(R.id.root_layout)
    RelativeLayout rootLayout;

    MapPresenter mapPresenter;

    private FusedLocationProviderClient locationClient;
    private GeofencingClient geofencingClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationSettingsRequest locationSettingsRequest;
    private Marker currPosMarker = null;
    private Marker geoFenceMarker = null;
    private GoogleMap googleMap;

    ArrayList<Geofence> geofencesList = new ArrayList<>();
    private PendingIntent geoFencePendingIntent = null;
    private Circle geoFenceCircle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        initViews();

        mapPresenter = new MapPresenter(this);

        setupLocationClient();
        setupGeoFencingClient();
    }


    private void initViews() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map);
        mapFragment.getMapAsync(this);
    }

    private void setupGeoFencingClient() {
        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    private void setupLocationClient() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mapPresenter.onLocationChanged(locationResult.getLastLocation());
            }
        };
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create()
                .setInterval(MIN_TIME)
                .setFastestInterval(100)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationSettingsRequest = new LocationSettingsRequest.Builder().setAlwaysShow(true).addLocationRequest(locationRequest).build();
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequest);
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MapActivity.this,
                                        REQ_CODE_PERM_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapPresenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapPresenter.onStop();
    }

    private boolean hasLocationPermissions() {

        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_CODE_PERM_LOCATION);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_add_fence:
                mapPresenter.onAddFenceClicked(geoFenceMarker);
                break;
            case R.id.mi_clear_fence:
                mapPresenter.onClearFenceClicked();
                break;
            case R.id.mi_search:
                mapPresenter.onSearchClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_CODE_PERM_LOCATION && resultCode == Activity.RESULT_OK) {
            setupLocationClient();
        }
    }


    //        Implementation of interface OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapPresenter.onMapReady(googleMap);
        this.googleMap = googleMap;

        setMap(this.googleMap);
    }

    @SuppressLint("MissingPermission")
    private void setMap(GoogleMap googleMap) {
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapPresenter.onMapClicked(latLng);
            }
        });

        googleMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                Log.i(TAG, "circle clicked");
            }
        });

        if (hasLocationPermissions()) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private PendingIntent getGeoFencePendingIntent() {
        if (geoFencePendingIntent != null) {
            return geoFencePendingIntent;
        }

        return geoFencePendingIntent = PendingIntent.getService(this, 0,
                new Intent(this, GeoFenceTransitionService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeoFencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    //      Implementation of interface MapListener
    @Override
    public void setTVLocation(double latitude, double longitude) {
        tvLocation.setText(String.format("Latitude: %s, Longitude: %s", latitude, longitude));
    }


    @SuppressLint("MissingPermission")
    @Override
    public void startLocationUpdates() {
        if (hasLocationPermissions()) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            requestPermissions();
        }
    }

    @Override
    public void stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void showMarkerAt(Location location) {
        if (currPosMarker == null) {
            MarkerOptions ops = new MarkerOptions();
            ops.position(new LatLng(location.getLatitude(), location.getLongitude()));
            currPosMarker = googleMap.addMarker(ops);
            currPosMarker.setTitle("Current Position");
        } else {
            currPosMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    @Override
    public void moveCameraTo(LatLng latLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void showGeoFenceMarkerAt(LatLng latLng) {
        if (geoFenceMarker == null) {
            MarkerOptions ops = new MarkerOptions();
            ops.position(latLng);
            ops.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            geoFenceMarker = googleMap.addMarker(ops);
            geoFenceMarker.setTitle("Center of Geo Fence");
        } else {
            geoFenceMarker.setPosition(latLng);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void addGeoFence(final LatLng latLng) {
        Geofence gf = getGeoFence(latLng);

        if (hasLocationPermissions()) {
            Task<Void> task = geofencingClient.addGeofences(getGeoFencingRequest(gf), getGeoFencePendingIntent());
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mapPresenter.onFenceAdded(latLng);
                }
            });
        } else {
            showSnackbar("Please grant Location permissions");
        }
    }

    @Override
    public void addCircleAt(LatLng latLng) {
        Log.i(TAG, "addCircleAt() " + latLng.toString());

        if (geoFenceCircle == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.radius(FENCE_RADIUS);
            circleOptions.strokeColor(Color.LTGRAY);
            circleOptions.fillColor(Color.CYAN);
            geoFenceCircle = googleMap.addCircle(circleOptions);
        } else {
            geoFenceCircle.setCenter(latLng);
        }
    }

    @Override
    public void clearFences() {
        if (geofencingClient != null) {
            geofencingClient.removeGeofences(getGeoFencePendingIntent());
        }

        if (geoFenceCircle != null) {
            geoFenceCircle.remove();
        }

        if (geoFenceMarker != null) {
            geoFenceMarker.remove();
        }

        geoFenceMarker = null;
        geoFenceCircle = null;
    }

    @Override
    public void zoomCamera(float v) {
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(v));
    }

    @Override
    public void startPlacesSearchActivity() {
//        new PlaceAu
    }

    @Override
    public void showSnackbar(String msg) {
        Snackbar snackbar = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @NonNull
    private Geofence getGeoFence(LatLng latlng) {
        return new Geofence.Builder()
                .setRequestId("0")
                .setCircularRegion(latlng.latitude, latlng.longitude, FENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

}
