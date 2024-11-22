package com.example.mybestlocation.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.databinding.FragmentHomeBinding;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    ArrayList<Position> data = new ArrayList<Position>();
    private GoogleMap mMap;  // Déclarez votre carte Google Map

    private FragmentHomeBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Configurez la carte lorsque le fragment est prêt
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.btnDownload.setOnClickListener(view -> {
            Download d = new Download();
            d.execute();
        });

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Vérification des permissions avant de permettre l'accès à la localisation
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Récupérer la position actuelle
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Log.d("Location", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                                // Déplacer la caméra et ajouter un marqueur
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                            }else {
                                Log.d("Location", "Location is null");
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize the map
                onMapReady(mMap);
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(getActivity(), "Permission denied to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class Download extends AsyncTask {

        AlertDialog alert;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Téléchargement");
            dialog.setMessage("Veuillez patienter...");

            alert = dialog.create();
            alert.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Code du second thread : pas accès à l'IHM
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.Url_GetAll);

            try {
                int success = response.getInt("success");
                if (success > 0) {
                    JSONArray tab = response.getJSONArray("positions");
                    for (int i = 0; i < tab.length(); i++) {
                        JSONObject lignes = tab.getJSONObject(i);
                        int idposition = lignes.getInt("idposition");
                        String pseudo = lignes.getString("pseudo");
                        String longitude = lignes.getString("longitude");
                        String latitude = lignes.getString("latitude");

                        data.add(new Position(idposition, pseudo, longitude, latitude));
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            // Mettez à jour l'adaptateur de la ListView
            binding.lv.setAdapter(new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.simple_list_item_1,
                    data
            ));
            alert.dismiss();

            // Mettez à jour les marqueurs sur la carte après le téléchargement des données
            if (mMap != null) {
                for (Position position : data) {
                    double lat = Double.parseDouble(position.getLatitude());
                    double lng = Double.parseDouble(position.getLongitude());
                    LatLng latLng = new LatLng(lat, lng);

                    mMap.addMarker(new MarkerOptions().position(latLng).title(position.getPseudo()));
                }

                // Déplacer la caméra sur la première position
                LatLng firstPosition = new LatLng(Double.parseDouble(data.get(0).getLatitude()), Double.parseDouble(data.get(0).getLongitude()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 10));
            }
        }
    }
}
