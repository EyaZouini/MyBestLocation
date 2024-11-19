package com.example.mybestlocation.ui.home;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    ArrayList<Position> data= new ArrayList<Position>();

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Download d= new Download();
                    d.execute();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class Download extends AsyncTask {

        AlertDialog alert ;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Telechargement");
            dialog.setMessage("Veiller patientez...");

            alert= dialog.create();
            alert.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //code du second thread : pas acces a l'IHM
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.Url_GetAll);
            System.out.println(response);

            try {
                int success= response.getInt("success");
                if (success>0)
                {
                    JSONArray tab= response.getJSONArray("positions");
                    for (int i = 0; i < tab.length(); i++) {
                        JSONObject lignes = tab.getJSONObject(i);
                        int idposition= lignes.getInt("idposition");
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
            binding.lv.setAdapter(new ArrayAdapter(
                    getActivity(),
                    android.R.layout.simple_list_item_1,
                    data
                    ));
            alert.dismiss();
        }
    }
}