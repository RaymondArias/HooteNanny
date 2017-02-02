package com.inasweaterpoorlyknit.hackpoly2016;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import static com.google.android.gms.location.LocationServices.*;

/**
 * class that simplifies the process of getting the current location of the user
 */
public class GPSHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Context context;
    private Activity activity;
    // flag for GPS Status
    private boolean isGPSEnabled = false;
    // flag for network status
    private boolean isNetworkEnabled = false;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private GoogleApiClient googleAPI;
    private Location location;
    private final int GET_LOCATION_PERMISSION = 10;

    public GPSHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        if (googleAPI == null) {
            googleAPI = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(API)
                    .build();
        }
        googleAPI.connect();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        updateMyLocation();
    }

    private void updateMyLocation() {
        List<String> providers = locationManager.getProviders(true);

        Location l = null;
        for (int i = 0; i < providers.size(); i++) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            l = locationManager.getLastKnownLocation(providers.get(i));
            if (l != null) {
                break;
            }
        }
        if (l != null) {
            latitude = l.getLatitude();
            longitude = l.getLongitude();
        }
    }

    public boolean isGPSenabled() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return (isGPSEnabled || isNetworkEnabled);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public double getUpdatedLatitude() {
        updateMyLocation();
        return latitude;
    }

    public double getUpdatedLongitude() {
        updateMyLocation();
        return longitude;
    }

    public LatLng getUpdatedLatLng() {
        updateMyLocation();
        return new LatLng(latitude, longitude);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GET_LOCATION_PERMISSION);
            return;
        }
        location = FusedLocationApi.getLastLocation(googleAPI);
        this.latitude = location.getLatitude();

        this.longitude = location.getLongitude();
        Log.d("Location" , "lat" + latitude);
        Log.d("Location" , "long" + longitude);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public double getLat()
    {
        return latitude;
    }
    public double getLong()
    {
        return longitude;
    }


}
