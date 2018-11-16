package com.cross.beaglesight;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cross.beaglesightlibs.LocationDescription;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import static com.cross.beaglesight.EditTarget.LOCATION_KEY;

public class EditLocation extends AppCompatActivity implements LocationListener {
    private LocationDescription locationDescription;
    private TextView longitude;
    private TextView latitude;
    private TextView altitude;
    private TextView longitude_accuracy;
    private TextView latitude_accuracy;
    private TextView altitude_accuracy;
    private EditText description;
    private boolean updateLocation = true;

    private DecimalFormat gpsFormatter = new DecimalFormat("####0.000000");
    private DecimalFormat altFormatter = new DecimalFormat("####0.00");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        locationDescription = getIntent().getParcelableExtra(LOCATION_KEY);

        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        altitude = findViewById(R.id.altitude);
        longitude_accuracy = findViewById(R.id.longitude_accuracy);
        latitude_accuracy = findViewById(R.id.latitude_accuracy);
        altitude_accuracy = findViewById(R.id.altitude_accuracy);

        description = findViewById(R.id.description);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, permissions, 1);
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationDescription.setDescription(description.getText().toString());

                Intent intent = new Intent();
                intent.putExtra(LOCATION_KEY, locationDescription);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        final Button lock_location = findViewById(R.id.lock_location);
        lock_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLocation = !updateLocation;
                if (updateLocation)
                {
                    lock_location.setText(R.string.lock_location);
                    lock_location.invalidate();
                }
                else
                {
                    lock_location.setText(R.string.acquire_location);
                    lock_location.invalidate();
                }
            }
        });

        fillViews(locationDescription);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (updateLocation) {
            locationDescription.setLongitude(location.getLongitude());
            locationDescription.setLatitude(location.getLatitude());
            locationDescription.setAltitude(location.getAltitude());
            locationDescription.setLatlng_accuracy(location.getAccuracy());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                locationDescription.setAltitude_accuracy(location.getVerticalAccuracyMeters());
            }

            fillViews(locationDescription);
        }
    }

    private void fillViews(LocationDescription location) {
        longitude.setText(gpsFormatter.format(location.getLongitude()));
        latitude.setText(gpsFormatter.format(location.getLatitude()));
        altitude.setText(altFormatter.format(location.getAltitude()));
        longitude_accuracy.setText(String.format(getString(R.string.plus_minus), altFormatter.format(location.getLatlng_accuracy())));
        latitude_accuracy.setText(String.format(getString(R.string.plus_minus), altFormatter.format(location.getLatlng_accuracy())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            altitude_accuracy.setText(String.format(getString(R.string.plus_minus), altFormatter.format(location.getAltitude_accuracy())));
        }

        longitude.invalidate();
        latitude.invalidate();
        altitude.invalidate();
        longitude_accuracy.invalidate();
        latitude_accuracy.invalidate();
        altitude_accuracy.invalidate();
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
}