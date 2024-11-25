package com.example.mybestlocation.ui.slideshow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentFriendsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FriendsFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_LOCATION = 100;
    private static final int PERMISSION_REQUEST_SEND_SMS = 123;
    private FragmentFriendsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialisation de SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapfriends);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialisation du FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Gestion du clic sur le bouton pour envoyer un SMS
        binding.btnenvoyer.setOnClickListener(v -> {
            String numero = binding.etnumero.getText().toString().trim();
            if (!numero.isEmpty()) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Demande de permission pour envoyer un SMS
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
                "FINDFRIENDS : Envoie moi ta position, s'il te plaît",
                null, null);
        Toast.makeText(getContext(), "SMS envoyé à " + numero, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Vérifiez les permissions de localisation
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            return;
        }

        // Activer la localisation et déplacer la caméra vers la position actuelle
        googleMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Ma position actuelle"));
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
                // Récupérer le numéro et envoyer le SMS après permission
                String numero = binding.etnumero.getText().toString().trim();
                if (!numero.isEmpty()) {
                    envoyerSms(numero);
                }
            } else {
                Toast.makeText(getContext(), "Permission refusée pour envoyer des SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}