package com.cross.beaglesight.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cross.beaglesight.R;
import com.cross.beaglesight.views.LockStatusView;
import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.LocationDescription;
import com.cross.beaglesightlibs.LockStatus;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.Manifest.permission_group.LOCATION;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private static final String LOCATION_KEY = "location";
    private static final String DELETABLE_KEY = "deletable";
    private LocationDescription locationDescription;
    private boolean deletable;
    private OnLocationFragmentInteractionListener mListener;

    public LocationFragment() {
        // Required empty public constructor
        deletable = false;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LocationFragment.
     */
    public static LocationFragment newInstance(LocationDescription locationDescription, boolean deletable) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putParcelable(LOCATION_KEY, locationDescription);
        args.putBoolean(DELETABLE_KEY, deletable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            locationDescription = getArguments().getParcelable(LOCATION_KEY);
            deletable = getArguments().getBoolean(DELETABLE_KEY);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LocationFragment.OnLocationFragmentInteractionListener) {
            mListener = (LocationFragment.OnLocationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);


        if (locationDescription != null)
        {
            TextView latLong = view.findViewById(R.id.latlong);
            latLong.setText(locationDescription.getLocationString());

            TextView description = view.findViewById(R.id.description);
            description.setText(locationDescription.getDescription());

            LockStatusView lockStatusView = view.findViewById(R.id.lockStatusView);
            lockStatusView.setStatus(locationDescription.getLockStatus());
        }

        MapView mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.setClickable(false);
        mapView.onResume(); // needed to get the map to display immediately


        try {
            MapsInitializer.initialize(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(this);

        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);

        ImageButton editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onEdit(locationDescription);
            }
        });
        ImageButton deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDelete(locationDescription);
            }
        });
        if (!deletable)
        {
            deleteButton.setVisibility(View.INVISIBLE);
            deleteButton.invalidate();
        }
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (locationDescription != null) {
            LatLng latLng = locationDescription.getLatLng();
            CameraPosition cameraPosition = new CameraPosition(latLng, 20, 0, 0);
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.addMarker(new MarkerOptions().position(latLng));

            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setAllGesturesEnabled(false);
        }
    }

    public interface OnLocationFragmentInteractionListener {
        void onDelete(LocationDescription item);
        void onEdit(LocationDescription item);
    }
}
