package com.example.mybestlocation.ui.createPosition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentCreatePositionBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.HashMap;

public class CreatePositionFragment extends Fragment implements OnMapReadyCallback {

    private FragmentCreatePositionBinding binding;
    private GoogleMap mMap;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreatePositionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Setup MapView
        mapView = binding.mapCreatePosition;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Configure Spinner for position types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.position_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.typeSpinner.setAdapter(adapter);

        // Save button listener
        binding.btnSavePosition.setOnClickListener(view -> {
            String name = binding.nameInput.getText().toString();
            String type = binding.typeSpinner.getSelectedItem().toString();

            if (selectedLocation != null && !TextUtils.isEmpty(name)) {
                savePosition(selectedLocation.latitude, selectedLocation.longitude, name, type);
            } else {
                Toast.makeText(getContext(), "Please select a location and enter a name", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    /**
     * Save position data to the server.
     */
    private void savePosition(double latitude, double longitude, String name, String type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        params.put("pseudo", name);
        params.put("type", type);

        new SavePositionTask(getContext(), () -> {
            // Reset fields after successful save
            binding.nameInput.setText("");
            binding.typeSpinner.setSelection(0);
            selectedLocation = null;
            if (mMap != null) mMap.clear();
            onMapReady(mMap);
        }).execute(params);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Request location permissions
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);

        // Display user's current location
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            } else {
                Toast.makeText(getContext(), "Unable to fetch current location", Toast.LENGTH_SHORT).show();
            }
        });

        // Set a listener for map clicks to select a location
        mMap.setOnMapClickListener(latLng -> {
            if (selectedLocation != null) {
                mMap.clear();
            }
            selectedLocation = latLng;
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        });
    }

    // Handle MapView lifecycle events
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}