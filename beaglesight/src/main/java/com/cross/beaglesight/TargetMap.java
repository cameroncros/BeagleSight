package com.cross.beaglesight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cross.beaglesightlibs.LocationDescription;
import com.cross.beaglesightlibs.Target;
import com.cross.beaglesightlibs.TargetManager;
import com.cross.beaglesightlibs.XmlParser;
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

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import static android.view.View.GONE;

public class TargetMap extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnMarkerClickListener {
    private static final int FILE_SELECT_CODE = 1;
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
        getMenuInflater().inflate(R.menu.menu_target_map, menu);
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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    String[] permissions = new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    };
                    ActivityCompat.requestPermissions(this, permissions, 1);
                    return true;
                }
                importConfig();
                return true;
            case R.id.action_export:
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        File outputDir = getCacheDir();
                        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        shareIntent.setType("text/xml");
                        ArrayList<Uri> uris = new ArrayList<>();

                        for (Target target : tm.targetDao().getAll()) {
                            List<LocationDescription> shootPositions = tm.locationDescriptionDao().getLocationsForTarget(target.getId());

                            try {
                                String filename = target.getId();

                                File outputFile = File.createTempFile("Target_" + filename, ".xml", outputDir);
                                FileOutputStream fos = new FileOutputStream(outputFile);
                                XmlParser.serialiseTarget(fos, target, shootPositions);

                                Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, outputFile);
                                uris.add(uri);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                        startActivity(Intent.createChooser(shareIntent, "Export Configs"));
                    }
                });
                return true;
            case R.id.action_update:
                //startActivity(new Intent(this, SyncTargets.class));
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

    private void importConfig() {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("file/*"); // intent type to filter application based on your requirement
        startActivityForResult(fileIntent, FILE_SELECT_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    try {
                        Uri uri = data.getData();

                        Log.d("BeagleSight", "File Uri: " + uri.toString());
                        // Get the path

                        File fname = new File(getRealPathFromURI(uri));

                        FileInputStream fis = new FileInputStream(fname);
                        final List<Target> targets = new ArrayList<>();
                        final List<LocationDescription> shootPositions = new ArrayList<>();
                        XmlParser.parseTargetXML(fis, targets, shootPositions);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                for (Target target : targets) {
                                    tm.targetDao().insertAll(target);
                                }
                                for (LocationDescription locationDescription : shootPositions) {
                                    tm.locationDescriptionDao().insertAll(locationDescription);
                                }
                            }
                        });
                    } catch (SAXException | ParserConfigurationException | IOException | NullPointerException e) {
                        Toast.makeText(this, "Failed to load BowConfig: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                recreate();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}