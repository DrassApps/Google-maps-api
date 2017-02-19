package com.drassapps.googlemapsandroid_drassapps;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleMap mapa;
    String ubi;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    double lat = 0.0;
    double lng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        final MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        // Initialize GoogleApiClient with API_KEY from manifest

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }

    @Override
    public void onConnected(Bundle bundle) {

        // Necessary request permission user, to ger current location

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        } else {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) { // If we got a location

                mapa.setMyLocationEnabled(true);
                mapa.getUiSettings().setMyLocationButtonEnabled(true); // Enable location button
                mapa.getUiSettings().setZoomControlsEnabled(true); // Enable controls for map

                lat = mLastLocation.getLatitude(); // Assign lat to our vb
                lng = mLastLocation.getLongitude(); // Assign lng to our vb


                // Create a new LatLng for move de camera to our location
                LatLng current = new LatLng(lat,lng);
                mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 14.0f));

                ubi = miUbicacion(); // ubi = our current location from gps

            }else if (ubi == null){
                Toast.makeText(MainActivity.this, "Enable Gps!", Toast.LENGTH_LONG).show();
            }
        }
    }


    // Initialize map

    @Override
    public void onMapReady(GoogleMap map) { mapa = map; }

    @Override
    public void onConnectionSuspended(int arg0) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    // FUNC to check Location Permission

    private void checkLocationPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 3);

        } else {

            // Do nothing
        }
    }


    // We handle the user Request

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 3: {

                // If Success

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("Info permission: ","Success!");

                } else {

                    // Request permission again
                    Log.i("Info permission: ","Denied!");

                    checkLocationPermission();


                }
                return;
            }
        }
    }

    // FUNC to get our location

    private String miUbicacion() {

        String ubi = "";

        List<Address> addressList = null;

        Geocoder geocoder = new Geocoder(this); // Constructs a Geocoder whose responses will be
                                                // localized for the default system Locale.

        if (lat == 0.0 && lng == 0.0) {

            Toast.makeText(MainActivity.this, "Enable Gps!", Toast.LENGTH_LONG).show();

        } else {
            try {

                addressList = geocoder.getFromLocation(lat, lng, 1);
                Address address = addressList.get(0);
                ubi = address.getAddressLine(0) + ", " + address.getLocality();
                Toast.makeText(MainActivity.this,"Your location:" + ubi, Toast.LENGTH_LONG).show();

            } catch (IOException e) {
            }

        }
        return ubi;
    }

    // Func to search a place in map

    public void onMapSearch(View view) {

        EditText locationSearch = (EditText) findViewById(R.id.search_map);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {

            Geocoder geocoder = new Geocoder(this);

            try {

                InputMethodManager inputManager =
                        (InputMethodManager) this.
                                getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(
                        this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                if (addressList == null || addressList.size() <= 0 || geocoder == null) {

                    addressList = geocoder.getFromLocationName(location, 1);
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mapa.addMarker(new MarkerOptions().position(latLng).title("Your place"));
                    mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));

                }else {Toast.makeText(this, "This palce don't exist", Toast.LENGTH_SHORT).show();}

            } catch (IOException e) {
                Toast.makeText(this, "This palce don't exist", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this,"This palce don't exist",Toast.LENGTH_SHORT).show();
        }

    }

}
