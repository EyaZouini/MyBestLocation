package com.example.mybestlocation.ui.home;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.Position;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class Download extends AsyncTask<Void, Position, Void> {

    private AlertDialog alert;
    private GoogleMap mMap;
    private ArrayList<Position> data;
    private ListView listView;  // ListView ici, pas ArrayAdapter

    public Download(GoogleMap map, ArrayList<Position> data, ListView listView) {
        this.mMap = map;
        this.data = data;
        this.listView = listView;
    }

    @Override
    protected void onPreExecute() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(listView.getContext());
        dialog.setTitle("Téléchargement");
        dialog.setMessage("Veuillez patienter...");
        alert = dialog.create();
        alert.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Thread.sleep(1000); // Simule un délai
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Récupère les données depuis le backend
        JSONParser parser = new JSONParser();
        JSONObject response = parser.makeRequest(Config.Url_GetAll);

        try {
            int success = response.getInt("success");
            if (success > 0) {
                JSONArray tab = response.getJSONArray("positions");
                for (int i = 0; i < tab.length(); i++) {
                    JSONObject ligne = tab.getJSONObject(i);
                    int idposition = ligne.getInt("idposition");
                    String pseudo = ligne.getString("pseudo");
                    String longitude = ligne.getString("longitude");
                    String latitude = ligne.getString("latitude");
                    String type = ligne.getString("type");

                    // Vérifie si la position existe déjà
                    boolean exists = false;
                    for (Position position : data) {
                        if (position.getIdposition() == idposition) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        data.add(new Position(idposition, pseudo, longitude, latitude, type));
                        publishProgress(new Position(idposition, pseudo, longitude, latitude,type));  // Met à jour le progrès
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Position... values) {
        super.onProgressUpdate(values);
        Position position = values[0];

        // Ajoute la nouvelle position dans le ListView
        if (listView != null) {
            ArrayAdapter<Position> adapter = new ArrayAdapter<>(listView.getContext(), android.R.layout.simple_list_item_1, data);
            listView.setAdapter(adapter);
        }

        // Ajoute un marqueur sur la carte
        if (mMap != null) {
            double lat = Double.parseDouble(position.getLatitude());
            double lng = Double.parseDouble(position.getLongitude());
            LatLng latLng = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(latLng).title(position.getPseudo()));
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (alert != null && alert.isShowing()) {
            alert.dismiss();
        }
    }
}
