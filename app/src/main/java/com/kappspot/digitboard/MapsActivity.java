package com.kappspot.digitboard;
import android.content.Context;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private double lat, lon, alt;
    private double currSpeed;
    private LatLng prevPos, currPos;
    private double sample;
    private double maxSpeedLimit;

    //UI Elements
    TextView tvSpeed;
    boolean cameraSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.kappspot.digitboard.R.layout.activity_maps);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(com.kappspot.digitboard.R.id.map);
        mapFragment.getMapAsync(this);

        //Initialize the objects
        final Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        final Geocoder address = new Geocoder(getApplicationContext(), Locale.getDefault());
        sample = 0;
        maxSpeedLimit = 0.25;
        cameraSet = false;


        //Create the location manager to get the data
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Get the geo location data
                lat = location.getLatitude();
                lon = location.getLongitude();
                alt = location.getAltitude();
                currSpeed = location.getSpeed();

                //Set the target and move the map's camera to that location
                prevPos = currPos == null ? new LatLng(lat, lon) : currPos;
                currPos = new LatLng(lat, lon);
                if(cameraSet == false) {
                    CameraPosition cameraPosition = new CameraPosition
                            .Builder()
                            .target(currPos)
                            .zoom(20)
                            .build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);
                }

                //Set the display message
                //And display the message as a toast
                String displayMessage = "";
                try {
                    displayMessage += "Speed : " + currSpeed + "m/hr" + "\n"
                            + "Latitude : " + lat + "\n"
                            + "Longitude : " + lon + "\n"
                            + "Altitude : " + alt + "\n"
                            + "Road : " + address.getFromLocation(lat, lon, 1).get(0).getAddressLine(0) + "\n"
                            + "Max Limit : " + 1 + "m/hr";

                } catch (Exception e) { displayMessage = e.getMessage(); }
                Toast.makeText(getApplicationContext(), displayMessage, Toast.LENGTH_LONG).show();



                //Cancel the phone vibration if any
                //Then check if the speed is exceeded
                //If so show the path in red and also start the vibration of the phone
                vibrator.cancel();
                if(currSpeed >= maxSpeedLimit) {
                    vibrator.vibrate(5000);

                    map.addPolyline(new PolylineOptions().add(prevPos, currPos).width(10).color(Color.RED));
                    map.addCircle(new CircleOptions().center(currPos).radius(0.5).fillColor(Color.RED).strokeColor(Color.RED));
                } else {
                    map.addPolyline(new PolylineOptions().add(prevPos, currPos).width(10).color(Color.GREEN));
                    map.addCircle(new CircleOptions().center(currPos).radius(0.5).fillColor(Color.GREEN).strokeColor(Color.GREEN));
                }

                //Increase the sample count
                if(sample < 10) { sample++; }
                else {
                    sample = 0;

                    //Update the new speed limit here
                    maxSpeedLimit = 1;
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 5,0,locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Set the camera here
        if(currPos != null) {
            CameraPosition cameraPosition = new CameraPosition
                    .Builder()
                    .target(currPos)
                    .zoom(20)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);
        }
    }

    GoogleMap map;

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
    }
}
