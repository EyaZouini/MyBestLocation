package com.example.mybestlocation.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mybestlocation.R;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.databinding.FragmentHomeBinding;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ArrayList<Position> data = new ArrayList<>();
    private PositionAdapter adapter;
    private SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        searchView = binding.searchBar;  // Initialize the SearchView

        initializeLocationServices();
        setupMap();
        setupSearchView();

        return root;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // You can handle query submission here if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // When the text in the search view changes, filter the list
                filterPositions(newText);
                return true;
            }
        });
    }

    private void filterPositions(String query) {
        ArrayList<Position> filteredList = new ArrayList<>();
        for (Position position : data) {
            // Check if the pseudo or type contains the query text (case-insensitive)
            if (position.getPseudo().toLowerCase().contains(query.toLowerCase()) ||
                    position.getType().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(position);
            }
        }

        // Update the adapter with the filtered list
        adapter.updateList(filteredList);
    }



    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationRequest = LocationRequest.create()
                .setInterval(60000) // 1 minute
                .setFastestInterval(30000) // 30 seconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(10); // 10 meters

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    LatLng updatedLocation = new LatLng(
                            locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude()
                    );
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(updatedLocation, 15));
                    mMap.addMarker(new MarkerOptions()
                            .position(updatedLocation)
                            .title("My Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
            }
        };
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);
        loadPositions();
        startLocationUpdates();
    }

    private void loadPositions() {
        adapter = new PositionAdapter(requireActivity(), data, position -> {
            // Handle the click on the item
            LatLng latLng = new LatLng(
                    Double.parseDouble(position.getLatitude()),
                    Double.parseDouble(position.getLongitude())
            );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(position.getPseudo()));

            Toast.makeText(requireContext(), "Position: " + position.getPseudo(), Toast.LENGTH_SHORT).show();
        });

        binding.rvPositions.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.rvPositions.setAdapter(adapter);

        // Pass the adapter to the Download task
        new Download(mMap, data, adapter).execute();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        binding = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(getActivity(), "Permission denied to access location", Toast.LENGTH_SHORT).show();
        }
    }
}
