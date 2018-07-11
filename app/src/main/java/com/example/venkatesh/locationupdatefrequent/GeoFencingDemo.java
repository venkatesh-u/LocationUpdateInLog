package com.example.venkatesh.locationupdatefrequent;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.master.permissionhelper.PermissionHelper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeoFencingDemo extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {


    ComponentName component;
    Marker marker;
    ProgressDialog pDialog;

    private static final String TAG = GeoFencingDemo.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE_SMS = 35;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    // FIXME: 5/16/17
    private static final long UPDATE_INTERVAL = 5 * 1000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    // FIXME: 5/14/17
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * The entry point to Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

    // UI Widgets.
    //private Button mRequestUpdatesButton;
    //private Button mRemoveUpdatesButton;
    //private TextView mLocationUpdatesResultView;
    private GeofencingRequest geofencingRequest;
    private PendingIntent pendingIntent;
    private boolean isMonitoring = false;
    private GoogleMap googleMap;
    private IntentFilter statusIntentFilter;
    private BroadcastReceiver mDownloadStateReceiver;
    private SharedPreferences sharedPreferences;

    Activity act;
    private Circle circle;

    Geocoder geocoder;
    SupportMapFragment mapFragment;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        act = this;

        component = new ComponentName(act, LocationUpdatesBroadcastReceiver.class);
        geocoder = new Geocoder(this, Locale.getDefault());

        sharedPreferences = getSharedPreferences("location_date_storage", MODE_PRIVATE);


        Log.d(TAG, "onCreate");
        // The filter's action is BROADCAST_ACTION
        statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);

        //mRequestUpdatesButton = (Button) findViewById(R.id.request_updates_button);
        //mRemoveUpdatesButton = (Button) findViewById(R.id.remove_updates_button);
        //mLocationUpdatesResultView = (TextView) findViewById(R.id.location_updates_result);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        //permissions
        isWriteStoragePermissionGranted();

//        if (!checkPermissionsSMS()){
//            requestPermissionsSMS();
//        }


        buildGoogleApiClient();


        mapFragment.getMapAsync(this);




        mDownloadStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Broadcast", "RECEIVED!!");
            }
        };
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mDownloadStateReceiver, statusIntentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d(TAG, "onResume");
////        updateButtonsState(LocationRequestHelper.getRequesting(this));
////        mLocationUpdatesResultView.setText(LocationResultHelper.getSavedLocationResult(this));
//    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
//        getMenuInflater().inflate(R.menu.menu_map_activity, menu);
//        if (isMonitoring) {
//            menu.findItem(R.id.action_start_monitor).setVisible(false);
//            menu.findItem(R.id.action_stop_monitor).setVisible(true);
//        } else {
//            menu.findItem(R.id.action_start_monitor).setVisible(true);
//            menu.findItem(R.id.action_stop_monitor).setVisible(false);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Log.d(TAG, "onOptionsItemSelected");
//        switch (item.getItemId()) {
//            case R.id.action_start_monitor:
//                startGeofencing();
//                break;
//            case R.id.action_stop_monitor:
//                stopGeoFencing();
//                break;
//            case R.id.action_settings:
////                changeGeofence();
//                break;
//        }
        return super.onOptionsItemSelected(item);
    }


//    private void changeGeofence() {
//
//        if (isMonitoring) {
//            Toast.makeText(GeoFencingDemo.this, "Please stop monitoring and try again.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // get prompts.xml view
//        LayoutInflater li = LayoutInflater.from(this);
//        View promptsView = li.inflate(R.layout.custom_dialog, null);
//
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//
//        // set prompts.xml to alertdialog builder
//        alertDialogBuilder.setView(promptsView);
//
//        final EditText etLat = (EditText) promptsView
//                .findViewById(R.id.etLat);
//        final EditText etLong = (EditText) promptsView
//                .findViewById(R.id.etLong);
//        final EditText etRadius = (EditText) promptsView
//                .findViewById(R.id.etRadius);
//        final EditText etLocationName = (EditText) promptsView
//                .findViewById(R.id.etLocationName);
//
//
//        Button btnCancel = (Button) promptsView.findViewById(R.id.btnCancel);
//        Button btnSave = (Button) promptsView.findViewById(R.id.btnSave);
//
//        // set dialog message
//        alertDialogBuilder
//                .setCancelable(false);
//
//        // create alert dialog
//        final AlertDialog alertDialog = alertDialogBuilder.create();
//
//        // show it
//        alertDialog.show();
//
//        btnCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.cancel();
//            }
//        });
//
//        btnSave.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                validateFields(etLat, etLong, etRadius,etLocationName, alertDialog);
//
//            }
//        });
//
//
//    }

//    private void validateFields(EditText etLat, EditText etLong, EditText etRadius, EditText etLocationName, AlertDialog alertDialog) {
//
//
//        if (etLat.getText().toString().isEmpty()) {
//            etLat.setError("Enter latitude");
//            return;
//        } else if (etLong.getText().toString().isEmpty()) {
//            etLong.setError("Enter longitude");
//            return;
//        } else if (etRadius.getText().toString().isEmpty()) {
//            etRadius.setError("Enter radius");
//            return;
//        }
////        else if (etLocationName.getText().toString().isEmpty()){
////            etLocationName.setError("Enter location name");
////        }
//
//
//        double latitude = Double.parseDouble(etLat.getText().toString());
//        double longitude = Double.parseDouble(etLong.getText().toString());
//        float radiusInMtrs = Float.parseFloat(etRadius.getText().toString());
//        String area  = getaddressFromGEO(latitude, longitude);
//
//        saveDataInSharedPreferences(latitude, longitude, radiusInMtrs,area,  alertDialog);
//
//    }

//    private void saveDataInSharedPreferences(double latitude, double longitude, float radiusInMtrs, String area, AlertDialog alertDialog) {
//        alertDialog.cancel();
//
//        sharedPreferences.edit()
//                .putFloat("radius", radiusInMtrs)
//                .putString("latitude", String.valueOf(latitude))
//                .putString("longitude", String.valueOf(longitude))
//                .putString("area", area)
//                .apply();
//
//
//        showMap();
//    }

    private String getaddressFromGEO(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

//        String city = addresses.get(0).getLocality();
//        String state = addresses.get(0).getAdminArea();
//        String country = addresses.get(0).getCountryName();
//        String postalCode = addresses.get(0).getPostalCode();
//        String knownName = addresses.get(0).getFeatureName();

//        String arrea =  address.substring(0, 10);
        return address;
    }

//    private void showMap() {
//
//        float rad = sharedPreferences.getFloat("radius", 0);
//
//        if (rad != 0) {
//
//            marker.remove();
//            circle.remove();
//
//            double lat = Double.parseDouble(sharedPreferences.getString("latitude", null));
//            double longit = Double.parseDouble(sharedPreferences.getString("longitude", null));
//           String area = sharedPreferences.getString("area", "Unknown area");
//            this.googleMap = googleMap;
//            LatLng latLng = Constants.AREA_LANDMARKS.get(Constants.GEOFENCE_ID_TECH_M);
//            LatLng latLng1 = new LatLng(lat, longit);
//            marker =  googleMap.addMarker(new MarkerOptions().position(latLng1).title(area));
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 17f));
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//
//            googleMap.setMyLocationEnabled(true);
//
//            circle = googleMap.addCircle(new CircleOptions()
//                    .center(new LatLng(latLng1.latitude, latLng1.longitude))
//                    .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
//                    .strokeColor(Color.RED)
//                    .strokeWidth(4f));
//
//        }
//
//    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        Log.d(TAG,"createLocationRequest");
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    private void buildGoogleApiClient() {
        Log.d(TAG,"buildGoogleApiClient");
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        createLocationRequest();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
        LocationRequestHelper.setRequesting(this, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Log.d(TAG,"getPendingIntent");
        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"onConnectionSuspended");
        final String text = "Connection suspended";
        Log.w(TAG, text + ": Error code: " + i);
        showSnackbar("Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed");
        final String text = "Exception while connecting to Google Play services";
        Log.w(TAG, text + ": " + connectionResult.getErrorMessage());
        showSnackbar(text);
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        Log.d(TAG,"showSnackbar");
        View container = findViewById(R.id.activity_main);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }



//
    private void requestPermissionsSMS() {
        Log.d(TAG,"requestPermissions");
        boolean shouldProvideSmsPer =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS);
        if (shouldProvideSmsPer) {
            Log.i(TAG, "Displaying permission rationale to provide .");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_sms,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(GeoFencingDemo.this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    REQUEST_PERMISSIONS_REQUEST_CODE_SMS);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(GeoFencingDemo.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_PERMISSIONS_REQUEST_CODE_SMS);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(TAG,"onSharedPreferenceChanged");
        if (s.equals(LocationResultHelper.KEY_LOCATION_UPDATES_RESULT)) {
            //mLocationUpdatesResultView.setText(LocationResultHelper.getSavedLocationResult(this));
        } else if (s.equals(LocationRequestHelper.KEY_LOCATION_UPDATES_REQUESTED)) {
            updateButtonsState(LocationRequestHelper.getRequesting(this));
        }
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates(View view) {
        Log.d(TAG,"requestLocationUpdates");
        try {
            Log.i(TAG, "Starting location updates");
            LocationRequestHelper.setRequesting(this, true);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            LocationRequestHelper.setRequesting(this, false);
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates(View view) {
        Log.i(TAG, "Removing location updates");
        LocationRequestHelper.setRequesting(this, false);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getPendingIntent());
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void updateButtonsState(boolean requestingLocationUpdates) {
        Log.d(TAG,"updateButtonsState");
        if (requestingLocationUpdates) {
            //mRequestUpdatesButton.setEnabled(false);
            //mRemoveUpdatesButton.setEnabled(true);
        } else {
            //mRequestUpdatesButton.setEnabled(true);
            //mRemoveUpdatesButton.setEnabled(false);
        }
    }


    private void startLocationMonitor() {
        Log.d(TAG, "start location monitor");
        /*LocationRequest locationRequest = LocationRequest.create()
                .setInterval(20000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);*/
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "Location Change Lat Lng " + location.getLatitude() + " " + location.getLongitude());
                    /*if (currentLocationMarker != null) {
                        currentLocationMarker.remove();
                    }
                    markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                    markerOptions.title("Current Location");

                    if(googleMap != null)
                        currentLocationMarker = googleMap.addMarker(markerOptions);
                    Log.d(TAG, "Location Change Lat Lng " + location.getLatitude() + " " + location.getLongitude());*/
                }
            });
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        }

    }

    private void startGeofencing() {
        Log.d(TAG, "Start geofencing monitoring call");

        if (!isReceiverEnabled()){
            act.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED ,
                    PackageManager.DONT_KILL_APP);
        }

        pendingIntent = getGeofencePendingIntent();
        geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                .addGeofence(getGeofence())
                .build();

        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Google API client not connected");
        } else {
            try {
                LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequest, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "Successfully Geofencing Connected");
                        } else {
                            Log.d(TAG, "Failed to add Geofencing " + status.getStatus());
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        isMonitoring = true;
        invalidateOptionsMenu();
    }

    @NonNull
    private Geofence getGeofence() {
        Log.d(TAG,"getGeofence");

     float rad  = sharedPreferences.getFloat("radius", 0);

        if (rad!=0){

            double lat = Double.parseDouble(sharedPreferences.getString("latitude", null));
            double longit = Double.parseDouble(sharedPreferences.getString("longitude", null));
            String area = sharedPreferences.getString("area", "unKnown area 1");
            LatLng latLng = Constants.AREA_LANDMARKS.get(Constants.GEOFENCE_ID_TECH_M);
             return new Geofence.Builder()
                 .setRequestId(area)
                 .setExpirationDuration(Geofence.NEVER_EXPIRE)
                 .setCircularRegion(lat, longit, rad)
                 .setNotificationResponsiveness(1000)
                 .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                 .build();

     }else {

         LatLng latLng = Constants.AREA_LANDMARKS.get(Constants.GEOFENCE_ID_TECH_M);
         return new Geofence.Builder()
                 .setRequestId(Constants.GEOFENCE_ID_TECH_M)
                 .setExpirationDuration(Geofence.NEVER_EXPIRE)
                 .setCircularRegion(latLng.latitude, latLng.longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                 .setNotificationResponsiveness(1000)
                 .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                 .build();
     }

    }

    private PendingIntent getGeofencePendingIntent() {
        Log.d(TAG,"getGeofencePendingIntent");
        if (pendingIntent != null) {
            return pendingIntent;
        }
//        Intent intent = new Intent(this, GeofenceRegistrationService.class);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return null;
    }

    private void stopGeoFencing() {
        Log.d(TAG,"stopGeoFencing");
        pendingIntent = getGeofencePendingIntent();
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, pendingIntent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess())
                            Log.d(TAG, "Stop geofencing");
                        else
                            Log.d(TAG, "Not stop geofencing");
                    }
                });
        isMonitoring = false;
        invalidateOptionsMenu();

        if (isReceiverEnabled()){
            act.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED ,
                    PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"onMapReady");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        float rad  = sharedPreferences.getFloat("radius", 0);


        if (rad!=0){
            double lat = Double.parseDouble(sharedPreferences.getString("latitude", null));
            double longit = Double.parseDouble(sharedPreferences.getString("longitude", null));
            String area = sharedPreferences.getString("area", "unKnown area 1");

            this.googleMap = googleMap;
            LatLng latLng = Constants.AREA_LANDMARKS.get(Constants.GEOFENCE_ID_TECH_M);
            LatLng latLng1 = new LatLng(lat, longit);
            marker = googleMap.addMarker(new MarkerOptions().position(latLng1).title(area));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 17f));
            googleMap.setMyLocationEnabled(true);

             circle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(latLng1.latitude, latLng1.longitude))
                    .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
                    .strokeColor(Color.RED)
                    .strokeWidth(4f));


        }else {

            this.googleMap = googleMap;
            LatLng latLng = Constants.AREA_LANDMARKS.get(Constants.GEOFENCE_ID_TECH_M);
             marker = googleMap.addMarker(new MarkerOptions().position(latLng).title("TECH MAHINDRA"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
            googleMap.setMyLocationEnabled(true);

             circle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(latLng.latitude, latLng.longitude))
                    .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
                    .strokeColor(Color.RED)
                    .strokeWidth(4f));

        }

    }

    public boolean isReceiverEnabled(){
        boolean b=false;
        int status = act.getPackageManager().getComponentEnabledSetting(component);
        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Log.d(TAG, "receiver is enabled");
            b = true;
        } else if(status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            Log.d(TAG, "receiver is disabled");
            b = false;
        }
        return b;
    }



    public void isWriteStoragePermissionGranted() {

        permissionHelper = new PermissionHelper(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, 100);

        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(act, "granted", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onPermissionGranted() called");

                buildGoogleApiClient();

                mapFragment.getMapAsync(GeoFencingDemo.this);

                isMonitoring = true;

            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {
                Log.d(TAG, "onIndividualPermissionGranted() called with: grantedPermission = [" + TextUtils.join(",", grantedPermission) + "]");
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(GeoFencingDemo.this, "permission denied", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onPermissionDenied() called");
            }

            @Override
            public void onPermissionDeniedBySystem() {
                Log.d(TAG, "onPermissionDeniedBySystem() called");
                permissionHelper.openAppDetailsActivity();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}

