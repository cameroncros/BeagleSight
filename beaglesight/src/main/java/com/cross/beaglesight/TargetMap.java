package com.cross.beaglesight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cross.beaglesightlibs.LocationDescription;
import com.cross.beaglesightlibs.Target;
import com.cross.beaglesightlibs.TargetManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import static android.view.View.GONE;

public class TargetMap extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnMarkerClickListener {
    private volatile GoogleMap mMap = null;
    private TargetManager tm;
    private LocationManager locationManager;
    private Boolean trackLocation = true;
    private Location currentLocation = null;
    private ProgressBar progressBar;
    private FloatingActionButton refocusButton;
    private Map<Target, List<LocationDescription>> targetListMap = new HashMap<>();
    private Map<Marker, Target> markerOptionsTargetMap = new HashMap<>();
    private Map<Marker, LocationDescription> markerOptionsLocationDescriptionMap = new HashMap<>();
    private View targetInfo;
    private TextView targetDescription;
    private TextView targetDistance;
    private LocationDescription selectedShootLocation = null;
    private Target selectedTarget = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        targetInfo = findViewById(R.id.targetInfo);
        targetDescription = findViewById(R.id.targetDescription);
        targetDistance = findViewById(R.id.targetDistance);

        MapView mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        // Initialise TargetManager
        tm = TargetManager.getInstance(this);

        progressBar = findViewById(R.id.fabProgress);

        refocusButton = findViewById(R.id.refocus);
        refocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackLocation = true;
                progressBar.setVisibility(View.VISIBLE);
                progressBar.invalidate();
                focusMap();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sight_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id)
        {
            case R.id.action_add:
                startActivity(new Intent(this, EditTarget.class));
                return true;
            case R.id.action_import:
                //TODO: startActivity(new Intent(this, ImportTargets.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 1) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
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
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setMinZoomPreference(15);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };
                ActivityCompat.requestPermissions(this, permissions, 1);
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            mMap.setMyLocationEnabled(true);
        }

        LatLng latLng = new LatLng(-37.625729, 145.130766);
        CameraPosition cameraPosition = new CameraPosition(latLng, 15, 0, 0);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        getTargets();
    }

    private void getTargets() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                targetListMap.clear();
                List<Target> targets = tm.targetDao().getAll();
                for (Target target : targets) {
                    List<LocationDescription> shootLocations = tm.locationDescriptionDao().getLocationsForTarget(target.getTargetLocation().getTargetId());
                    targetListMap.put(target, shootLocations);
                }

                renderTargets();
            }
        });
    }

    private String getStats(LocationDescription shootLocation,
                            LocationDescription targetLocation) {
        return String.format(Locale.ENGLISH, "Distance: %.2fm Pitch: %.2f°",
                shootLocation.distanceTo(targetLocation),
                shootLocation.pitchTo(targetLocation));
    }

    private void renderTargets() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                markerOptionsTargetMap.clear();
                markerOptionsLocationDescriptionMap.clear();

                while (mMap == null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mMap.clear();

//                // TODO: Draw current location.
//                if (currentLocation != null)
//                {
//                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                    mMap.addMarker(new MarkerOptions()
//                            .position(latLng)
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//                    mMap.addCircle(new CircleOptions().center(latLng).radius(currentLocation.getAccuracy()));
//                }

                Set<Target> targets = targetListMap.keySet();
                for (Target target : targets) {
                    LocationDescription targetLocation = target.getTargetLocation();
                    LatLng targetPos = targetLocation.getLatLng();
                    Marker targetMarker = mMap.addMarker(new MarkerOptions()
                            .position(targetPos)
                            .title(target.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    markerOptionsTargetMap.put(targetMarker, target);

                    List<LocationDescription> shootLocations = targetListMap.get(target);
                    if (shootLocations != null) {
                        for (LocationDescription shootLocation : shootLocations) {
                            LatLng shootPos = shootLocation.getLatLng();
                            Marker shootMarker = mMap.addMarker(new MarkerOptions()
                                    .position(shootPos)
                                    .title(shootLocation.getDescription())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                            markerOptionsTargetMap.put(shootMarker, target);
                            markerOptionsLocationDescriptionMap.put(shootMarker, shootLocation);

                            mMap.addPolyline(new PolylineOptions().add(shootPos).add(targetPos));
                        }
                    }
                }
            }
        });
    }

    private void focusMap() {
        if (trackLocation && currentLocation != null) {
            Float zoom = 20f;
            Float tilt = 0f;
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraPosition cameraPosition = new CameraPosition(latLng, zoom, tilt, currentLocation.getBearing());
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        progressBar.setVisibility(View.GONE);
        progressBar.invalidate();
        refocusButton.hide();
        refocusButton.invalidate();
        focusMap();
        updateTargetInfo();
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

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == REASON_GESTURE) {
            trackLocation = false;
            refocusButton.show();
            refocusButton.invalidate();
            targetInfo.setVisibility(GONE);
            targetInfo.invalidate();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        trackLocation = false;
        refocusButton.show();
        refocusButton.invalidate();
        selectedShootLocation = null;
        selectedTarget = null;

        if (markerOptionsLocationDescriptionMap.containsKey(marker))
        {
            selectedShootLocation = markerOptionsLocationDescriptionMap.get(marker);
            selectedTarget = markerOptionsTargetMap.get(marker);
        }
        else if (markerOptionsTargetMap.containsKey(marker))
        {
            selectedTarget = markerOptionsTargetMap.get(marker);
        }

        updateTargetInfo();
        return false;
    }

    public void updateTargetInfo()
    {
        if (selectedShootLocation != null && selectedTarget != null)
        {
            targetInfo.setVisibility(View.VISIBLE);
            targetDescription.setText(selectedShootLocation.getDescription());
            targetDistance.setText(getStats(selectedShootLocation, selectedTarget.getTargetLocation()));
        }
        else if (selectedTarget != null)
        {
            LocationDescription targetLocation = selectedTarget.getTargetLocation();
            targetInfo.setVisibility(View.VISIBLE);
            targetDescription.setText(targetLocation.getDescription());
            if (currentLocation != null) {
                LocationDescription currentShootLocation = new LocationDescription(currentLocation);
                targetDistance.setText(getStats(currentShootLocation, targetLocation));
            }else
            {
                targetDistance.setText(R.string.waiting_for_location);
            }
        }
        else {

            targetInfo.setVisibility(View.GONE);
        }
        targetInfo.invalidate();
    }


}
