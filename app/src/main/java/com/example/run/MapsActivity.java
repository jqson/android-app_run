package com.example.run;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity
        implements LocationListener,
                OnMapReadyCallback,
                GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener {

    public static final String EXTRA_MESSAGE = "com.example.run.MESSAGE";
    private static final String TAG = MapsActivity.class.getSimpleName();

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private static final int COLOR_RED_ARGB = 0xffff0000;
    private static final int POLYLINE_WIDTH_PX = 10;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    private GoogleMap mMap;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private boolean mRequestingLocationUpdates;


    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private Location mPreLocation;

    private CameraPosition mCameraPosition;

    private static final long LOCATION_REQUEST_INTERVAL = 1000;
    private static final long LOCATION_REQUEST_MAX_INTERVAL = 1000;
    private LocationRequest mLocationRequest;

    List<LatLng> mPolylineVertex;
    private Polyline mPolyline;

    // information to be displayed
    private TextView timeTextView, distTextView, latitudeTextView, longitudeTextView, speedTextView;

    private static final byte UNREADY = 0;
    private static final byte READY = 1;
    private static final byte RUNNING = 2;
    private static final byte FINISH = 3;
    private static final byte PAUSE = 4;
    private byte mRunStatus;

    private Date mStartTime;
    private short mRunTimeSec;
    private float mRunDistance;

    // speed m/s less than this will be treat as GPS error
    private static final float SPEED_ERR_THRESHOLD = 0.2f;

    private static final long TIMMER_INTERVAL = 1000;
    private static final long TIMMER_MAX = 3600000;
    private CountDownTimer mCDTimer;

    // time s, latitude, longitude, distance to pre m (speed m/s), total distance m
    private List<String> mRunData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_maps);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mRunStatus = UNREADY;
        mRequestingLocationUpdates = true;

        timeTextView = (TextView) findViewById(R.id.time);
        distTextView = (TextView) findViewById(R.id.distance);
        latitudeTextView = (TextView) findViewById(R.id.latitude);
        longitudeTextView = (TextView) findViewById(R.id.longitude);
        speedTextView = (TextView) findViewById(R.id.speed);

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                //.addApi(Places.GEO_DATA_API)
                //.addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        initMapAndLocation();
        initInfo();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMapAndLocation();
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

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_MAX_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        mLastKnownLocation = newLocation;
        if (mRunStatus != FINISH) {
            centerMapOnCurLocation();
        }
        if (mRunStatus == UNREADY) {
            mRunStatus = READY;
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void initMapAndLocation() {
        if (mMap == null) {
            return;
        }
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

            if (mRequestingLocationUpdates) {
                createLocationRequest();
                startLocationUpdates();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private void initInfo() {
        timeTextView.setText("Run time in sec");
        distTextView.setText("Run distance in meter");
        latitudeTextView.setText("Latitude");
        longitudeTextView.setText("Longitude");
        speedTextView.setText("Speed in m/s");
    }

    private void updateDisplayInfo(float newDist) {
        timeTextView.setText(String.valueOf(mRunTimeSec));
        distTextView.setText(String.valueOf(mRunDistance));
        latitudeTextView.setText(String.valueOf(mLastKnownLocation.getLatitude()));
        longitudeTextView.setText(String.valueOf(mLastKnownLocation.getLongitude()));
        speedTextView.setText(String.valueOf(newDist));
    }

    private void centerMapOnCurLocation() {
        if (mLastKnownLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        }
    }

    private void initPolyline() {
        if (mPolyline != null) {
            clearPolyline();
        }

        PolylineOptions polylineOpt = new PolylineOptions().width(POLYLINE_WIDTH_PX).color(COLOR_RED_ARGB);
        mPolyline = mMap.addPolyline(polylineOpt);

        mPolylineVertex = new ArrayList<>();
        mPolyline.setPoints(mPolylineVertex);
    }

    private void addToPolyline(Location location) {
        mPolylineVertex.add(new LatLng(location.getLatitude(), location.getLongitude()));
        mPolyline.setPoints(mPolylineVertex);
    }

    private void clearPolyline() {
        mPolyline.remove();
        mPolyline = null;
    }

    public void startRun(View view) {
        if (mRunStatus == READY) {
            mStartTime = new Date();

            mRunData = new ArrayList<>();

            initPolyline();

            mRunTimeSec = 0;
            mRunDistance = 0.0f;
            mPreLocation = mLastKnownLocation;

            mCDTimer = new CountDownTimer(TIMMER_MAX, TIMMER_INTERVAL) {
                public void onTick(long millisUntilFinished) {
                    mRunTimeSec = (short) ((TIMMER_MAX - millisUntilFinished) / 1000);
                    float newDist = mPreLocation.distanceTo(mLastKnownLocation);
                    if (newDist / (TIMMER_INTERVAL / 1000) < SPEED_ERR_THRESHOLD) {
                        newDist = 0.0f;
                    }
                    mRunDistance += newDist;
                    mPreLocation = mLastKnownLocation;

                    addToPolyline(mLastKnownLocation);
                    updateDisplayInfo(newDist);

                    List<String> runData = new ArrayList<>();
                    runData.add(String.valueOf(mRunTimeSec));
                    runData.add(String.valueOf(mLastKnownLocation.getLatitude()));
                    runData.add(String.valueOf(mLastKnownLocation.getLongitude()));
                    runData.add(String.valueOf(newDist));
                    runData.add(String.valueOf(mRunDistance));
                    mRunData.add(TextUtils.join(",", runData));
                }

                public void onFinish() {
                    timeTextView.setText("Out of time!");
                }
            };

            mCDTimer.start();
            mRunStatus = RUNNING;
        }
    }

    public void endRun(View view) {
        if (mRunStatus == RUNNING) {
            mCDTimer.cancel();

            mRunStatus = FINISH;

            String filename = "sample.csv";

            String fileText = "time,lat,lng,incDist,totalDist\n";
            fileText += TextUtils.join("\n", mRunData);

            OutputStreamWriter outputStreamWriter;

            try {
                outputStreamWriter  = new OutputStreamWriter(
                        openFileOutput(filename, Context.MODE_PRIVATE));
                outputStreamWriter.write(fileText);
                outputStreamWriter .close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            moveCameraFitPolyline(mPolylineVertex);

            showResult(filename);

            mRunStatus = READY;
        }
    }

    private void showResult(String filename) {
        Intent intent = new Intent(this, DisplayRunResultActivity.class);
        intent.putExtra(EXTRA_MESSAGE, filename);
        startActivity(intent);
    }

    private void moveCameraFitPolyline(List<LatLng> mPolylineVertex) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : mPolylineVertex) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
    }
}
