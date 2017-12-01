package com.example.raphael.bigbrother;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;

/**
 * Created by tim on 11/24/17.
 */

public class LocationHandler extends AppCompatActivity implements LocationListener {
    static LocationManager locationManager;
    static double lat;
    static double lng;
    private long time = System.currentTimeMillis();
    private static String url;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        url = this.getApplicationContext().getResources().getString(R.string.clockUrl);
    }


    public LocationHandler(AppCompatActivity activity) {

        time = System.currentTimeMillis();

        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        } else {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            String locationProvider = LocationManager.GPS_PROVIDER;
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            // Make sure that the location isn't mock data
            // if (lastKnownLocation.isFromMockProvider()) return; // TODO(timp): include handling for when data is falsified
            if (lastKnownLocation != null) {
                lat = lastKnownLocation.getLatitude();
                lng = lastKnownLocation.getLongitude();
                this.onLocationChanged(lastKnownLocation);
            }
        }
    }

    public static JSONObject getCoordinates() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("lat", lat);
        res.put("lng", lng);
        return res;
    }

    public static LatLng getLastKnownLocation() {
        return new LatLng(lat, lng);
    }

    @Override
    public void onLocationChanged(Location location) {
        // If it has been five minutes since last update, do so.
        if (System.currentTimeMillis() - time > 300000 && ConnectionHandler.user != null && ConnectionHandler.user.clockStatus) {
            time = System.currentTimeMillis();
        // Update the last known location for the user
            lat = location.getLatitude();
            lng = location.getLongitude();
            // Create a request
            final JSONObject body = new JSONObject();
            try {
                body.put("username", ConnectionHandler.user.username);
                body.put("location", getCoordinates());
            } catch (JSONException e) {

                e.printStackTrace();
            }
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.PUT, url, body, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Successful update location
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });

            // Make the request (add it to the request queue)
            ConnectionHandler.getInstance(this).addToRequestQueue(jsObjRequest);
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

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void stop() {
        locationManager.removeUpdates(this);
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
