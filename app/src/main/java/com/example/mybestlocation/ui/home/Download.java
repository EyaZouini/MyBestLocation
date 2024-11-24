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
    private final GoogleMap mMap;
    private final ArrayList<Position> data;
    private final ListView listView;

    public Download(GoogleMap map, ArrayList<Position> data, ListView listView) {
        this.mMap = map;
        this.data = data;
        this.listView = listView;
    }

    @Override
    protected void onPreExecute() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(listView.getContext());
        dialog.setTitle("Téléchargement")
                .setMessage("Veuillez patienter...");
        alert = dialog.create();
        alert.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Thread.sleep(1000); // Simulate a delay
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        JSONParser parser = new JSONParser();
        JSONObject response = parser.makeRequest(Config.Url_GetAll);

        try {
            if (response.getInt("success") > 0) {
                JSONArray positionsArray = response.getJSONArray("positions");
                for (int i = 0; i < positionsArray.length(); i++) {
                    JSONObject positionObject = positionsArray.getJSONObject(i);
                    int idposition = positionObject.getInt("idposition");
                    String pseudo = positionObject.getString("pseudo");
                    String longitude = positionObject.getString("longitude");
                    String latitude = positionObject.getString("latitude");
                    String type = positionObject.getString("type");

                    // Check if position already exists
                    boolean exists = false;
                    for (Position position : data) {
                        if (position.getIdposition() == idposition) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        Position newPosition = new Position(idposition, pseudo, longitude, latitude, type);
                        data.add(newPosition);
                        publishProgress(newPosition); // Update progress
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
        Position position = values[0];

        // Update the ListView with new data
        if (listView != null) {
            ArrayAdapter<Position> adapter = new ArrayAdapter<>(listView.getContext(), android.R.layout.simple_list_item_1, data);
            listView.setAdapter(adapter);
        }

        // Add a marker to the map
        if (mMap != null) {
            LatLng latLng = new LatLng(Double.parseDouble(position.getLatitude()), Double.parseDouble(position.getLongitude()));
            mMap.addMarker(new MarkerOptions().position(latLng).title(position.getPseudo()));
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (alert != null && alert.isShowing()) {
            alert.dismiss();
        }
    }
}
