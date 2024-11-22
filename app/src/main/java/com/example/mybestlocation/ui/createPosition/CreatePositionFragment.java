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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mapView = binding.mapCreatePosition;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Configuration du Spinner pour le type
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.position_types, // Assurez-vous d'avoir défini cela dans strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.typeSpinner.setAdapter(adapter);

        binding.btnSavePosition.setOnClickListener(view -> {
            if (selectedLocation != null && !TextUtils.isEmpty(binding.nameInput.getText())) {
                String name = binding.nameInput.getText().toString();
                String type = binding.typeSpinner.getSelectedItem().toString(); // Récupère le type sélectionné
                savePosition(selectedLocation.latitude, selectedLocation.longitude, name, type);
                Toast.makeText(getContext(), "Position saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please select a location and enter a name", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }


    private void savePosition(double latitude, double longitude, String name, String type) {
        // Créer une HashMap pour contenir les données
        HashMap<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        params.put("pseudo", name);
        params.put("type", type); // Ajouter le type

        // Exécuter l'AsyncTask avec le contexte
        new SavePositionTask(getContext()).execute(params);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);

        // Get the current location on map load
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
            if (location != null) {
                // Use the same location logic from HomeFragment
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
            } else {
                Toast.makeText(getContext(), "Unable to fetch current location", Toast.LENGTH_SHORT).show();
            }
        });

        // Set a listener to select a location on the map
        mMap.setOnMapClickListener(latLng -> {
            if (selectedLocation != null) {
                mMap.clear(); // Clear the previous marker
            }
            selectedLocation = latLng;
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        });
    }

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
