package com.s3603441.roadqualityanalyser.ui.analytics_map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.db.AppDatabase;
import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsMap extends Fragment implements LocationListener,
        OnMapReadyCallback, GoogleApiClient
                .ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private AnalyticsMapViewModel analyticsMapViewModel;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private List<PolylineOptions> polylineOptions;
    private boolean zoomOnce = false;
// https://guides.codepath.com/android/Retrieving-Location-with-LocationServices-API

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        analyticsMapViewModel = new ViewModelProvider(this).get(AnalyticsMapViewModel.class);
        final View root = inflater.inflate(R.layout.analytics_map_fragment, container, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Source: https://demonuts.com/android-google-map-in-fragment/
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        polylineOptions = new ArrayList<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(root.getContext().getApplicationContext());
        // Check permission for accessing GPS location and prompt it if required.
        if (ActivityCompat.checkSelfPermission(root.getContext().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }

        return root;
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates(final Context contex) {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(contex);
        settingsClient.checkLocationSettings(locationSettingsRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        startLocationUpdates(getContext());
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());

        loadData(getView());
    }

    @Override
    public void onLocationChanged(Location location) {
        mMap.clear();
        LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());

        for (final PolylineOptions line : polylineOptions)
        {
            mMap.addPolyline(line);
        }

        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        if (!zoomOnce) {
            zoomOnce = true;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16f));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void loadData(final View root) {
        final Snackbar loadingSnackbar = Snackbar.make(root, "Loading data...", Snackbar.LENGTH_INDEFINITE);
        final GetDataTask getDataTask = new GetDataTask(root, getArguments().getString("dateTime"), loadingSnackbar);
        loadingSnackbar.show();
        getDataTask.execute(root.getContext().getApplicationContext());
    }

    private class GetDataTask extends AsyncTask<Context, Void, List<Accelerometer>> {
        private View root;
        private String dateTime;
        private Snackbar loadingSnackbar;
        private float detectionValue;

        public GetDataTask(final View root, final String dateTime, final Snackbar loadingSnackbar) {
            this.root = root;
            this.dateTime = dateTime;
            this.loadingSnackbar = loadingSnackbar;
        }

        @Override
        protected List<Accelerometer> doInBackground(Context... contexts) {
            final AppDatabase appDatabase = Room.databaseBuilder(contexts[0],
                    AppDatabase.class, "data").build();
            final AppDatabase appDatabaseSettings = Room.databaseBuilder(contexts[0],
                    AppDatabase.class, "settings").build();

            detectionValue = appDatabaseSettings.settingDao().getSetting("threshold").getValue();

            return appDatabase.accelerometerDao().getData(dateTime);
        }

        @Override
        protected void onPostExecute(final List<Accelerometer> data) {
            analyticsMapViewModel.setData(data);

            for (int i = 0; i < analyticsMapViewModel.getData().size(); i++) {
                final Accelerometer dataPoint = analyticsMapViewModel.getData().get(i);

                if (i ==  analyticsMapViewModel.getData().size() - 1) {
                    continue;
                }

                final Accelerometer nextDataPoint = analyticsMapViewModel.getData().get(i + 1);
                final LatLng latLng = new LatLng(dataPoint.getLatitude(), dataPoint.getLongitude());
                final LatLng nextLatLng = new LatLng(nextDataPoint.getLatitude(), nextDataPoint.getLongitude());
                PolylineOptions line = new PolylineOptions();

                if (dataPoint.getDetectionValueY() < detectionValue) {
                    line.color(Color.GREEN);
                } else {
                    line.color(Color.RED);
                }

                line.width(10f);
                line.add(latLng);
                line.add(nextLatLng);
                polylineOptions.add(line);
            }

            loadingSnackbar.dismiss();
            Snackbar.make(root, "Loaded data successfully!", Snackbar.LENGTH_SHORT);
        }
    }
}
