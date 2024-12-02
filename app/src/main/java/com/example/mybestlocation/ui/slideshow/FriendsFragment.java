package com.example.mybestlocation.ui.slideshow;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentFriendsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FriendsFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_LOCATION = 100;
    private static final int PERMISSION_REQUEST_SEND_SMS = 123;
    private FragmentFriendsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;  // Déclarer la variable mMap ici

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapfriends);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Handle button click to send SMS
        binding.btnenvoyer.setOnClickListener(v -> {
            String numero = binding.etnumero.getText().toString().trim();
            if (!numero.isEmpty()) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request permission to send SMS
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
                } else {
                    envoyerSms(numero);
                }
            } else {
                Toast.makeText(getContext(), "Veuillez saisir un numéro", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void envoyerSms(String numero) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(numero, null,
                "FINDFRIENDS : Envoyer moi votre position",
                null, null);
        Toast.makeText(getContext(), "SMS envoyé à " + numero, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Initialize mMap with the GoogleMap reference
        mMap = googleMap;

        // Check location permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            return;
        }

        // Enable location and move camera to current position
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Ma position actuelle").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            } else {
                Toast.makeText(getContext(), "Impossible d'obtenir la position actuelle", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapfriends);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }
            } else {
                Toast.makeText(getContext(), "Permission refusée pour la localisation", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Retrieve number and send SMS after permission
                String numero = binding.etnumero.getText().toString().trim();
                if (!numero.isEmpty()) {
                    envoyerSms(numero);
                }
            } else {
                Toast.makeText(getContext(), "Permission refusée pour envoyer des SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Récupérer les coordonnées depuis l'intent
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);

            // Mettre à jour la carte avec les nouvelles coordonnées
            afficherPositionSurCarte(latitude, longitude);
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        // Enregistrer le récepteur pour écouter les mises à jour de position
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(locationReceiver, new IntentFilter("com.example.mybestlocation.LOCATION_UPDATE"));
    }

    @Override
    public void onPause() {
        super.onPause();
        // Désinscrire le récepteur lorsque le fragment est en pause
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(locationReceiver);
    }

    public void afficherPositionSurCarte(double latitude, double longitude) {
        if (mMap != null) {
            LatLng newLocation = new LatLng(latitude, longitude);

            // Ajouter un nouveau marqueur à la carte
            mMap.addMarker(new MarkerOptions().position(newLocation)
                    .title("Position de l'ami")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Déplacer la caméra vers cette position
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));
        }
    }



}
