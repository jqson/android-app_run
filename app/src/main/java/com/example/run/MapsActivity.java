package com.example.run;

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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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

    private CameraPosition mCameraPosition;

    private static final long LOCATION_REQUEST_INTERVAL = 2000;
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 1000;
    private LocationRequest mLocationRequest;

    List<LatLng> mPolylineVertex;
    private Polyline mPolyline;

    private static final int COLOR_RED_ARGB = 0xffff0000;
    private static final int COLOR_BLUE_ARGB = 0xff0000ff;
    private static final int POLYLINE_WIDTH_PX = 10;

    // information to be displayed
    private TextView timeTextView, distTextView, speedTextView;
    private Button startButton;

    private TextView ghostTextView;

    private static final byte UNREADY = 0;
    private static final byte READY = 1;
    private static final byte RUNNING = 2;
    private static final byte FINISH = 3;
    private static final byte PAUSE = 4;
    private byte mRunStatus;

    private Date mStartTime;
    private int mRunTimeSec;

    private static final long TIMMER_INTERVAL = 1000;
    private static final long TIMMER_MAX = 36000000;
    private CountDownTimer mCDTimer;

    private boolean mHasGhost;
    // time s, latitude, longitude, distance to pre m (speed m/s), total distance m
    private RunnerDataManager mRunnerData;
    private GhostDataManager mGhostData;

    private String mRunnerFilename;
    private String mGhostFilename;

    private Marker mRunnerMarker;
    private Marker mGhostMarker;

    private static final float RUNNER_MARKER_COLOR = BitmapDescriptorFactory.HUE_RED;
    private static final float GHOST_MARKER_COLOR = BitmapDescriptorFactory.HUE_BLUE;

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
        mHasGhost = false;

        mRequestingLocationUpdates = true;

        timeTextView = (TextView) findViewById(R.id.time);
        distTextView = (TextView) findViewById(R.id.distance);
        speedTextView = (TextView) findViewById(R.id.speed);

        ghostTextView = (TextView) findViewById(R.id.ghost);

        startButton = (Button) findViewById(R.id.start_button);

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

        Intent intent = getIntent();
        mGhostFilename = intent.getStringExtra(EXTRA_MESSAGE);
        if (!mGhostFilename.equals(HistoryActivity.NO_GHOST_FILENAME)) {
            mHasGhost = true;
            loadRunHistory(mGhostFilename);
        }

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
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
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
        if (mRunStatus == READY || mRunStatus == RUNNING) {
            centerMapOnCurLocation();
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
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onBackPressed() {
        if (mRunStatus != RUNNING) {
            super.onBackPressed();
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
        if (mHasGhost) {
            timeTextView.setText(Util.secToTimeString(mGhostData.getTotalTime()));
            distTextView.setText(Util.meterToString(mGhostData.getTotalDistance()));
            speedTextView.setText(String.valueOf(mGhostData.getAvgSpeed()));
            ghostTextView.setText("Distance to ghost");
        } else {
            timeTextView.setText("00:00");
            distTextView.setText("0");
            speedTextView.setText("0");
            ghostTextView.setText("No Ghost");
        }
    }

    private void updateDisplayInfo(float speed) {
        timeTextView.setText(Util.secToTimeString(mRunTimeSec));
        distTextView.setText(Util.meterToString(mRunnerData.getDistance()));
        speedTextView.setText(String.valueOf(speed));
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

        PolylineOptions polylineOpt = new PolylineOptions()
                .width(POLYLINE_WIDTH_PX)
                .color(COLOR_RED_ARGB);
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
        if ((mRunStatus == UNREADY || mRunStatus == FINISH) && mLastKnownLocation != null) {
            mRunStatus = READY;
            // TODO
            startButton.setText(R.string.start_button_text);
            if (mHasGhost) {
                mGhostData.reset();
                setGhostMarker(mGhostData.getLatLng());
            }
            return;
        }

        if (mRunStatus == READY) {
            mStartTime = new Date();

            mRunnerFilename = String.valueOf(mStartTime.getTime()) + ".csv";

            mRunnerData = new RunnerDataManager(this);

            initPolyline();

            mRunTimeSec = 0;

            mCDTimer = new CountDownTimer(TIMMER_MAX, TIMMER_INTERVAL) {
                public void onTick(long millisUntilFinished) {
                    mRunTimeSec = (int) ((TIMMER_MAX - millisUntilFinished) / 1000);

                    float speed = mRunnerData.addPoint(mRunTimeSec, mLastKnownLocation);
                    updateDisplayInfo(speed);
                    addToPolyline(mLastKnownLocation);

                    if (mHasGhost) {
                        mGhostData.setRunTime(mRunTimeSec);
                        setGhostMarker(mGhostData.getLatLng());
                        ghostTextView.setText(String.valueOf(
                                mRunnerData.getDistance() - mGhostData.getDistance()));
                    }
                }

                public void onFinish() {
                    timeTextView.setText("Out of time!");
                }
            };

            mCDTimer.start();
            mRunStatus = RUNNING;
        }
    }

    // TODO: change START/END to one single button
    public void endRun(View view) {
        if (mRunStatus == RUNNING) {
            mCDTimer.cancel();

            mRunStatus = FINISH;

            // TODO show final polyline based on mRunData
            moveCameraFitPolyline(mPolylineVertex);

            mRunnerData.saveToFile(mRunnerFilename);

            addToHistoryFile();
            addHistoryToDb();

            //showResult(filename);

            // TODO
            startButton.setText(R.string.ready_button_text);
        }
    }

    private void moveCameraFitPolyline(List<LatLng> mPolylineVertex) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : mPolylineVertex) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60));
    }

    private void loadRunHistory(String filename) {
        mGhostData = new GhostDataManager(this);
        mGhostData.loadFromFile(filename);
        List<LatLng> historyLatLngList = mGhostData.getLatLngList();

        PolylineOptions polylineOpt = new PolylineOptions()
                .width(POLYLINE_WIDTH_PX)
                .color(COLOR_BLUE_ARGB);
        Polyline gPolyline = mMap.addPolyline(polylineOpt);
        gPolyline.setPoints(historyLatLngList);
        moveCameraFitPolyline(historyLatLngList);

        setGhostMarker(mGhostData.getLatLng());
    }

    private void setGhostMarker(LatLng position) {
        if (mGhostMarker != null) {
            mGhostMarker.remove();
        }
        mGhostMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(GHOST_MARKER_COLOR)));
    }

    private void addHistoryToDb() {
        RunHistory newHistory = new RunHistory(
                mStartTime.getTime(),
                mRunnerData.getTime(),
                mRunnerData.getDistance(),
                mRunnerFilename);

        HistoryDbHelper dbHelper = new HistoryDbHelper(this);

        dbHelper.addHistory(newHistory);
    }

    private void addToHistoryFile() {
        // "filename,dateMs,time,dist\n";
        String newFileInfo = mRunnerFilename + "," +
                String.valueOf(mStartTime.getTime()) + "," +
                String.valueOf(mRunnerData.getTime()) + "," +
                String.valueOf(mRunnerData.getDistance()) + "\n";

        String orgFileText = DataManager.readFile(HistoryActivity.HISTORY_FILENAME, this);
        DataManager.writeFile(HistoryActivity.HISTORY_FILENAME, orgFileText + newFileInfo, this);
    }
}
