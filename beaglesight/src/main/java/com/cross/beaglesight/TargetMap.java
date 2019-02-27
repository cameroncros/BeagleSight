package com.cross.beaglesight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cross.beaglesight.views.BowConfigAdapter;
import com.cross.beaglesight.views.LockStatusView;
import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.BowManager;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import static android.view.View.GONE;
import static com.cross.beaglesight.EditTarget.TARGET_KEY;

public class TargetMap extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnMarkerClickListener {
    static final int FILE_SELECT_CODE = 1;
    private static final int IMPORT_FILES = 2;
    private static final int TRACK_LOCATION = 3;
    private static final int ADD_TARGET = 4;
    private volatile GoogleMap mMap = null;
    private TargetManager tm;
    private LocationManager locationManager;
    private Boolean trackLocation = true;
    private Location currentLocation = null;
    private ProgressBar progressBar;
    private FloatingActionButton refocusButton;
    private List<Target> targets = new ArrayList<>();
    private Map<Marker, Target> markerOptionsTargetMap = new HashMap<>();
    private Map<Marker, LocationDescription> markerOptionsLocationDescriptionMap = new HashMap<>();
    private View targetInfo;
    private TextView targetDescription;
    private TextView targetDistance;
    private LockStatusView targetStatus;
    private LockStatusView lockStatusView;
    private LocationDescription selectedShootLocation = null;
    private Target selectedTarget = null;
    private ProgressDialog mProgressDialog;
    private boolean isTracking = false;
    private ImageButton editButton;
    private ImageButton deleteButton;
    private BowConfig selectedBow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        targetInfo = findViewById(R.id.targetInfo);
        targetDescription = findViewById(R.id.targetDescription);
        targetDistance = findViewById(R.id.targetDistance);
        targetStatus = findViewById(R.id.targetStatus);

        MapView mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        mMapView.getMapAsync(this);
        mMapView.onResume(); // needed to get the map to display immediately

        // Initialise TargetManager
        tm = TargetManager.getInstance(this);

        progressBar = findViewById(R.id.fabProgress);
        lockStatusView = findViewById(R.id.lockStatusView);

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

        FloatingActionButton arButton = findViewById(R.id.arMode);
        arButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TargetMap.this, TargetAR.class));
            }
        });

        mProgressDialog = new ProgressDialog(TargetMap.this);
        mProgressDialog.setMessage(getString(R.string.download_message));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);

        final Spinner bowChooser = findViewById(R.id.selectedBow);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<BowConfig> configs = BowManager.getInstance(TargetMap.this).getAllBowConfigsWithPositions();
                final BowConfigAdapter adapter = new BowConfigAdapter(configs, getResources().getColor(R.color.colorPrimaryDark));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bowChooser.setAdapter(adapter);
                        bowChooser.invalidate();
                    }
                });
            }
        });
        bowChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBow = (BowConfig)adapterView.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedBow = null;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void trackLocation() {
        synchronized (this) {
            if (!isTracking) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                mMap.setMyLocationEnabled(true);
                isTracking = true;
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        super.onPause();
        synchronized (this) {
            if (isTracking) {
                locationManager.removeUpdates(this);
                mMap.setMyLocationEnabled(false);
                isTracking = false;
            }
        }
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
        switch (id) {
            case R.id.action_add:
                startActivityForResult(new Intent(this, EditTarget.class), ADD_TARGET);
                return true;
            case R.id.action_import:
                String[] permissions = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };
                ActivityCompat.requestPermissions(this, permissions, IMPORT_FILES);
                return true;
            case R.id.action_export:
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        File outputDir = getCacheDir();
                        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        shareIntent.setType("text/xml");
                        ArrayList<Uri> uris = new ArrayList<>();

                        List<Target> targets = tm.getTargetsWithShootPositions(true);
                        try {
                            String filename = new Date().toString();

                            File outputFile = File.createTempFile("Target_" + filename, ".xml", outputDir);
                            FileOutputStream fos = new FileOutputStream(outputFile);
                            XmlParser.serialiseTargets(fos, targets);

                            Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, outputFile);
                            uris.add(uri);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                        startActivity(Intent.createChooser(shareIntent, "Export Configs"));
                    }
                });
                return true;
            case R.id.action_update:
                try {
                    File defaultsFile = File.createTempFile("Targets", ".xml");
                    mProgressDialog.show();
                    Intent intent = new Intent(this, DownloadService.class);
                    intent.putExtra(DownloadService.URL, getString(R.string.targets_url));
                    intent.putExtra(DownloadService.PATH, defaultsFile.getAbsolutePath());
                    intent.putExtra(DownloadService.RECEIVER, new DownloadReceiver(new Handler()));
                    startService(intent);
                } catch (IOException ignored) {
                    Toast.makeText(this, "Can't download, failed to create temp file", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        switch (requestCode) {
            case TRACK_LOCATION:
                trackLocation();
                break;
            case IMPORT_FILES:
                importConfig();
                break;
        }
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                String[] permissions = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };
                ActivityCompat.requestPermissions(this, permissions, TRACK_LOCATION);
            }
        } else {
            trackLocation();
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
                targets = tm.getTargetsWithShootPositions(false);
                renderTargets();
            }
        });
    }

    private String getStats(LocationDescription shootLocation,
                            LocationDescription targetLocation,
                            BowConfig selectedBow) {
        String stats = String.format(Locale.ENGLISH, "Distance: %.2fm Pitch: %.2f° ",
                shootLocation.distanceTo(targetLocation),
                shootLocation.pitchTo(targetLocation));
        if (selectedBow != null)
        {
            float distance = (float)shootLocation.distanceTo(targetLocation);
            float pin = selectedBow.getPositionCalculator().calcPosition(distance);
            String pinSetting = String.format(Locale.ENGLISH, "Pin: %.1f", pin);
            stats = stats + pinSetting;
        }
        return stats;
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

                for (Target target : targets) {
                    LocationDescription targetLocation = target.getTargetLocation();
                    LatLng targetPos = targetLocation.getLatLng();

                    float targetColor = BitmapDescriptorFactory.HUE_RED;
                    float shootColor = BitmapDescriptorFactory.HUE_ROSE;
                    if (target.isBuiltin())
                    {
                        targetColor = BitmapDescriptorFactory.HUE_ORANGE;
                        shootColor = BitmapDescriptorFactory.HUE_YELLOW;
                    }

                    Marker targetMarker = mMap.addMarker(new MarkerOptions()
                            .position(targetPos)
                            .title(target.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(targetColor)));
                    markerOptionsTargetMap.put(targetMarker, target);

                    List<LocationDescription> shootLocations = target.getShootLocations();
                    if (shootLocations != null) {
                        for (LocationDescription shootLocation : shootLocations) {
                            LatLng shootPos = shootLocation.getLatLng();
                            Marker shootMarker = mMap.addMarker(new MarkerOptions()
                                    .position(shootPos)
                                    .title(shootLocation.getDescription())
                                    .icon(BitmapDescriptorFactory.defaultMarker(shootColor)));
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
            refocusButton.hide();
            refocusButton.invalidate();
            Float zoom = 20f;
            Float tilt = 0f;
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraPosition cameraPosition = new CameraPosition(latLng, zoom, tilt, currentLocation.getBearing());
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lockStatusView.updateLocation(location);
        lockStatusView.invalidate();
        currentLocation = location;
        progressBar.setVisibility(View.GONE);
        progressBar.invalidate();
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

        if (markerOptionsLocationDescriptionMap.containsKey(marker)) {
            selectedShootLocation = markerOptionsLocationDescriptionMap.get(marker);
            selectedTarget = markerOptionsTargetMap.get(marker);
        } else if (markerOptionsTargetMap.containsKey(marker)) {
            selectedTarget = markerOptionsTargetMap.get(marker);
        }

        updateTargetInfo();
        return false;
    }

    public void updateTargetInfo() {
        if (selectedTarget != null)
        {
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(TargetMap.this, EditTarget.class);
                    intent.putExtra(TARGET_KEY, selectedTarget);
                    startActivityForResult(intent, ADD_TARGET);
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            tm.deleteTarget(selectedTarget);
                            getTargets();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    targetInfo.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                }
            });
        }

        if (selectedShootLocation != null && selectedTarget != null) {
            targetInfo.setVisibility(View.VISIBLE);
            targetDescription.setText(selectedShootLocation.getDescription());
            targetDistance.setText(getStats(selectedShootLocation, selectedTarget.getTargetLocation(), selectedBow));
            targetStatus.setStatus(selectedShootLocation.getLockStatus());
        } else if (selectedTarget != null) {
            LocationDescription targetLocation = selectedTarget.getTargetLocation();
            targetInfo.setVisibility(View.VISIBLE);
            targetDescription.setText(targetLocation.getDescription());
            if (currentLocation != null) {
                LocationDescription currentShootLocation = new LocationDescription(currentLocation);
                targetDistance.setText(getStats(currentShootLocation, targetLocation, selectedBow));
            } else {
                targetDistance.setText(R.string.waiting_for_location);
            }
            targetStatus.setStatus(targetLocation.getLockStatus());
        } else {
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
                    Uri uri = data.getData();
                    Log.d("BeagleSight", "File Uri: " + uri.toString());

                    // Get the path
                    File fname = new File(getRealPathFromURI(uri));
                    importXMLFile(fname);
                }
                recreate();
                break;
            case ADD_TARGET:
                if (resultCode == RESULT_OK)
                {
                    final Target target = data.getParcelableExtra(TARGET_KEY);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            tm.deleteTarget(target);
                            tm.saveTarget(target);

                            getTargets();
                        }
                    });
                }
                recreate();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void importXMLFile(File fname) {
        try {
            FileInputStream fis = new FileInputStream(fname);
            final List<Target> targets = XmlParser.parseTargetsXML(fis);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    tm.saveTargets(targets);
                    getTargets();
                }
            });
        } catch (IOException | ParserConfigurationException | SAXException e) {
            Toast.makeText(this, "Failed to import file", Toast.LENGTH_LONG).show();
        }
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

    private class DownloadReceiver extends ResultReceiver {
        DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                int progress = resultData.getInt(DownloadService.PROGRESS);
                String filePath = resultData.getString(DownloadService.PATH);
                File file = new File(filePath);
                mProgressDialog.setProgress(progress);
                if (progress == 100) {

                    boolean success = resultData.getBoolean(DownloadService.RESULT);
                    mProgressDialog.dismiss();

                    if (success) {
                        importXMLFile(file);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Failed to download file",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
}
