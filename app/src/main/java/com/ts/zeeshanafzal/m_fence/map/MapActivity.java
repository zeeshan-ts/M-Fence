package com.ts.zeeshanafzal.m_fence.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ts.zeeshanafzal.m_fence.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapActivity extends AppCompatActivity implements MapListener,
        OnMapReadyCallback {

    private final String TAG = "MapActivity";
    private final long MIN_TIME = 3 * 1000;     //3 Secs
    private final float MIN_DISTANCE = 10;    //meters

    final int REQ_CODE_PERM_LOCATION = 4433;

    @BindView(R.id.tv_location)
    TextView tvLocation;

    MapPresenter mapPresenter;

    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationSettingsRequest locationSettingsRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        initViews();

        mapPresenter = new MapPresenter(this);

        setupLocationClient();
    }


    private void initViews() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map);
        mapFragment.getMapAsync(this);
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
                mapPresenter.onAddFenceClicked();
                break;
            case R.id.mi_clear_fence:
                mapPresenter.onClearFenceClicked();
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
}
