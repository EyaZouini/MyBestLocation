package com.example.mybestlocation.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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

    ArrayList<Position> data = new ArrayList<>();
    private GoogleMap mMap;  // Google Map object
    private FragmentHomeBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isDataDownloaded = false;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Setup location request
        locationRequest = new LocationRequest();
        locationRequest.setInterval(60000); // 1 minute (in milliseconds)
        locationRequest.setFastestInterval(30000); // 30 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10); // 10 meters minimum displacement

        // Setup location callback to receive location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    android.location.Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d("Location", "Updated Location: Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude());

                        // Update the map with new location
                        LatLng updatedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(updatedLocation, 15));
                        mMap.addMarker(new MarkerOptions().position(updatedLocation).title("Updated Location"));
                    }
                }
            }
        };

        // Configure the map when the fragment is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Automatically download the data when the fragment is created
        if (!isDataDownloaded) {
            new Download(mMap, data, binding.lv).execute();  // Start downloading data
        }

        // Set up the ListView adapter after the data is downloaded
        setUpListView();

        return root;
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

        // Add initial markers for positions already in the list
        for (Position position : data) {
            addMarkerToMap(position);
        }

        // Get the current location on map load
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
            if (location != null) {
                Log.d("Location", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
            } else {
                Log.d("Location", "Location is null");
            }
        }).addOnFailureListener(e -> Log.e("Location", "Error fetching location", e));

        // Start receiving location updates
        startLocationUpdates();
    }

    // Add marker to map for a given position
    private void addMarkerToMap(Position position) {
        if (mMap != null) {
            double lat = Double.parseDouble(position.getLatitude());
            double lng = Double.parseDouble(position.getLongitude());
            LatLng latLng = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(latLng).title(position.getPseudo()));
        }
    }

    // Set up ListView adapter and click listener
    private void setUpListView() {
        // Explicitly define the type of the adapter as Position
        ArrayAdapter<Position> adapter = new ArrayAdapter<Position>(getActivity(), android.R.layout.simple_list_item_1, data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                // Only show the pseudo (name)
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setText(data.get(position).getPseudo());
                return view;
            }
        };

        // Set the adapter to the ListView
        binding.lv.setAdapter(adapter);

        // Set an item click listener to zoom into the selected position on the map
        binding.lv.setOnItemClickListener((parent, view, position, id) -> {
            Position selectedPosition = data.get(position);
            double lat = Double.parseDouble(selectedPosition.getLatitude());
            double lng = Double.parseDouble(selectedPosition.getLongitude());
            LatLng latLng = new LatLng(lat, lng);

            // Zoom into the selected position
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPosition.getPseudo()));
        });
    }


    // Start receiving location updates
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    // Stop location updates when fragment is destroyed to avoid memory leaks
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fusedLocationClient.removeLocationUpdates(locationCallback);  // Stop location updates
        binding = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(getActivity(), "Permission denied to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
