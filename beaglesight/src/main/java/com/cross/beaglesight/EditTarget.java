package com.cross.beaglesight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cross.beaglesight.fragments.LocationFragment;
import com.cross.beaglesightlibs.LocationDescription;
import com.cross.beaglesightlibs.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.cross.beaglesight.EditLocation.LOCATION_KEY;

public class EditTarget extends AppCompatActivity implements LocationFragment.OnLocationFragmentInteractionListener {

    private static final int EDIT_LOCATION = 1;
    static final String TARGET_KEY = "target";
    private Target target;
    private List<LocationFragment> locationFragments = new ArrayList<>();
    private EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_target);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        target = getIntent().getParcelableExtra(TARGET_KEY);
        if (target == null) {
            target = new Target();
            target.setTargetLocation(new LocationDescription());
        }

        // If the target is a built-in, duplicate it rather than editting the built-in version.
        // The built-in one can be deleted by the user.
        if (target.isBuiltin()) {
            target.setBuiltin(false);
            target.setId(UUID.randomUUID().toString());
            for (LocationDescription shootPos : target.getShootLocations())
            {
                shootPos.setTargetId(target.getId());
            }
        }
        name = findViewById(R.id.textName);

        final FloatingActionButton fab = findViewById(R.id.fabAddSight);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setEnabled(false);
                fab.invalidate();
                target.setName(name.getText().toString());

                Intent intent = new Intent();
                intent.putExtra(TARGET_KEY, target);
                setResult(RESULT_OK, intent);
                finish();
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
                        target.addShootLocation(location);
                    }
                }
        }
    }

    @Override
    public void onDelete(LocationDescription item) {
        target.removeShootLocation(item);
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

        for (LocationDescription locationDescription : target.getShootLocations()) {
            LocationFragment locationFragment = LocationFragment.newInstance(locationDescription, true);
            locationFragments.add(locationFragment);
            fragmentTransaction.add(R.id.shootLocationsList, locationFragment);
        }

        fragmentTransaction.commit();
    }
}
