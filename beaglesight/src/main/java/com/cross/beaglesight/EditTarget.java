package com.cross.beaglesight;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cross.beaglesight.fragments.LocationFragment;
import com.cross.beaglesightlibs.LocationDescription;
import com.cross.beaglesightlibs.Target;
import com.cross.beaglesightlibs.TargetManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class EditTarget extends AppCompatActivity implements LocationFragment.OnLocationFragmentInteractionListener {

    private static final int EDIT_LOCATION = 1;
    static final String LOCATION_KEY = "location";
    private Target target;
    private Map<String, LocationDescription> shootLocations;
    private TargetManager tm;
    private List<LocationFragment> locationFragments = new ArrayList<>();
    private EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_target);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tm = TargetManager.getInstance(this);

        target = new Target();
        target.setTargetLocation(new LocationDescription());

        name = findViewById(R.id.textName);

        shootLocations = new HashMap<>();

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setEnabled(false);
                fab.invalidate();
                target.setName(name.getText().toString());

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        tm.targetDao().insertAll(target);
                        for (LocationDescription locationDescription : shootLocations.values()) {
                            tm.locationDescriptionDao().insertAll(locationDescription);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                });
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Button addTarget = findViewById(R.id.addButton);
        addTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editLocationIntent = new Intent(getApplicationContext(), EditLocation.class);
                LocationDescription shootLocation = new LocationDescription();
                shootLocation.setLocationId(UUID.randomUUID().toString());
                shootLocation.setTargetId(target.getId());
                editLocationIntent.putExtra(LOCATION_KEY, shootLocation);
                startActivityForResult(editLocationIntent, EDIT_LOCATION);
            }
        });

        redraw();
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        redraw();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT_LOCATION:
                if (resultCode == RESULT_OK) {
                    LocationDescription location = data.getParcelableExtra(LOCATION_KEY);
                    if (target.getTargetLocation().getLocationId().equals(location.getLocationId())) {
                        target.setTargetLocation(location);
                    } else {
                        shootLocations.put(location.getLocationId(), location);
                    }
                }
        }
    }

    @Override
    public void onDelete(LocationDescription item) {
        shootLocations.remove(item.getLocationId());
        redraw();
    }

    @Override
    public void onEdit(LocationDescription item) {
        Intent editLocationIntent = new Intent(getApplicationContext(), EditLocation.class);
        editLocationIntent.putExtra(LOCATION_KEY, item);
        startActivityForResult(editLocationIntent, EDIT_LOCATION);
    }

    private void redraw() {
        // Redraw view
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        // Begin Fragment transaction.
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        LocationFragment targetFragment = LocationFragment.newInstance(target.getTargetLocation(), false);
        fragmentTransaction.replace(R.id.fragmentTarget, targetFragment);

        for (LocationFragment locationFragment : locationFragments) {
            fragmentTransaction.remove(locationFragment);
        }

        for (LocationDescription locationDescription : shootLocations.values()) {
            LocationFragment locationFragment = LocationFragment.newInstance(locationDescription, true);
            locationFragments.add(locationFragment);
            fragmentTransaction.add(R.id.shootLocationsList, locationFragment);
        }

        fragmentTransaction.commit();
    }
}
