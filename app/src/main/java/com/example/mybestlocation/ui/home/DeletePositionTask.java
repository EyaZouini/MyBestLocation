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

    private final Context context;
    private final DeleteCallback callback;

    public DeletePositionTask(Context context, DeleteCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        String idposition = params[0]; // ID of the position to delete
        String result = "";

        try {
            // Configure HTTP connection
            URL url = new URL(Config.Url_DeletePosition);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Send request data
            String postData = "idposition=" + idposition;
            try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
                writer.writeBytes(postData);
                writer.flush();
            }

            // Read response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                result = response.toString();
            }

        } catch (Exception e) {
            Log.e("DeletePositionTask", "Error deleting position", e);
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result.contains("\"success\": 1")) {
            // Notify success
            Log.d("DeletePositionTask", "Position deleted successfully");
            callback.onDeleteSuccess();
        } else {
            // Notify failure
            Log.d("DeletePositionTask", "Failed to delete position");
            callback.onDeleteFailure();
        }
    }

    // Callback interface for notifying results
    public interface DeleteCallback {
        void onDeleteSuccess();
        void onDeleteFailure();
    }
}