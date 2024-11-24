package com.example.mybestlocation.ui.slideshow;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentFriendsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FriendsFragment extends Fragment implements OnMapReadyCallback {

    private FragmentFriendsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialisation de SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapfriends);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Gestion du clic sur le bouton pour envoyer un SMS
        binding.btnenvoyer.setOnClickListener(v -> {
            String numero = binding.etnumero.getText().toString().trim();
            if (!numero.isEmpty()) {
                // Envoi du SMS
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(numero, null,
                        "FINDFRIENDS : Envoie moi ta position, s'il te plaît",
                        null, null);
                Toast.makeText(getContext(), "SMS envoyé à " + numero, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Veuillez saisir un numéro", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Ajoutez un marker sur la carte avec des coordonnées par défaut
        LatLng defaultPosition = new LatLng(48.8584, 2.2945); // Exemple : Paris
        googleMap.addMarker(new MarkerOptions().position(defaultPosition).title("Votre position"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 15));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}