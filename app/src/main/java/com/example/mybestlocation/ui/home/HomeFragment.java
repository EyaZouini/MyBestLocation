package com.example.mybestlocation.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ArrayList<Position> data = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initializeLocationServices();

        setupSearchBar();
        setupMap();

        return root;
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

    private void setupSearchBar() {
        binding.searchBar.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
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
        new Download(mMap, data, binding.lv).execute();
        setupListView();
    }

    private void setupListView() {
        ArrayAdapter<Position> adapter = new ArrayAdapter<Position>(requireActivity(), android.R.layout.simple_list_item_1, data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setText(data.get(position).toString());
                return view;
            }
        };

        binding.lv.setAdapter(adapter);
        binding.lv.setOnItemClickListener((parent, view, position, id) -> {
            Position selectedPosition = data.get(position);
            LatLng latLng = new LatLng(
                    Double.parseDouble(selectedPosition.getLatitude()),
                    Double.parseDouble(selectedPosition.getLongitude())
            );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(selectedPosition.getPseudo()));
        });
    }

    private void filterList(String query) {
        ArrayList<Position> filteredData = new ArrayList<>();
        for (Position position : data) {
            if (position.getPseudo().toLowerCase().contains(query.toLowerCase()) ||
                    position.getType().toLowerCase().contains(query.toLowerCase())) {
                filteredData.add(position);
            }
        }

        ArrayAdapter<Position> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, filteredData);
        binding.lv.setAdapter(adapter);
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
