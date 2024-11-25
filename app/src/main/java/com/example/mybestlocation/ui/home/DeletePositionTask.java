package com.example.mybestlocation.ui.home;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mybestlocation.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeletePositionTask extends AsyncTask<String, Void, String> {

    private Context context;
    private DeleteCallback callback;

    public DeletePositionTask(Context context, DeleteCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        String idposition = params[0];  // The position ID to delete
        String result = "";

        try {
            // Build URL and open connection
            URL url = new URL(Config.Url_DeletePosition);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Prepare data to be sent
            String postData = "idposition=" + idposition;

            // Send the data
            try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
                writer.writeBytes(postData);
                writer.flush();
            }

            // Get the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            result = response.toString();
        } catch (Exception e) {
            Log.e("DeletePositionTask", "Error in deleting position", e);
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result.contains("\"success\": 1")) {
            // Successfully deleted the position
            Log.d("DeletePositionTask", "Position deleted successfully");
            callback.onDeleteSuccess(); // Notify success to the fragment
        } else {
            // Failed to delete position
            Log.d("DeletePositionTask", "Failed to delete position");
            callback.onDeleteFailure(); // Notify failure to the fragment
        }
    }

    // Callback interface
    public interface DeleteCallback {
        void onDeleteSuccess();
        void onDeleteFailure();
    }
}
